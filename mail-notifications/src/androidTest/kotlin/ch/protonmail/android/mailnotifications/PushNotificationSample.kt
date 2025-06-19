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

package ch.protonmail.android.mailnotifications

import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotification
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationSenderData
import me.proton.core.domain.entity.UserId

internal object PushNotificationSample {

    private val SampleNewMessagePushNotificationData = LocalPushNotification(
        userPushData = LocalPushNotificationData.UserPushData(UserId("userId"), "userEmail"),
        pushNotificationData = LocalPushNotificationData.MessagePushData.NewMessagePushData(
            sender = PushNotificationSenderData("SenderEmail", "Sender", ""),
            messageId = "aMessageId",
            content = "Notification"
        )
    )

    private val SampleMessageReadPushNotificationData = LocalPushNotification(
        userPushData = LocalPushNotificationData.UserPushData(UserId("userId"), "userEmail"),
        pushNotificationData = LocalPushNotificationData.MessagePushData.MessageReadPushData(messageId = "aMessageId")
    )

    private val SampleLoginPushNotificationData = LocalPushNotification(
        userPushData = LocalPushNotificationData.UserPushData(UserId("userId"), "userEmail"),
        pushNotificationData = LocalPushNotificationData.MessagePushData.NewMessagePushData(
            sender = PushNotificationSenderData("Proton Mail", "abuse@proton.me", ""),
            messageId = "",
            content = "New login attempt"
        )
    )

    fun getSampleLoginAlertNotification() = SampleLoginPushNotificationData
    fun getSampleMessageReadNotification() = SampleMessageReadPushNotificationData
    fun getSampleNewMessageNotification() = SampleNewMessagePushNotificationData
}
