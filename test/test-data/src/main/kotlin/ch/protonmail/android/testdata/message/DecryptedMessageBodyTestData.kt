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

package ch.protonmail.android.testdata.message

import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBanner
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample

object DecryptedMessageBodyTestData {

    const val DECRYPTED_MESSAGE_BODY = "This is a decrypted message body"

    val messageBodyWithAttachment = buildDecryptedMessageBody(
        attachments = listOf(
            AttachmentMetadataSamples.Invoice
        )
    )

    val messageBodyWithEmbeddedImage = buildDecryptedMessageBody(
        attachments = listOf(
            AttachmentMetadataSamples.EmbeddedImageAttachment
        )
    )

    val messageBodyWithEmbeddedOctetStream = buildDecryptedMessageBody(
        attachments = listOf(
            AttachmentMetadataSamples.EmbeddedOctetStreamAttachment
        )
    )

    val messageBodyWithInvalidEmbeddedAttachment = buildDecryptedMessageBody(
        attachments = listOf(
            AttachmentMetadataSamples.InvalidEmbeddedImageAttachment
        )
    )

    val MessageWithAttachments = buildDecryptedMessageBody(
        messageId = MessageIdSample.MessageWithAttachments,
        attachments = listOf(
            AttachmentMetadataSamples.Document,
            AttachmentMetadataSamples.DocumentWithReallyLongFileName,
            AttachmentMetadataSamples.EmbeddedImageAttachment
        )
    )

    val MessageWithSignedAttachments = buildDecryptedMessageBody(
        messageId = MessageIdSample.MessageWithAttachments,
        attachments = listOf(
            AttachmentMetadataSamples.SignedDocument
        )
    )

    val htmlInvoice = buildDecryptedMessageBody(
        messageId = MessageIdSample.HtmlInvoice,
        value = "<div>Decrypted invoice message HTML body</div>",
        mimeType = MimeType.Html,
        attachments = emptyList()
    )

    val PlainTextDecryptedBody = buildDecryptedMessageBody(
        messageId = MessageIdSample.PlainTextMessage,
        value = "Plain text message",
        mimeType = MimeType.PlainText,
        attachments = emptyList()
    )

    val PgpMimeMessage = buildDecryptedMessageBody(
        messageId = MessageIdSample.PgpMimeMessage,
        attachments = listOf(
            AttachmentMetadataSamples.EmbeddedImageAttachment,
            AttachmentMetadataSamples.Image
        )
    )

    fun buildDecryptedMessageBody(
        messageId: MessageId = MessageIdSample.build(),
        value: String = DECRYPTED_MESSAGE_BODY,
        mimeType: MimeType = MimeType.Html,
        hasQuotedText: Boolean = false,
        isUnread: Boolean = false,
        banners: List<MessageBanner> = emptyList(),
        attachments: List<AttachmentMetadata> = emptyList()
    ) = DecryptedMessageBody(
        messageId = messageId,
        value = value,
        isUnread = isUnread,
        hasQuotedText = hasQuotedText,
        mimeType = mimeType,
        banners = banners,
        attachments = attachments
    )
}
