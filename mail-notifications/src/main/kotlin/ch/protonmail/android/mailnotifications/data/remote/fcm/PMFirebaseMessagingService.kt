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

package ch.protonmail.android.mailnotifications.data.remote.fcm

import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailnotifications.data.repository.NotificationTokenRepository
import ch.protonmail.android.mailnotifications.domain.usecase.content.ProcessPushNotificationMessage
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

@AndroidEntryPoint
internal class PMFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationTokenRepository: NotificationTokenRepository

    @Inject
    lateinit var processPushNotificationMessage: ProcessPushNotificationMessage

    @Inject
    lateinit var scopeProvider: CoroutineScopeProvider

    @Inject
    @AppScope
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var userSessionRepository: UserSessionRepository

    private var onNewTokenJob: Job? = null

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        onNewTokenJob?.cancel()
        onNewTokenJob = scopeProvider.GlobalDefaultSupervisedScope.launch {
            notificationTokenRepository.storeToken(token)

            userSessionRepository
                .observeAccounts()
                .distinctUntilChanged()
                .map { accounts -> accounts.filter { it.state == AccountState.Ready } }
                .map { readyAccounts -> readyAccounts.map { it.userId } }
                .collect { userIds ->
                    userIds.forEach { userId ->
                        notificationTokenRepository.bindTokenToUser(userId)
                    }
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val uid = remoteMessage.data["UID"]
        val encryptedMessage = remoteMessage.data["encryptedMessage"]
        if (uid != null && encryptedMessage != null) {
            appScope.launch { processPushNotificationMessage(SessionId(uid), encryptedMessage) }
        }
    }

    override fun onDestroy() {
        onNewTokenJob?.cancel()
        super.onDestroy()
    }
}
