/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailnotifications.domain

import ch.protonmail.android.mailcommon.domain.AppInBackgroundState
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotification
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationSenderData
import ch.protonmail.android.mailnotifications.domain.proxy.NotificationManagerCompatProxy
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessMessageReadPushNotification
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessNewMessagePushNotification
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.mail_uniffi.LocalMailNotificationEvent
import uniffi.mail_uniffi.LocalMailNotificationStreamNextAsyncResult
import uniffi.mail_uniffi.MailUserSessionWatchNotificationsResult
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Consumes the core Rust local mail notification stream for every ready account and feeds the
 * existing local push notification pipeline. This covers the background-sync case where a new
 * message is stored locally but no FCM push arrives (e.g. the server did not emit a push, the
 * push was lost, or the device token is not registered as a push target), so the user still
 * gets a new-message notification.
 *
 * It is started from [Application.onCreate] (not from a lifecycle owner) on purpose: in a
 * background-only process (periodic sync worker) no activity is ever created, so a
 * ProcessLifecycleOwner-bound observer would never start and the stream would be silently
 * missed - exactly the case this observer exists for.
 *
 * The stream yields [LocalMailNotificationEvent.NewMessage] when a message is stored locally
 * and [LocalMailNotificationEvent.Dismiss] when a message becomes read. Both are mapped onto
 * [LocalPushNotification] and processed exactly like FCM-decrypted pushes, so the same
 * builder, grouping, deep links, quick actions and privacy toggles apply.
 *
 * [uniffi.mail_uniffi.LocalNewMessageNotification.remoteMessageId] is used as the message id
 * everywhere - the same id space as the FCM path (server message UUID). Consequences:
 *  - the notification id ([messageId.hashCode()]) collides with the FCM one, so a message
 *    delivered through both channels produces a single notification, not two;
 *  - the tap deep link resolves the message correctly (`GetMessageByRemoteId`).
 *
 * New-message notifications are only posted while the app is in the background and system
 * notifications are enabled (avoiding duplicate banners while the user is looking at the app);
 * read/dismiss events are processed unconditionally so stale notifications are always cleaned
 * up.
 *
 * The user session is owned by [UserSessionRepository]; this observer only borrows it to open
 * the stream and disposes of the stream itself when observation ends. The session is never
 * destroyed here.
 */
class LocalMailNotificationObserver @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val appInBackgroundState: AppInBackgroundState,
    private val notificationManagerCompatProxy: NotificationManagerCompatProxy,
    @AppScope private val coroutineScope: CoroutineScope
) {

    // Property-injected (not constructor-injected) because these pipeline use cases are
    // `internal` to this module and a public constructor of a public class may not expose
    // internal parameter types. Dagger cannot inject private fields, so they are `internal`.
    @Inject
    internal lateinit var processNewMessagePushNotification: ProcessNewMessagePushNotification

    @Inject
    internal lateinit var processMessageReadPushNotification: ProcessMessageReadPushNotification

    private var accountsJob: Job? = null
    private val watchedAccounts = ConcurrentHashMap<UserId, Job>()

    @Synchronized
    fun start() {
        if (accountsJob?.isActive == true) {
            Timber.tag(LogTag).d("Already started")
            return
        }
        Timber.tag(LogTag).d("Starting local notification stream observer")
        accountsJob = coroutineScope.launch {
            userSessionRepository.observeAccounts().collect { accounts ->
                accounts
                    .filter { it.state == AccountState.Ready }
                    .forEach { account -> startWatching(account.userId, account.primaryAddress) }
            }
        }
    }

    private fun startWatching(userId: UserId, userEmail: String) {
        watchedAccounts.computeIfAbsent(userId) {
            coroutineScope.launch { watch(it, userEmail) }
        }
    }

    private suspend fun watch(userId: UserId, userEmail: String) {
        val userSessionWrapper = userSessionRepository.getUserSession(userId)
        if (userSessionWrapper == null) {
            Timber.tag(LogTag).d("No user session for %s, skipping local notification stream", userId.id)
            watchedAccounts.remove(userId)
            return
        }
        runWatch(userSessionWrapper, userId, userEmail)
        // When runWatch returns (stream error or end) the job finishes and the entry is
        // removed; coverage resumes on the next account state re-emission or app start.
    }

    private suspend fun runWatch(
        userSessionWrapper: MailUserSessionWrapper,
        userId: UserId,
        userEmail: String
    ) {
        val userSession = userSessionWrapper.getRustUserSession()
        val stream = when (val streamResult = userSession.watchNotifications()) {
            is MailUserSessionWatchNotificationsResult.Error -> {
                Timber.tag(LogTag).w(
                    "Failed to open local notification stream for %s: %s",
                    userId.id,
                    streamResult.v1
                )
                null
            }

            is MailUserSessionWatchNotificationsResult.Ok -> streamResult.v1
        } ?: run {
            watchedAccounts.remove(userId)
            return
        }

        try {
            while (true) {
                when (val next = stream.nextAsync()) {
                    is LocalMailNotificationStreamNextAsyncResult.Error -> {
                        Timber.tag(LogTag).w(
                            "Local notification stream error for %s: %s",
                            userId.id,
                            next.v1
                        )
                        break
                    }

                    is LocalMailNotificationStreamNextAsyncResult.Ok -> {
                        // The UNIFFI binding exposes the payload as nullable.
                        val event = next.v1
                        if (event == null) {
                            Timber.tag(LogTag).w("Null local notification event for %s, skipping", userId.id)
                            continue
                        }
                        processEvent(event, userId, userEmail)
                    }
                }
            }
        } finally {
            stream.destroy()
            watchedAccounts.remove(userId)
        }
    }

    private suspend fun processEvent(event: LocalMailNotificationEvent, userId: UserId, userEmail: String) {
        val userPushData = LocalPushNotificationData.UserPushData(userId, userEmail)
        when (event) {
            is LocalMailNotificationEvent.NewMessage -> {
                val notification = event.v1
                if (appInBackgroundState.isAppInBackground() && notificationManagerCompatProxy.areNotificationsEnabled()) {
                    Timber.tag(LogTag).d(
                        "Local new message from %s, remoteId=%s, labelType=%s",
                        notification.senderAddress,
                        notification.remoteMessageId,
                        notification.labelType
                    )
                    processNewMessagePushNotification(
                        LocalPushNotification.Message.NewMessage(
                            userData = userPushData,
                            pushData = LocalPushNotificationData.MessagePushData.NewMessagePushData(
                                sender = PushNotificationSenderData(
                                    senderName = notification.senderName,
                                    senderAddress = notification.senderAddress,
                                    senderGroup = ""
                                ),
                                messageId = notification.remoteMessageId,
                                content = notification.subject
                            )
                        )
                    )
                }
            }

            is LocalMailNotificationEvent.Dismiss -> {
                Timber.tag(LogTag).d("Local message read: %s", event.remoteMessageId)
                processMessageReadPushNotification(
                    LocalPushNotification.Message.MessageRead(
                        userPushData = userPushData,
                        pushData = LocalPushNotificationData.MessagePushData.MessageReadPushData(
                            messageId = event.remoteMessageId
                        )
                    )
                )
            }
        }
    }

    companion object {
        private const val LogTag = "Local notification stream"
    }
}
