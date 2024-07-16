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

package ch.protonmail.android.mailconversation.data.mapper

import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import ch.protonmail.android.mailmessage.data.mapper.toParticipant
import ch.protonmail.android.mailmessage.domain.model.AttachmentCount
import me.proton.core.label.domain.entity.LabelId
import org.junit.Test
import uniffi.proton_api_mail.Disposition
import uniffi.proton_api_mail.MessageAddress
import uniffi.proton_mail_common.AvatarInformation
import uniffi.proton_mail_common.LocalAttachmentMetadata
import uniffi.proton_mail_common.LocalConversation
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalInlineLabelInfo
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConversationMapperTest {

    @Test
    fun `local conversation to conversation should convert correctly`() {
        // Given
        val id: LocalConversationId = 123uL
        val remoteId = "remote123"
        val order = 1uL
        val subject = "Test Subject"
        val senders = listOf(
            MessageAddress("sender1@test.com", "Sender1", true, false, false, null),
            MessageAddress("sender2@test.com", "Sender2", false, false, false, null)
        )
        val recipients = listOf(
            MessageAddress("recipient1@test.com", "Recipient1", true, false, false, null),
            MessageAddress("recipient2@test.com", "Recipient2", false, false, false, null)
        )
        val numMessages = 5uL
        val numMessagesCtx = 3uL
        val numUnread = 2uL
        val numAttachments = 1uL
        val expirationTime = 1625235000000uL
        val snoozeTime = 1625240000000uL
        val size = 1024uL
        val time = 1625250000000uL
        val labels = listOf(
            LocalInlineLabelInfo(1uL, "Test Label", "0xFF0000")
        )
        val starred = false
        val attachments = listOf(
            LocalAttachmentMetadata(
                id = 123uL,
                rid = "rid",
                name = "file.txt",
                mimeType = "text/plain",
                size = 123uL,
                disposition = Disposition.ATTACHMENT
            )
        )
        val avatarInformation = AvatarInformation("A", "blue")

        val localConversation = LocalConversation(
            id = id,
            remoteId = remoteId,
            order = order,
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

        // When
        val conversation = localConversation.toConversation()

        // Then
        assertEquals(id.toString(), conversation.conversationId.id)
        assertEquals(FAKE_USER_ID, conversation.userId)
        assertEquals(order.toLong(), conversation.order)
        assertEquals(subject, conversation.subject)
        assertEquals(senders.map { it.toParticipant() }, conversation.senders)
        assertEquals(recipients.map { it.toParticipant() }, conversation.recipients)
        assertEquals(numMessages.toInt(), conversation.numMessages)
        assertEquals(numUnread.toInt(), conversation.numUnread)
        assertEquals(numAttachments.toInt(), conversation.numAttachments)
        assertEquals(expirationTime.toLong(), conversation.expirationTime)
        assertEquals(
            labels.map {
                it.toConversationLabel(
                    ConversationId(id.toString()),
                    numMessages.toInt(),
                    numUnread.toInt(),
                    numAttachments.toInt(),
                    time.toLong(),
                    size.toLong()
                )
            },
            conversation.labels
        )
        assertEquals(AttachmentCount(numAttachments.toInt()), conversation.attachmentCount)
        assertTrue(!conversation.starred)
    }

    @Test
    fun `LocalConversation to ConversationWithContext should convert correctly`() {
        // Given
        val id: LocalConversationId = 123uL
        val remoteId = "remote123"
        val order = 1uL
        val subject = "Test Subject"
        val senders = listOf(
            MessageAddress("sender1@test.com", "Sender1", true, false, false, null),
            MessageAddress("sender2@test.com", "Sender2", false, false, false, null)
        )
        val recipients = listOf(
            MessageAddress("recipient1@test.com", "Recipient1", true, false, false, null),
            MessageAddress("recipient2@test.com", "Recipient2", false, false, false, null)
        )
        val numMessages = 5uL
        val numMessagesCtx = 3uL
        val numUnread = 2uL
        val numAttachments = 1uL
        val expirationTime = 1625235000000uL
        val snoozeTime = 1625240000000uL
        val size = 1024uL
        val time = 1625250000000uL
        val labels = listOf(
            LocalInlineLabelInfo(1uL, "Test Label", "0xFF0000")
        )
        val starred = false
        val attachments = listOf(
            LocalAttachmentMetadata(
                id = 123uL,
                rid = "rid",
                name = "file.txt",
                mimeType = "text/plain",
                size = 123uL,
                disposition = Disposition.ATTACHMENT
            )
        )
        val avatarInformation = AvatarInformation("A", "blue")
        val contextLabelId = LabelId("contextLabelId")

        val localConversation = LocalConversation(
            id = id,
            remoteId = "remoteId",
            order = order,
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

        // When
        val conversationWithContext = localConversation.toConversationWithContext(contextLabelId)

        // Then
        assertEquals(contextLabelId, conversationWithContext.contextLabelId)
        val conversation = conversationWithContext.conversation
        assertEquals(id.toString(), conversation.conversationId.id)
        assertEquals(FAKE_USER_ID, conversation.userId)
        assertEquals(order.toLong(), conversation.order)
        assertEquals(subject, conversation.subject)
        assertEquals(senders.map { it.toParticipant() }, conversation.senders)
        assertEquals(recipients.map { it.toParticipant() }, conversation.recipients)
        assertEquals(numMessages.toInt(), conversation.numMessages)
        assertEquals(numUnread.toInt(), conversation.numUnread)
        assertEquals(numAttachments.toInt(), conversation.numAttachments)
        assertEquals(expirationTime.toLong(), conversation.expirationTime)
        assertEquals(
            labels.map {
                it.toConversationLabel(
                    ConversationId(id.toString()),
                    numMessages.toInt(),
                    numUnread.toInt(),
                    numAttachments.toInt(),
                    time.toLong(),
                    size.toLong()
                )
            },
            conversation.labels
        )
        assertEquals(AttachmentCount(numAttachments.toInt()), conversation.attachmentCount)
        assertTrue(!conversation.starred)
    }

    @Test
    fun `LocalInlineLabelInfo to ConversationLabel should convert correctly`() {
        // Given
        val localInlineLabelInfo = LocalInlineLabelInfo(
            id = 1uL,
            name = "Test Label",
            color = "0xFF0000"
        )
        val conversationId = ConversationId("remote123")
        val contextNumMessages = 5
        val contextNumUnread = 2
        val contextNumAttachments = 1
        val contextTime = 1_625_250_000L
        val contextSize = 1024L

        // When
        val conversationLabel = localInlineLabelInfo.toConversationLabel(
            conversationId = conversationId,
            contextNumMessages = contextNumMessages,
            contextNumUnread = contextNumUnread,
            contextNumAttachments = contextNumAttachments,
            contextTime = contextTime,
            contextSize = contextSize
        )

        // Then
        assertEquals(conversationId, conversationLabel.conversationId)
        assertEquals(LabelId(localInlineLabelInfo.id.toString()), conversationLabel.labelId)
        assertEquals(contextNumMessages, conversationLabel.contextNumMessages)
        assertEquals(contextNumUnread, conversationLabel.contextNumUnread)
        assertEquals(contextNumAttachments, conversationLabel.contextNumAttachments)
        assertEquals(contextTime, conversationLabel.contextTime)
        assertEquals(contextSize, conversationLabel.contextSize)
    }
}
