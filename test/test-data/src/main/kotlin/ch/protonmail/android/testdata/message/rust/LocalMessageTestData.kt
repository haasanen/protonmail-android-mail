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

package ch.protonmail.android.testdata.message.rust

import uniffi.proton_api_mail.ExternalId
import uniffi.proton_api_mail.MessageAddress
import uniffi.proton_api_mail.MessageFlags
import uniffi.proton_mail_common.AvatarInformation
import uniffi.proton_mail_common.LocalAttachmentMetadata
import uniffi.proton_mail_common.LocalInlineLabelInfo
import uniffi.proton_mail_common.LocalMessageId
import uniffi.proton_mail_common.LocalMessageMetadata

object LocalMessageTestData {
    const val RAW_SUBJECT = "Subject"
    const val RAW_MESSAGE_ID = 1000uL

    val sender = MessageAddress(
        address = "sender@pm.me",
        name = "Sender",
        isProton = true,
        displaySenderImage = false,
        isSimpleLogin = false,
        bimiSelector = null
    )

    val recipient1 = MessageAddress(
        address = "recipient1@pm.me", name = "Recipient1",
        isProton = true,
        displaySenderImage = false,
        isSimpleLogin = false,
        bimiSelector = null
    )
    val recipient2 = MessageAddress(
        address = "recipient2@pm.me", name = "Recipient2",
        isProton = true,
        displaySenderImage = false,
        isSimpleLogin = false,
        bimiSelector = null
    )
    val recipient3 = MessageAddress(
        address = "recipient3@pm.me", name = "Recipient3",
        isProton = true,
        displaySenderImage = false,
        isSimpleLogin = false,
        bimiSelector = null
    )

    val AugWeatherForecast = buildMessage(
        id = LocalMessageIdSample.AugWeatherForecast,
        subject = "August weather forecast",
        sender = sender,
        to = listOf(recipient1),
        cc = emptyList(),
        bcc = emptyList(),
        time = 1667924198uL
    )

    val SepWeatherForecast = buildMessage(
        id = LocalMessageIdSample.SepWeatherForecast,
        subject = "September weather forecast",
        sender = sender,
        to = listOf(recipient1),
        cc = emptyList(),
        bcc = emptyList(),
        time = 1667924198uL
    )

    val OctWeatherForecast = buildMessage(
        id = LocalMessageIdSample.OctWeatherForecast,
        subject = "October weather forecast",
        sender = sender,
        to = listOf(recipient1),
        cc = emptyList(),
        bcc = emptyList(),
        time = 1667924198uL
    )


    val multipleRecipientsMessage = buildMessage(
        id = RAW_MESSAGE_ID,
        subject = "Multiple recipients message",
        sender = sender,
        to = listOf(recipient1, recipient2),
        cc = emptyList(),
        bcc = emptyList(),
        time = 1667924198uL
    )

    val trashedMessage = buildMessage(
        id = RAW_MESSAGE_ID,
        subject = "Trashed message",
        sender = sender,
        to = listOf(recipient1),
        cc = emptyList(),
        bcc = emptyList(),
        labels = listOf(LocalInlineLabelInfo(2uL, "Trash", "red")),
        time = 1667924198uL
    )

    val trashedMessageWithCustomLabels = buildMessage(
        id = RAW_MESSAGE_ID,
        subject = "Trashed message with custom labels",
        sender = sender,
        to = listOf(recipient1),
        cc = emptyList(),
        bcc = emptyList(),
        labels = listOf(
            LocalInlineLabelInfo(2uL, "Trash", "red"),
            LocalInlineLabelInfo(3uL, "Travel", "blue")
        ),
        time = 1667924198uL
    )

    val spamMessage = buildMessage(
        id = RAW_MESSAGE_ID,
        subject = RAW_SUBJECT,
        sender = sender,
        to = listOf(recipient1),
        cc = emptyList(),
        bcc = emptyList(),
        labels = listOf(LocalInlineLabelInfo(4uL, "Spam", "yellow")),
        time = 1667924198uL
    )

    val spamMessageWithMultipleRecipients = buildMessage(
        id = RAW_MESSAGE_ID,
        subject = RAW_SUBJECT,
        sender = sender,
        to = listOf(recipient1),
        cc = listOf(recipient2),
        bcc = emptyList(),
        labels = listOf(LocalInlineLabelInfo(4uL, "Spam", "yellow")),
        time = 1667924198uL
    )

    val starredMessage = buildMessage(
        id = RAW_MESSAGE_ID,
        subject = RAW_SUBJECT,
        sender = sender,
        to = listOf(recipient1),
        cc = emptyList(),
        bcc = emptyList(),
        labels = listOf(LocalInlineLabelInfo(1uL, "Inbox", "green"), LocalInlineLabelInfo(5uL, "Starred", "yellow")),
        time = 1667924198uL
    )

    val starredMessagesWithCustomLabel = listOf(
        buildMessage(
            id = 123uL,
            subject = "Message 123",
            sender = sender,
            to = listOf(recipient1),
            labels = listOf(
                LocalInlineLabelInfo(1uL, "Inbox", "green"),
                LocalInlineLabelInfo(5uL, "Starred", "yellow"),
                LocalInlineLabelInfo(11uL, "Custom", "blue")
            ),
            time = 1667924198uL
        ),
        buildMessage(
            id = 124uL,
            subject = "Message 124",
            sender = sender,
            to = listOf(recipient2),
            labels = listOf(
                LocalInlineLabelInfo(1uL, "Inbox", "green"),
                LocalInlineLabelInfo(5uL, "Starred", "yellow"),
                LocalInlineLabelInfo(11uL, "Custom", "blue")
            ),
            time = 1667924198uL
        ),
        buildMessage(
            id = 125uL,
            subject = "Message 125",
            sender = sender,
            to = listOf(recipient3),
            labels = listOf(
                LocalInlineLabelInfo(1uL, "Inbox", "green"),
                LocalInlineLabelInfo(5uL, "Starred", "yellow")
            ),
            time = 1667924198uL
        )
    )

    fun buildMessage(
        id: LocalMessageId,
        subject: String,
        sender: MessageAddress,
        to: List<MessageAddress>,
        cc: List<MessageAddress> = emptyList(),
        bcc: List<MessageAddress> = emptyList(),
        labels: List<LocalInlineLabelInfo> = listOf(LocalInlineLabelInfo(1uL, "Inbox", "green")),
        time: ULong,
        size: ULong = 0uL,
        expirationTime: ULong = 0uL,
        snoozeTime: ULong = 0uL,
        isReplied: Boolean = false,
        isRepliedAll: Boolean = false,
        isForwarded: Boolean = false,
        externalId: ExternalId? = null,
        numAttachments: UInt = 0u,
        flags: MessageFlags = 0uL,
        starred: Boolean = false,
        attachments: List<LocalAttachmentMetadata>? = null,
        avatarInformation: AvatarInformation = AvatarInformation("A", "blue")
    ) = LocalMessageMetadata(
        id = id,
        rid = null,
        conversationId = 1uL,
        addressId = "1",
        order = 1uL,
        subject = subject,
        unread = false,
        sender = sender,
        to = to,
        cc = cc,
        bcc = bcc,
        time = time,
        size = size,
        expirationTime = expirationTime,
        snoozeTime = snoozeTime,
        isReplied = isReplied,
        isRepliedAll = isRepliedAll,
        isForwarded = isForwarded,
        externalId = externalId,
        numAttachments = numAttachments,
        flags = flags,
        starred = starred,
        attachments = attachments,
        labels = labels,
        avatarInformation = avatarInformation
    )
}
