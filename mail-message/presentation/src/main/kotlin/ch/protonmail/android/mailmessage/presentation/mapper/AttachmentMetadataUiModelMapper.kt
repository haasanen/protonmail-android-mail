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

package ch.protonmail.android.mailmessage.presentation.mapper

import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentIdUiModel
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.AttachmentState
import ch.protonmail.android.mailmessage.domain.model.MimeTypeCategory
import ch.protonmail.android.mailmessage.domain.model.isCalendarAttachment
import ch.protonmail.android.mailmessage.presentation.R
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentMetadataUiModel
import javax.inject.Inject

class AttachmentMetadataUiModelMapper @Inject constructor() {

    fun toUiModel(
        attachmentMetadata: AttachmentMetadata,
        isDeletable: Boolean = false,
        status: AttachmentState? = null
    ): AttachmentMetadataUiModel {
        return AttachmentMetadataUiModel(
            id = AttachmentIdUiModel(attachmentMetadata.attachmentId.id),
            name = attachmentMetadata.name,
            icon = getIcon(attachmentMetadata.mimeType.category),
            size = attachmentMetadata.size,
            isCalendar = attachmentMetadata.isCalendarAttachment(),
            contentDescription = getContentDescription(attachmentMetadata.mimeType.category),
            deletable = isDeletable,
            status = status
        )
    }

    private fun getIcon(mimeTypeCategory: MimeTypeCategory): Int {
        return when (mimeTypeCategory) {
            MimeTypeCategory.Audio -> R.drawable.ic_file_type_audio
            MimeTypeCategory.Calendar -> R.drawable.ic_file_type_calendar
            MimeTypeCategory.Code -> R.drawable.ic_file_type_code
            MimeTypeCategory.Compressed -> R.drawable.ic_file_type_zip
            MimeTypeCategory.Default -> R.drawable.ic_file_type_default
            MimeTypeCategory.Excel -> R.drawable.ic_file_type_excel
            MimeTypeCategory.Font -> R.drawable.ic_file_type_font
            MimeTypeCategory.Image -> R.drawable.ic_file_type_image
            MimeTypeCategory.Key -> R.drawable.ic_file_type_key
            MimeTypeCategory.Keynote -> R.drawable.ic_file_types_keynote
            MimeTypeCategory.Numbers -> R.drawable.ic_file_type_numbers
            MimeTypeCategory.Pages -> R.drawable.ic_file_type_pages
            MimeTypeCategory.Pdf -> R.drawable.ic_file_type_pdf
            MimeTypeCategory.Powerpoint -> R.drawable.ic_file_type_powerpoint
            MimeTypeCategory.Text -> R.drawable.ic_file_type_text
            MimeTypeCategory.Video -> R.drawable.ic_file_type_video
            MimeTypeCategory.Word -> R.drawable.ic_file_type_word
            MimeTypeCategory.Unknown -> R.drawable.ic_file_type_unknown
        }
    }

    private fun getContentDescription(mimeTypeCategory: MimeTypeCategory): Int {
        return when (mimeTypeCategory) {
            MimeTypeCategory.Audio -> R.string.attachment_type_audio
            MimeTypeCategory.Calendar -> R.string.attachment_type_calendar
            MimeTypeCategory.Code -> R.string.attachment_type_code
            MimeTypeCategory.Compressed -> R.string.attachment_type_archive
            MimeTypeCategory.Default -> R.string.attachment_type_unknown
            MimeTypeCategory.Excel -> R.string.attachment_type_spreadsheet
            MimeTypeCategory.Font -> R.string.attachment_type_font
            MimeTypeCategory.Image -> R.string.attachment_type_image
            MimeTypeCategory.Key -> R.string.attachment_type_key
            MimeTypeCategory.Keynote -> R.string.attachment_type_keynote
            MimeTypeCategory.Numbers -> R.string.attachment_type_numbers
            MimeTypeCategory.Pages -> R.string.attachment_type_pages
            MimeTypeCategory.Pdf -> R.string.attachment_type_pdf
            MimeTypeCategory.Powerpoint -> R.string.attachment_type_presentation
            MimeTypeCategory.Text -> R.string.attachment_type_text
            MimeTypeCategory.Video -> R.string.attachment_type_video
            MimeTypeCategory.Word -> R.string.attachment_type_word
            MimeTypeCategory.Unknown -> R.string.attachment_type_unknown
        }
    }


}
