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

package ch.protonmail.android.testdata.conversation.rust

import uniffi.proton_api_mail.MessageAddress
import uniffi.proton_mail_common.AvatarInformation
import uniffi.proton_mail_common.LocalAttachmentMetadata
import uniffi.proton_mail_common.LocalConversation
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalInlineLabelInfo

object LocalConversationTestData {
    const val RAW_SUBJECT = "Conversation Subject"
    const val RAW_CONVERSATION_ID = 1000uL

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

    val AugConversation = buildConversation(
        id = LocalConversationIdSample.AugConversation,
        subject = "August conversation",
        senders = listOf(sender),
        recipients = listOf(recipient1),
        time = 1667924198uL
    )

    val SepConversation = buildConversation(
        id = LocalConversationIdSample.SepConversation,
        subject = "September conversation",
        senders = listOf(sender),
        recipients = listOf(recipient1),
        time = 1667924198uL
    )

    val OctConversation = buildConversation(
        id = LocalConversationIdSample.OctConversation,
        subject = "October conversation",
        senders = listOf(sender),
        recipients = listOf(recipient1),
        time = 1667924198uL
    )

    val multipleRecipientsConversation = buildConversation(
        id = RAW_CONVERSATION_ID,
        subject = "Multiple recipients conversation",
        senders = listOf(sender),
        recipients = listOf(recipient1, recipient2),
        time = 1667924198uL
    )

    val trashedConversation = buildConversation(
        id = RAW_CONVERSATION_ID,
        subject = "Trashed conversation",
        senders = listOf(sender),
        recipients = listOf(recipient1),
        labels = listOf(LocalInlineLabelInfo(2uL, "Trash", "red")),
        time = 1667924198uL
    )

    val trashedConversationWithCustomLabels = buildConversation(
        id = RAW_CONVERSATION_ID,
        subject = "Trashed conversation with custom labels",
        senders = listOf(sender),
        recipients = listOf(recipient1),
        labels = listOf(
            LocalInlineLabelInfo(2uL, "Trash", "red"),
            LocalInlineLabelInfo(3uL, "Travel", "blue")
        ),
        time = 1667924198uL
    )

    val spamConversation = buildConversation(
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        senders = listOf(sender),
        recipients = listOf(recipient1),
        labels = listOf(LocalInlineLabelInfo(4uL, "Spam", "yellow")),
        time = 1667924198uL
    )

    val spamConversationWithMultipleRecipients = buildConversation(
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        senders = listOf(sender),
        recipients = listOf(recipient1),
        labels = listOf(LocalInlineLabelInfo(4uL, "Spam", "yellow")),
        time = 1667924198uL
    )

    val starredConversation = buildConversation(
        id = RAW_CONVERSATION_ID,
        subject = RAW_SUBJECT,
        senders = listOf(sender),
        recipients = listOf(recipient1),
        labels = listOf(LocalInlineLabelInfo(1uL, "Inbox", "green"), LocalInlineLabelInfo(5uL, "Starred", "yellow")),
        time = 1667924198uL
    )

    val starredConversationsWithCustomLabel = listOf(
        buildConversation(
            id = 123uL,
            subject = "Conversation 123",
            senders = listOf(sender),
            recipients = listOf(recipient1),
            labels = listOf(
                LocalInlineLabelInfo(1uL, "Inbox", "green"),
                LocalInlineLabelInfo(5uL, "Starred", "yellow"),
                LocalInlineLabelInfo(11uL, "Custom", "blue")
            ),
            time = 1667924198uL
        ),
        buildConversation(
            id = 124uL,
            subject = "Conversation 124",
            senders = listOf(sender),
            recipients = listOf(recipient2),
            labels = listOf(
                LocalInlineLabelInfo(1uL, "Inbox", "green"),
                LocalInlineLabelInfo(5uL, "Starred", "yellow"),
                LocalInlineLabelInfo(11uL, "Custom", "blue")
            ),
            time = 1667924198uL
        ),
        buildConversation(
            id = 125uL,
            subject = "Conversation 125",
            senders = listOf(sender),
            recipients = listOf(recipient3),
            labels = listOf(
                LocalInlineLabelInfo(1uL, "Inbox", "green"),
                LocalInlineLabelInfo(5uL, "Starred", "yellow")
            ),
            time = 1667924198uL
        )
    )

    fun buildConversation(
        id: LocalConversationId,
        subject: String,
        senders: List<MessageAddress>,
        recipients: List<MessageAddress>,
        labels: List<LocalInlineLabelInfo> = listOf(LocalInlineLabelInfo(1uL, "Inbox", "green")),
        time: ULong,
        size: ULong = 0uL,
        expirationTime: ULong = 0uL,
        snoozeTime: ULong = 0uL,
        numMessages: ULong = 1uL,
        numMessagesCtx: ULong = 1uL,
        numUnread: ULong = 0uL,
        numAttachments: ULong = 0uL,
        starred: Boolean = false,
        attachments: List<LocalAttachmentMetadata>? = emptyList(),
        avatarInformation: AvatarInformation = AvatarInformation("A", "blue")
    ) = LocalConversation(
        id = id,
        remoteId = null,
        order = 1uL,
        subject = subject,
        senders = senders,
        recipients = recipients,
        numMessages = numMessages,
        numMessagesCtx = numMessagesCtx,
        numUnread = numUnread,
        numAttachments = numAttachments,
        expirationTime = expirationTime,
        snoozeTime = snoozeTime,
        size = size,
        time = time,
        labels = labels,
        starred = starred,
        attachments = attachments,
        avatarInformation = avatarInformation
    )
}
