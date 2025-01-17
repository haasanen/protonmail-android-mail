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

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.AttachmentIdUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.AttachmentMetadataUiModel
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.MimeTypeCategory
import javax.inject.Inject

class AttachmentMetadataUiModelMapper @Inject constructor() {

    fun toUiModel(attachmentMetadata: AttachmentMetadata): AttachmentMetadataUiModel {
        return AttachmentMetadataUiModel(
            id = AttachmentIdUiModel(attachmentMetadata.id.id),
            name = TextUiModel(attachmentMetadata.name),
            icon = getIcon(attachmentMetadata.mimeTypeCategory)
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

}
