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

package ch.protonmail.android.mailmessage.data.repository

import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailmessage.data.mapper.toMessage
import ch.protonmail.android.mailmessage.data.mapper.toParticipant
import ch.protonmail.android.mailmessage.data.mapper.toRecipient
import ch.protonmail.android.mailmessage.domain.model.AttachmentCount
import ch.protonmail.android.mailmessage.domain.model.MessageId
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.user.domain.entity.AddressId
import org.junit.Test
import uniffi.proton_api_mail.MessageAddress
import uniffi.proton_mail_common.AvatarInformation
import uniffi.proton_mail_common.LocalAttachmentMetadata
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalInlineLabelInfo
import uniffi.proton_mail_common.LocalMessageMetadata

class MessageMapperTest {

    @Test
    fun `message address to participant should convert correctly`() {
        // Given
        val address = "address@test.com"
        val name = "Name"
        val isProton = true
        val displaySenderImage = false
        val isSimpleLogin = false
        val bimiSelector = "bimiSelector"

        val messageAddress = MessageAddress(
            address = address,
            name = name,
            isProton = isProton,
            displaySenderImage = displaySenderImage,
            isSimpleLogin = isSimpleLogin,
            bimiSelector = bimiSelector
        )

        // When
        val participant = messageAddress.toParticipant()

        // Then
        assertEquals(address, participant.address)
        assertEquals(name, participant.name)
        assertTrue(participant.isProton)
        assertEquals(bimiSelector, participant.bimiSelector)
    }


    @Test
    fun `message address to recipient should convert correctly`() {
        // Given
        val address = "recipient@test.com"
        val name = "Recipient Name"
        val isProton = true
        val displaySenderImage = false
        val isSimpleLogin = false
        val bimiSelector = "bimiSelector"

        val messageAddress = MessageAddress(
            address = address,
            name = name,
            isProton = isProton,
            displaySenderImage = displaySenderImage,
            isSimpleLogin = isSimpleLogin,
            bimiSelector = bimiSelector
        )

        // When
        val recipient = messageAddress.toRecipient()

        // Then
        assertEquals(address, recipient.address)
        assertEquals(name, recipient.name)
        assertTrue(recipient.isProton)
    }

    @Test
    fun `LocalMessageMetadata toMessage should convert correctly`() {
        // Given
        val id = 111uL
        val rid = "RemoteId"
        val conversationId: LocalConversationId = 99uL
        val time = 1625234000000uL
        val snoozeTime = 1625888000000uL
        val size = 1024uL
        val order = 1uL
        val labels = listOf(LocalInlineLabelInfo(id = 1u, name = "Test Label", color = "0xFF0000"))
        val subject = "Test Subject"
        val unread = true
        val sender = MessageAddress("sender@test.com", "Sender", true, false, false, "bimiSelector")
        val to = listOf(
            MessageAddress("to1@test.com", "To1", true, false, false, "bimiSelector"),
            MessageAddress("to2@test.com", "To2", false, false, false, "bimiSelector")
        )
        val cc = listOf(
            MessageAddress("cc1@test.com", "Cc1", true, false, false, "bimiSelector"),
            MessageAddress("cc2@test.com", "Cc2", false, false, false, "bimiSelector")
        )
        val bcc = listOf(
            MessageAddress("bcc1@test.com", "Bcc1", true, false, false, "bimiSelector"),
            MessageAddress("bcc2@test.com", "Bcc2", false, false, false, "bimiSelector")
        )
        val expirationTime = 1625235000000u
        val isReplied = false
        val isRepliedAll = false
        val isForwarded = false
        val addressId = "addressId"
        val externalId = "externalId"
        val numAttachments = 0u
        val flags = 1897uL
        val starred = false
        val attachments: List<LocalAttachmentMetadata>? = emptyList()
        val avatarInformation: AvatarInformation = AvatarInformation("A", "blue")

        val localMessageMetadata = LocalMessageMetadata(
            id = id,
            rid = rid,
            conversationId = conversationId,
            time = time,
            size = size,
            order = order,
            labels = labels,
            subject = subject,
            unread = unread,
            sender = sender,
            to = to,
            cc = cc,
            bcc = bcc,
            expirationTime = expirationTime,
            snoozeTime = snoozeTime,
            isReplied = isReplied,
            isRepliedAll = isRepliedAll,
            isForwarded = isForwarded,
            addressId = addressId,
            externalId = externalId,
            numAttachments = numAttachments,
            flags = flags,
            starred = starred,
            attachments = attachments,
            avatarInformation = avatarInformation
        )

        // When
        val message = localMessageMetadata.toMessage()

        // Then
        assertEquals(MessageId(id.toString()), message.messageId)
        assertEquals(ConversationId(conversationId.toString()), message.conversationId)
        assertEquals(time.toLong(), message.time)
        assertEquals(size.toLong(), message.size)
        assertEquals(order.toLong(), message.order)
        assertEquals(labels.map { LabelId(it.id.toString()) }, message.labelIds)
        assertEquals(subject, message.subject)
        assertTrue(message.unread)
        assertEquals(sender.toParticipant(), message.sender)
        assertEquals(to.map { it.toRecipient() }, message.toList)
        assertEquals(cc.map { it.toRecipient() }, message.ccList)
        assertEquals(bcc.map { it.toRecipient() }, message.bccList)
        assertEquals(expirationTime.toLong(), message.expirationTime)
        assertFalse(message.isReplied)
        assertFalse(message.isRepliedAll)
        assertFalse(message.isForwarded)
        assertEquals(AddressId(addressId), message.addressId)
        assertEquals(externalId, message.externalId)
        assertEquals(numAttachments.toInt(), message.numAttachments)
        assertEquals(flags.toLong(), message.flags)
        assertEquals(AttachmentCount(numAttachments.toInt()), message.attachmentCount)
    }
}

