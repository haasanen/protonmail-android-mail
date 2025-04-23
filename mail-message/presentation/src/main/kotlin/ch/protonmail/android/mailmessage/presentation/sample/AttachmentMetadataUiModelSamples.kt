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

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmessage.domain.model.AttachmentState
import ch.protonmail.android.mailmessage.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailmessage.presentation.R
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentIdUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentMetadataUiModel

object AttachmentMetadataUiModelSamples {

    val Invoice = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_INVOICE),
        name = TextUiModel("invoice.pdf"),
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 5678L
    )

    val Image = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_IMAGE),
        name = TextUiModel("profile_picture.png"),
        icon = R.drawable.ic_file_type_image,
        contentDescription = R.string.attachment_type_image,
        size = 2048L
    )

    val Audio = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_AUDIO),
        name = TextUiModel("song.mp3"),
        icon = R.drawable.ic_file_type_audio,
        contentDescription = R.string.attachment_type_audio,
        size = 5_242_880L
    )

    val Video = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_VIDEO),
        name = TextUiModel("vacation_video.mp4"),
        icon = R.drawable.ic_file_type_video,
        contentDescription = R.string.attachment_type_video,
        size = 10_485_760L
    )

    val Pdf = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_PDF),
        name = TextUiModel("ebook.pdf"),
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 302_976L
    )

    val Zip = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_ZIP),
        name = TextUiModel("archive.zip"),
        icon = R.drawable.ic_file_type_zip,
        contentDescription = R.string.attachment_type_archive,
        size = 10_240L
    )

    val InvoiceWithBinaryContentType = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_INVOICE_BINARY),
        name = TextUiModel("invoice.pdf"),
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 5678L
    )

    val PublicKey = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_PUBLIC_KEY),
        name = TextUiModel("publickey - example@protonmail.com - 0x61DD734E.asc"),
        icon = R.drawable.ic_file_type_key,
        contentDescription = R.string.attachment_type_key,
        size = 666L
    )

    val Document = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_DOCUMENT),
        name = TextUiModel("document.docx"),
        icon = R.drawable.ic_file_type_word,
        contentDescription = R.string.attachment_type_word,
        size = 1234L
    )

    val DocumentWithMultipleDots = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_DOCUMENT_MULTIPLE_DOTS),
        name = TextUiModel("complicated.document.docx"),
        icon = R.drawable.ic_file_type_word,
        contentDescription = R.string.attachment_type_word,
        size = 1234L
    )

    val DocumentWithReallyLongFileName = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_DOCUMENT_LONG_NAME),
        name = TextUiModel("document-with-really-long-and-unnecessary-file-name-that-should-be-truncated.pdf"),
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 1234L
    )

    val Calendar = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_CALENDAR),
        name = TextUiModel("invite.ics"),
        icon = R.drawable.ic_file_type_calendar,
        contentDescription = R.string.attachment_type_calendar,
        size = 1234
    )

    val EmbeddedImageAttachment = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_EMBEDDED_IMAGE),
        name = TextUiModel("embeddedImage.png"),
        icon = R.drawable.ic_file_type_image,
        contentDescription = R.string.attachment_type_image,
        size = 1234
    )

    val SignedDocument = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_SIGNED_DOCUMENT),
        name = TextUiModel("document_signed.pdf"),
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 1234L
    )

    val InvalidEmbeddedImageAttachment = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_INVALID_EMBEDDED_IMAGE),
        name = TextUiModel("embeddedImage.png"),
        icon = R.drawable.ic_file_type_pdf,
        contentDescription = R.string.attachment_type_pdf,
        size = 1234L
    )

    val EmbeddedOctetStreamAttachment = AttachmentMetadataUiModel(
        id = AttachmentIdUiModel(AttachmentMetadataSamples.Ids.ID_EMBEDDED_OCTET_STREAM),
        name = TextUiModel("embeddedOctet.png"),
        icon = R.drawable.ic_file_type_image,
        contentDescription = R.string.attachment_type_image,
        size = 1234L
    )

    val DeletableInvoice = Invoice.copy(deletable = true)

    val DeletableInvoiceUploaded = Invoice.copy(deletable = true, status = AttachmentState.Uploaded)
}

