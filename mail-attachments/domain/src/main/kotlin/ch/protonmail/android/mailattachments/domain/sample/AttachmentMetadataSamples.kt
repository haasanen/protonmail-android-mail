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

package ch.protonmail.android.mailattachments.domain.sample

import ch.protonmail.android.mailattachments.domain.model.AttachmentDisposition
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailattachments.domain.model.AttachmentMimeType
import ch.protonmail.android.mailattachments.domain.model.MimeTypeCategory

object AttachmentMetadataSamples {

    val Invoice = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_INVOICE),
        mimeType = AttachmentMimeType(
            mime = "application/pdf",
            category = MimeTypeCategory.Pdf
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "invoice.pdf",
        size = 5678L,
        includeInPreview = true
    )

    val Image = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_IMAGE),
        mimeType = AttachmentMimeType(
            mime = "image/png",
            category = MimeTypeCategory.Image
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "profile_picture.png",
        size = 2048L,
        includeInPreview = true
    )

    val Audio = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_AUDIO),
        mimeType = AttachmentMimeType(
            mime = "audio/mpeg",
            category = MimeTypeCategory.Audio
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "song.mp3",
        size = 5_242_880L,
        includeInPreview = true
    )

    val Video = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_VIDEO),
        mimeType = AttachmentMimeType(
            mime = "video/mp4",
            category = MimeTypeCategory.Video
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "vacation_video.mp4",
        size = 10_485_760L,
        includeInPreview = true
    )

    val Pdf = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_PDF),
        mimeType = AttachmentMimeType(
            mime = "application/pdf",
            category = MimeTypeCategory.Pdf
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "ebook.pdf",
        size = 302_976L,
        includeInPreview = true
    )

    val Zip = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_ZIP),
        mimeType = AttachmentMimeType(
            mime = "application/zip",
            category = MimeTypeCategory.Compressed
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "archive.zip",
        size = 10_240L,
        includeInPreview = true
    )

    val InvoiceWithBinaryContentType = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_INVOICE_BINARY),
        mimeType = AttachmentMimeType(
            mime = "application/octet-stream",
            category = MimeTypeCategory.Pdf
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "invoice.pdf",
        size = 5678L,
        includeInPreview = true
    )

    val PublicKey = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_PUBLIC_KEY),
        mimeType = AttachmentMimeType(
            mime = "application/pgp-keys",
            category = MimeTypeCategory.Key
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "publickey - example@protonmail.com - 0x61DD734E.asc",
        size = 666L,
        includeInPreview = true
    )

    val Document = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_DOCUMENT),
        mimeType = AttachmentMimeType(
            mime = "application/doc",
            category = MimeTypeCategory.Word
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "document.docx",
        size = 1234L,
        includeInPreview = true
    )

    val DocumentWithMultipleDots = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_DOCUMENT_MULTIPLE_DOTS),
        mimeType = AttachmentMimeType(
            mime = "application/doc",
            category = MimeTypeCategory.Word
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "complicated.document.docx",
        size = 1234L,
        includeInPreview = true
    )

    val DocumentWithReallyLongFileName = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_DOCUMENT_LONG_NAME),
        mimeType = AttachmentMimeType(
            mime = "application/doc",
            category = MimeTypeCategory.Pdf
        ),
        disposition = AttachmentDisposition.Attachment,
        name = "document-with-really-long-and-unnecessary-file-name-that-should-be-truncated.pdf",
        size = 1234L,
        includeInPreview = true
    )

    val Calendar = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_CALENDAR),
        mimeType = AttachmentMimeType(
            mime = "text/calendar",
            category = MimeTypeCategory.Calendar
        ),
        name = "invite.ics",
        size = 1234,
        disposition = AttachmentDisposition.Attachment,
        includeInPreview = true
    )

    val EmbeddedImageAttachment = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_EMBEDDED_IMAGE),
        name = "embeddedImage.png",
        size = 1234,
        mimeType = AttachmentMimeType(
            mime = "text/calendar",
            category = MimeTypeCategory.Image
        ),
        disposition = AttachmentDisposition.Inline,
        includeInPreview = true
    )

    val SignedDocument = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_SIGNED_DOCUMENT),
        name = "document_signed.pdf",
        size = 1234,
        mimeType = AttachmentMimeType(
            mime = "application/doc",
            category = MimeTypeCategory.Pdf
        ),
        disposition = AttachmentDisposition.Attachment,
        includeInPreview = true
    )

    val InvalidEmbeddedImageAttachment = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_INVALID_EMBEDDED_IMAGE),
        name = "embeddedImage.png",
        size = 1234,
        mimeType = AttachmentMimeType(
            mime = "application/pdf",
            category = MimeTypeCategory.Pdf
        ),
        disposition = AttachmentDisposition.Inline,
        includeInPreview = true
    )

    val EmbeddedOctetStreamAttachment = AttachmentMetadata(
        attachmentId = AttachmentId(Ids.ID_EMBEDDED_OCTET_STREAM),
        name = "embeddedOctet.png",
        size = 1234,
        mimeType = AttachmentMimeType(
            mime = "application/octet-stream",
            category = MimeTypeCategory.Image
        ),
        disposition = AttachmentDisposition.Inline,
        includeInPreview = true
    )

    object Ids {
        const val ID_INVOICE = "6"
        const val ID_IMAGE = "1"
        const val ID_AUDIO = "2"
        const val ID_VIDEO = "3"
        const val ID_PDF = "4"
        const val ID_ZIP = "5"
        const val ID_INVOICE_BINARY = "7"
        const val ID_PUBLIC_KEY = "8"
        const val ID_DOCUMENT = "9"
        const val ID_DOCUMENT_MULTIPLE_DOTS = "10"
        const val ID_DOCUMENT_LONG_NAME = "11"
        const val ID_CALENDAR = "12"
        const val ID_EMBEDDED_IMAGE = "13"
        const val ID_SIGNED_DOCUMENT = "14"
        const val ID_INVALID_EMBEDDED_IMAGE = "15"
        const val ID_EMBEDDED_OCTET_STREAM = "16"
    }

}
