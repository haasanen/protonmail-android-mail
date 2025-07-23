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

package ch.protonmail.android.mailnotifications.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.maildetail.domain.usecase.MarkMessageAsRead
import ch.protonmail.android.maildetail.domain.usecase.MoveMessage
import ch.protonmail.android.mailmessage.domain.model.RemoteMessageId
import ch.protonmail.android.mailmessage.domain.usecase.ObserveMessage
import ch.protonmail.android.mailnotifications.domain.model.LocalNotificationAction
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationDismissPendingIntentData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationPendingIntentPayloadData
import ch.protonmail.android.mailnotifications.domain.usecase.DismissEmailNotificationsForUser
import ch.protonmail.android.mailnotifications.domain.usecase.actions.CreateNotificationAction.Companion.NotificationActionIntentExtraKey
import ch.protonmail.android.mailnotifications.domain.usecase.actions.CreateNotificationAction.Companion.NotificationDismissalIntentExtraKey
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.deserialize
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
internal class PushNotificationActionsBroadcastReceiver @Inject constructor() : BroadcastReceiver() {

    @Inject
    lateinit var observeMessage: ObserveMessage

    @Inject
    lateinit var moveMessage: MoveMessage

    @Inject
    lateinit var markAsRead: MarkMessageAsRead

    @Inject
    lateinit var dismissEmailNotificationsForUser: DismissEmailNotificationsForUser

    @Inject
    @AppScope
    lateinit var coroutineScope: CoroutineScope

    @Inject
    @ApplicationContext
    lateinit var applicationContext: Context

    override fun onReceive(context: Context?, intent: Intent?) {
        val rawAction = intent?.extras?.getString(NotificationActionIntentExtraKey)
        if (rawAction != null) {
            val actionData = rawAction.deserialize<PushNotificationPendingIntentPayloadData>()
            handleNotificationAction(actionData)
            return
        }

        val dismissalAction = intent?.extras?.getString(NotificationDismissalIntentExtraKey)
        if (dismissalAction != null) {
            val actionData = dismissalAction.deserialize<PushNotificationDismissPendingIntentData>()
            handleNotificationDismissal(actionData)
            return
        }
    }

    private fun handleNotificationAction(actionData: PushNotificationPendingIntentPayloadData) {
        val userId = UserId(actionData.userId)
        val remoteMessageId = RemoteMessageId(actionData.messageId)

        coroutineScope.launch {
            val message = observeMessage(userId, remoteMessageId)
                .firstOrNull()
                ?.getOrNull()

            if (message == null) {
                Timber.e("Unable to fetch message for action ${actionData.action} - remoteId '$remoteMessageId'")
                return@launch
            }

            when (val action = actionData.action) {
                is LocalNotificationAction.MoveTo -> {
                    val result = moveMessage.invoke(
                        userId = userId,
                        messageId = message.messageId,
                        systemLabelId = action.destinationLabel
                    )

                    result.onLeft {
                        Timber.e("Error moving message from notification action: $it")
                    }.onRight {
                        Timber.d("Message moved successfully from notification action: ${message.messageId}")
                    }
                }

                is LocalNotificationAction.MarkAsRead -> {
                    markAsRead(userId, message.messageId).getOrElse {
                        return@launch Timber.e("Unable to find message with id $remoteMessageId.")
                    }

                    Timber.d("Message marked as read.")
                }
            }

            dismissEmailNotificationsForUser(
                userId = userId,
                notificationId = actionData.notificationId,
                checkIfNotificationExists = false
            )
        }
    }

    private fun handleNotificationDismissal(actionData: PushNotificationDismissPendingIntentData) {
        when (actionData) {
            is PushNotificationDismissPendingIntentData.GroupNotification -> {
                dismissEmailNotificationsForUser(
                    userId = UserId(actionData.groupId)
                )
            }

            is PushNotificationDismissPendingIntentData.SingleNotification -> {
                dismissEmailNotificationsForUser(
                    userId = UserId(actionData.userId),
                    notificationId = actionData.notificationId,
                    checkIfNotificationExists = true // Always check for swipe dismissals
                )
            }
        }
    }
}
