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

package ch.protonmail.android.mailmailbox.presentation.mailbox.mapper

import ch.protonmail.android.mailattachments.domain.model.AttachmentDisposition
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailattachments.domain.model.AttachmentMimeType
import ch.protonmail.android.mailattachments.domain.model.MimeTypeCategory
import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailattachments.presentation.model.AttachmentIdUiModel
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailmessage.presentation.sample.AttachmentMetadataUiModelSamples
import org.junit.Assert.assertEquals
import org.junit.Test

class AttachmentMetadataUiModelMapperTest {

    private val mapper = AttachmentMetadataUiModelMapper()

    @Test
    fun `toUiModel maps id correctly`() {
        // Given
        val attachmentMetadata = AttachmentMetadata(
            attachmentId = AttachmentId("123"),
            name = "TestFile.pdf",
            size = 1024L,
            mimeType = AttachmentMimeType(
                mime = "application/pdf",
                category = MimeTypeCategory.Pdf
            ),
            disposition = AttachmentDisposition.Attachment,
            includeInPreview = true
        )

        // When
        val result = mapper.toUiModel(attachmentMetadata)

        // Then
        assertEquals(AttachmentIdUiModel("123"), result.id)
        assertEquals("TestFile.pdf", result.name)
    }

    @Test
    fun `toUiModel maps icon correctly for each MimeTypeCategory`() {
        // Given
        val mimeTypeToIconMapping = mapOf(
            MimeTypeCategory.Audio to R.drawable.ic_file_type_audio,
            MimeTypeCategory.Calendar to R.drawable.ic_file_type_calendar,
            MimeTypeCategory.Code to R.drawable.ic_file_type_code,
            MimeTypeCategory.Compressed to R.drawable.ic_file_type_zip,
            MimeTypeCategory.Default to R.drawable.ic_file_type_default,
            MimeTypeCategory.Excel to R.drawable.ic_file_type_excel,
            MimeTypeCategory.Font to R.drawable.ic_file_type_font,
            MimeTypeCategory.Image to R.drawable.ic_file_type_image,
            MimeTypeCategory.Key to R.drawable.ic_file_type_key,
            MimeTypeCategory.Keynote to R.drawable.ic_file_types_keynote,
            MimeTypeCategory.Numbers to R.drawable.ic_file_type_numbers,
            MimeTypeCategory.Pages to R.drawable.ic_file_type_pages,
            MimeTypeCategory.Pdf to R.drawable.ic_file_type_pdf,
            MimeTypeCategory.Powerpoint to R.drawable.ic_file_type_powerpoint,
            MimeTypeCategory.Text to R.drawable.ic_file_type_text,
            MimeTypeCategory.Video to R.drawable.ic_file_type_video,
            MimeTypeCategory.Word to R.drawable.ic_file_type_word,
            MimeTypeCategory.Unknown to R.drawable.ic_file_type_unknown
        )

        mimeTypeToIconMapping.forEach { (mimeTypeCategory, expectedIcon) ->
            // Given
            val attachmentMetadata = AttachmentMetadata(
                attachmentId = AttachmentId("123"),
                name = "TestFile",
                size = 1024L,
                mimeType = AttachmentMimeType(
                    mime = "mime",
                    category = mimeTypeCategory
                ),
                disposition = AttachmentDisposition.Attachment,
                includeInPreview = true
            )

            // When
            val result = mapper.toUiModel(attachmentMetadata)

            // Then
            assertEquals(expectedIcon, result.icon)
        }
    }

    @Test
    fun `toUiModel maps all AttachmentMetadataSamples to AttachmentMetadataUiModelSamples correctly`() {
        // Given
        val testCases = listOf(
            AttachmentMetadataSamples.Invoice to AttachmentMetadataUiModelSamples.Invoice,
            AttachmentMetadataSamples.Image to AttachmentMetadataUiModelSamples.Image,
            AttachmentMetadataSamples.Audio to AttachmentMetadataUiModelSamples.Audio,
            AttachmentMetadataSamples.Video to AttachmentMetadataUiModelSamples.Video,
            AttachmentMetadataSamples.Pdf to AttachmentMetadataUiModelSamples.Pdf,
            AttachmentMetadataSamples.Zip to AttachmentMetadataUiModelSamples.Zip,
            AttachmentMetadataSamples.InvoiceWithBinaryContentType to
                AttachmentMetadataUiModelSamples.InvoiceWithBinaryContentType,
            AttachmentMetadataSamples.PublicKey to AttachmentMetadataUiModelSamples.PublicKey,
            AttachmentMetadataSamples.Document to AttachmentMetadataUiModelSamples.Document,
            AttachmentMetadataSamples.DocumentWithMultipleDots to
                AttachmentMetadataUiModelSamples.DocumentWithMultipleDots,
            AttachmentMetadataSamples.DocumentWithReallyLongFileName to
                AttachmentMetadataUiModelSamples.DocumentWithReallyLongFileName,
            AttachmentMetadataSamples.Calendar to AttachmentMetadataUiModelSamples.Calendar,
            AttachmentMetadataSamples.EmbeddedImageAttachment to
                AttachmentMetadataUiModelSamples.EmbeddedImageAttachment,
            AttachmentMetadataSamples.SignedDocument to AttachmentMetadataUiModelSamples.SignedDocument,
            AttachmentMetadataSamples.InvalidEmbeddedImageAttachment to
                AttachmentMetadataUiModelSamples.InvalidEmbeddedImageAttachment,
            AttachmentMetadataSamples.EmbeddedOctetStreamAttachment to
                AttachmentMetadataUiModelSamples.EmbeddedOctetStreamAttachment
        )

        testCases.forEach { (metadata, uiModelSample) ->
            // When
            val result = mapper.toUiModel(metadata)

            // Then
            assertEquals(uiModelSample.id, result.id)
            assertEquals(uiModelSample.name, result.name)
            assertEquals(uiModelSample.icon, result.icon)
            assertEquals(uiModelSample.contentDescription, result.contentDescription)
            assertEquals(uiModelSample.size, result.size)
        }
    }
}
