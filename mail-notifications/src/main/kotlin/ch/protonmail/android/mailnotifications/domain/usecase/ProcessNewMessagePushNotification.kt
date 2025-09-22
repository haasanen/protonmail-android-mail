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

package ch.protonmail.android.mailnotifications.domain.usecase

import android.content.Context
import androidx.work.ListenableWorker
import ch.protonmail.android.mailcommon.presentation.system.NotificationProvider
import ch.protonmail.android.mailnotifications.R
import ch.protonmail.android.mailnotifications.domain.model.LocalNotificationAction
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotification
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationDismissPendingIntentData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationPendingIntentPayloadData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationSenderData
import ch.protonmail.android.mailnotifications.domain.proxy.NotificationManagerCompatProxy
import ch.protonmail.android.mailnotifications.domain.usecase.actions.CreateNotificationAction
import ch.protonmail.android.mailnotifications.domain.usecase.intents.CreateNewMessageNavigationIntent
import ch.protonmail.android.mailsettings.domain.usecase.notifications.GetExtendedNotificationsSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class ProcessNewMessagePushNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationProvider: NotificationProvider,
    private val notificationManagerCompatProxy: NotificationManagerCompatProxy,
    private val getNotificationsExtendedPreference: GetExtendedNotificationsSetting,
    private val createNewMessageNavigationIntent: CreateNewMessageNavigationIntent,
    private val createNotificationAction: CreateNotificationAction
) {

    @Suppress("LongMethod")
    suspend operator fun invoke(notificationData: LocalPushNotification.Message.NewMessage): ListenableWorker.Result {

        val userData = notificationData.userData
        val pushData = notificationData.pushData

        val notificationTitle = resolveNotificationTitle(pushData.sender)
        val notificationUserAddress = userData.userEmail
        val notificationContent = pushData.content
        val notificationGroup = userData.userId.id
        val notificationId = pushData.messageId.hashCode()
        val groupNotificationId = notificationGroup.hashCode()

        val notification = notificationProvider.provideEmailNotificationBuilder(
            context = context,
            contentTitle = notificationTitle,
            subText = notificationUserAddress,
            contentText = notificationContent,
            group = notificationGroup,
            autoCancel = true
        ).apply {
            setContentIntent(
                createNewMessageNavigationIntent(notificationId, pushData.messageId, userData.userId.id)
            )

            val archiveAction = PushNotificationPendingIntentPayloadData(
                notificationId,
                notificationGroup,
                userData.userId.id,
                pushData.messageId,
                LocalNotificationAction.MoveTo.Archive
            )

            val trashAction = archiveAction.copy(action = LocalNotificationAction.MoveTo.Trash)
            val markAsReadAction = archiveAction.copy(action = LocalNotificationAction.MarkAsRead)

            addAction(createNotificationAction(archiveAction))
            addAction(createNotificationAction(trashAction))
            addAction(createNotificationAction(markAsReadAction))

            val dismissalAction = PushNotificationDismissPendingIntentData.SingleNotification(
                userData.userId.id,
                notificationId
            )

            setDeleteIntent(createNotificationAction(dismissalAction))
        }.build()

        val groupNotification = notificationProvider.provideEmailNotificationBuilder(
            context = context,
            contentTitle = notificationTitle,
            subText = notificationUserAddress,
            contentText = context.getString(R.string.notification_summary_text_new_messages),
            group = notificationGroup,
            isGroupSummary = true,
            autoCancel = true
        ).apply {
            setContentIntent(createNewMessageNavigationIntent(notificationId, userData.userId.id))

            val dismissalAction = PushNotificationDismissPendingIntentData.GroupNotification(userData.userId.id)
            setDeleteIntent(createNotificationAction(dismissalAction))
        }.build()

        notificationManagerCompatProxy.run {
            showNotification(notificationId, notification)
            showNotification(groupNotificationId, groupNotification)
        }

        return ListenableWorker.Result.success()
    }

    private suspend fun resolveNotificationTitle(sender: PushNotificationSenderData): String {
        val hasNotificationsExtended = getNotificationsExtendedPreference().getOrNull()?.enabled ?: true

        return if (hasNotificationsExtended) {
            sender.senderName
                .ifEmpty { sender.senderAddress }
                .takeIf { it.isNotEmpty() }
                ?: context.getString(R.string.notification_title_text_new_message_fallback)
        } else {
            context.getString(R.string.notification_title_text_new_message_fallback)
        }
    }

}
