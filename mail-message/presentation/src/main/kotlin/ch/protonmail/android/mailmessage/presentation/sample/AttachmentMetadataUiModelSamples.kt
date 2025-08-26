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

package ch.protonmail.android.mailmessage.presentation.sample

import ch.protonmail.android.mailattachments.domain.model.AttachmentState
import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailattachments.presentation.model.AttachmentIdUiModel
import ch.protonmail.android.mailattachments.presentation.model.AttachmentMetadataUiModel
import ch.protonmail.android.mailmessage.presentation.R

object AttachmentMetadataUiModelSamples {

    val Invoice = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_INVOICE),
        name = "invoice.pdf",
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 5678L,
        includeInPreview = true
    )

    val Image = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_IMAGE),
        name = "profile_picture.png",
        icon = R.drawable.ic_file_type_image,
        contentDescription = R.string.attachment_type_image,
        size = 2048L,
        includeInPreview = true
    )

    val Audio = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_AUDIO),
        name = "song.mp3",
        icon = R.drawable.ic_file_type_audio,
        contentDescription = R.string.attachment_type_audio,
        size = 5_242_880L,
        includeInPreview = true
    )

    val Video = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_VIDEO),
        name = "vacation_video.mp4",
        icon = R.drawable.ic_file_type_video,
        contentDescription = R.string.attachment_type_video,
        size = 10_485_760L,
        includeInPreview = true
    )

    val Pdf = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_PDF),
        name = "ebook.pdf",
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 302_976L,
        includeInPreview = true
    )

    val Zip = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_ZIP),
        name = "archive.zip",
        icon = R.drawable.ic_file_type_zip,
        contentDescription = R.string.attachment_type_archive,
        size = 10_240L,
        includeInPreview = true
    )

    val InvoiceWithBinaryContentType = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_INVOICE_BINARY),
        name = "invoice.pdf",
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 5678L,
        includeInPreview = true
    )

    val PublicKey = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_PUBLIC_KEY),
        name = "publickey - example@protonmail.com - 0x61DD734E.asc",
        icon = R.drawable.ic_file_type_key,
        contentDescription = R.string.attachment_type_key,
        size = 666L,
        includeInPreview = true
    )

    val Document = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_DOCUMENT),
        name = "document.docx",
        icon = R.drawable.ic_file_type_word,
        contentDescription = R.string.attachment_type_word,
        size = 1234L,
        includeInPreview = true
    )

    val DocumentWithMultipleDots = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_DOCUMENT_MULTIPLE_DOTS),
        name = "complicated.document.docx",
        icon = R.drawable.ic_file_type_word,
        contentDescription = R.string.attachment_type_word,
        size = 1234L,
        includeInPreview = true
    )

    val DocumentWithReallyLongFileName = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_DOCUMENT_LONG_NAME),
        name = "document-with-really-long-and-unnecessary-file-name-that-should-be-truncated.pdf",
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 1234L,
        includeInPreview = true
    )

    val Calendar = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_CALENDAR),
        name = "invite.ics",
        icon = R.drawable.ic_file_type_calendar,
        contentDescription = R.string.attachment_type_calendar,
        size = 1234,
        includeInPreview = true
    )

    val EmbeddedImageAttachment = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_EMBEDDED_IMAGE),
        name = "embeddedImage.png",
        icon = R.drawable.ic_file_type_image,
        contentDescription = R.string.attachment_type_image,
        size = 1234,
        includeInPreview = true
    )

    val SignedDocument = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_SIGNED_DOCUMENT),
        name = "document_signed.pdf",
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 1234L,
        includeInPreview = true
    )

    val InvalidEmbeddedImageAttachment = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_INVALID_EMBEDDED_IMAGE),
        name = "embeddedImage.png",
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 1234L,
        includeInPreview = true
    )

    val EmbeddedOctetStreamAttachment = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_EMBEDDED_OCTET_STREAM),
        name = "embeddedOctet.png",
        icon = R.drawable.ic_file_type_image,
        contentDescription = R.string.attachment_type_image,
        size = 1234L,
        includeInPreview = true
    )

    val DeletableInvoice = Invoice.copy(deletable = true)

    val DeletableInvoiceUploaded = Invoice.copy(deletable = true, status = AttachmentState.Uploaded)
}

