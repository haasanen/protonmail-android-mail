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
import me.proton.core.domain.entity.UserId

internal sealed interface LocalPushNotificationData {

    data class UserPushData(
        val userId: UserId,
        val userEmail: String
    )

    data class NewLoginPushData(
        val sender: PushNotificationSenderData,
        val content: String,
        val url: String
    ) : LocalPushNotificationData

    sealed interface MessagePushData : LocalPushNotificationData {
        data class NewMessagePushData(
            val sender: PushNotificationSenderData,
            val messageId: String,
            val content: String
        ) : MessagePushData

        data class MessageReadPushData(
            val messageId: String
        ) : MessagePushData

        data class UnsupportedActionPushData(val action: NotificationActionType?) : MessagePushData
    }
}
