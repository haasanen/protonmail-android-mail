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

package ch.protonmail.android.mailnotifications.domain.mapper

import ch.protonmail.android.mailnotifications.data.remote.resource.NotificationActionType
import ch.protonmail.android.mailnotifications.data.wrapper.DecryptedPushNotificationWrapper
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotification
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationSenderData

internal object PushNotificationMapper {

    fun toLocalPushNotification(
        userPushData: LocalPushNotificationData.UserPushData,
        decryptedPushNotification: DecryptedPushNotificationWrapper
    ): LocalPushNotification {
        val pushData = when (decryptedPushNotification) {
            is DecryptedPushNotificationWrapper.Email -> mapEmailData(decryptedPushNotification)
            is DecryptedPushNotificationWrapper.OpenUrl -> mapUrlData(decryptedPushNotification)
        }

        return LocalPushNotification.invoke(userPushData, pushData)
    }

    private fun mapEmailData(data: DecryptedPushNotificationWrapper.Email): LocalPushNotificationData.MessagePushData {
        val (senderAddress, senderName, senderGroup) = with(data.sender) {
            Triple(senderAddress, senderName, senderGroup)
        }

        val sender = PushNotificationSenderData(
            senderName = senderName,
            senderAddress = senderAddress,
            senderGroup = senderGroup
        )
        val remoteMessageId = data.messageId.value
        val subject = data.subject.value

        // Temporary until we get the Action exposed by Rust SDK
        val action = when {
            senderAddress.isEmpty() &&
                senderName.isEmpty() &&
                senderGroup.isEmpty() &&
                subject.isEmpty() ->
                NotificationActionType.Touched

            else -> NotificationActionType.Created
        }

        return when (action) {
            NotificationActionType.Created -> LocalPushNotificationData.MessagePushData.NewMessagePushData(
                sender,
                remoteMessageId,
                subject
            )

            NotificationActionType.Touched ->
                LocalPushNotificationData.MessagePushData.MessageReadPushData(remoteMessageId)
        }
    }

    private fun mapUrlData(data: DecryptedPushNotificationWrapper.OpenUrl): LocalPushNotificationData.NewLoginPushData {
        val (senderAddress, senderName, senderGroup) = with(data.sender) {
            Triple(senderAddress, senderName, senderGroup)
        }
        val sender = PushNotificationSenderData(
            senderName = senderName,
            senderAddress = senderAddress,
            senderGroup = senderGroup
        )

        return LocalPushNotificationData.NewLoginPushData(sender, data.content.value, data.url.value)
    }
}
