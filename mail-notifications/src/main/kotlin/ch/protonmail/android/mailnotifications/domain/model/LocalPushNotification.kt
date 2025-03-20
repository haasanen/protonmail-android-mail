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

package ch.protonmail.android.mailnotifications.domain.model

import ch.protonmail.android.mailnotifications.data.remote.resource.NotificationActionType
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData.MessagePushData.MessageReadPushData
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData.MessagePushData.NewMessagePushData
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData.MessagePushData.UnsupportedActionPushData
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData.NewLoginPushData
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData.UserPushData

internal sealed class LocalPushNotification {

    sealed class Message : LocalPushNotification() {
        data class MessageRead(
            val userPushData: UserPushData,
            val pushData: MessageReadPushData
        ) : Message()

        data class NewMessage(
            val userData: UserPushData,
            val pushData: NewMessagePushData
        ) : Message()

        data class UnsupportedMessageAction(
            val actionType: NotificationActionType?
        ) : Message()
    }

    data class Login(
        val userData: UserPushData,
        val pushData: NewLoginPushData
    ) : LocalPushNotification()

    companion object {

        operator fun invoke(userPushData: UserPushData, pushNotificationData: LocalPushNotificationData) =
            when (pushNotificationData) {
                is MessageReadPushData -> Message.MessageRead(userPushData, pushNotificationData)
                is NewMessagePushData -> Message.NewMessage(userPushData, pushNotificationData)
                is UnsupportedActionPushData -> Message.UnsupportedMessageAction(pushNotificationData.action)
                is NewLoginPushData -> Login(userPushData, pushNotificationData)
            }
    }
}
