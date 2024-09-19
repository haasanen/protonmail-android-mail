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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalAttachmentMetadata
import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversation
import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.maillabel.data.mapper.toLabel
import ch.protonmail.android.mailmessage.data.mapper.toParticipant
import ch.protonmail.android.mailmessage.domain.model.AttachmentCount
import ch.protonmail.android.maillabel.domain.model.LabelId
import org.junit.Test
import uniffi.proton_mail_uniffi.AttachmentMimeType
import uniffi.proton_mail_uniffi.AvatarInformation
import uniffi.proton_mail_uniffi.Disposition
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.InlineCustomLabel
import uniffi.proton_mail_uniffi.LabelColor
import uniffi.proton_mail_uniffi.MessageAddress
import uniffi.proton_mail_uniffi.MimeTypeCategory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConversationMapperTest {

    @Test
    fun `local conversation to conversation should convert correctly`() {
        // Given
        val id = LocalConversationId(123uL)
        val order = 1uL
        val subject = "Test Subject"
        val senders = listOf(
            MessageAddress("sender1@test.com", "Sender1", true, false, false, ""),
            MessageAddress("sender2@test.com", "Sender2", false, false, false, "")
        )
        val recipients = listOf(
            MessageAddress("recipient1@test.com", "Recipient1", true, false, false, ""),
            MessageAddress("recipient2@test.com", "Recipient2", false, false, false, "")
        )
        val totalNumMessages = 8uL
        val totalNumUnread = 3uL
        val numMessages = 5uL
        val numUnread = 2uL
        val numAttachments = 1uL
        val expirationTime = 1625235000000uL
        val size = 1024uL
        val time = 1625250000000uL
        val labels = listOf(
            InlineCustomLabel(LocalLabelId(1uL), "Test Label", LabelColor("0xFF0000"))
        )
        val starred = false
        val attachments = listOf(
            LocalAttachmentMetadata(
                id = Id(123uL),
                name = "file.txt",
                mimeType = AttachmentMimeType("text/plain", MimeTypeCategory.TEXT),
                size = 123uL,
                disposition = Disposition.ATTACHMENT
            )
        )
        val avatarInformation = AvatarInformation("A", "blue")

        val localConversation = LocalConversation(
            id = id,
            displayOrder = order,
            subject = subject,
            senders = senders,
            recipients = recipients,
            numMessages = numMessages,
            numUnread = numUnread,
            numAttachments = numAttachments,
            expirationTime = expirationTime,
            size = size,
            time = time,
            customLabels = labels,
            isStarred = starred,
            attachmentsMetadata = attachments,
            displaySnoozeReminder = false,
            exclusiveLocation = null,
            avatar = avatarInformation,
            totalMessages = totalNumMessages,
            totalUnread = totalNumUnread
        )

        // When
        val conversation = localConversation.toConversation()

        // Then
        val expectedId = ConversationId(id.value.toString())
        assertEquals(expectedId, conversation.conversationId)
        assertEquals(order.toLong(), conversation.order)
        assertEquals(subject, conversation.subject)
        assertEquals(senders.map { it.toParticipant() }, conversation.senders)
        assertEquals(recipients.map { it.toParticipant() }, conversation.recipients)
        assertEquals(numMessages.toInt(), conversation.numMessages)
        assertEquals(numUnread.toInt(), conversation.numUnread)
        assertEquals(numAttachments.toInt(), conversation.numAttachments)
        assertEquals(expirationTime.toLong(), conversation.expirationTime)
        assertEquals(avatarInformation.text, conversation.avatarInformation.initials)
        assertEquals(avatarInformation.color, conversation.avatarInformation.color)
        assertEquals(
            labels.map { it.toLabel() },
            conversation.customLabels
        )
        assertEquals(AttachmentCount(numAttachments.toInt()), conversation.attachmentCount)
        assertTrue(!conversation.isStarred)
    }

    @Test
    fun `InlineCustomLabel to ConversationLabel should convert correctly`() {
        // Given
        val name = "Test Label"
        val color = LabelColor("0xFF0000")
        val customLabel = InlineCustomLabel(
            id = LocalLabelId(1uL),
            name = name,
            color = color
        )

        // When
        val label = customLabel.toLabel()

        // Then
        assertEquals(LabelId(customLabel.id.value.toString()), label.labelId)
        assertEquals(color.value, label.color)
        assertEquals(name, label.name)
    }
}
