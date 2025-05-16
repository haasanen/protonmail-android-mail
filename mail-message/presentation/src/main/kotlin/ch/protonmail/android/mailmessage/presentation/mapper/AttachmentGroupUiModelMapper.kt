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

import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentListExpandCollapseMode
import ch.protonmail.android.mailmessage.presentation.model.attachment.DEFAULT_ATTACHMENT_LIMIT
import javax.inject.Inject

class AttachmentGroupUiModelMapper @Inject constructor(
    private val attachmentMetadataUiModelMapper: AttachmentMetadataUiModelMapper
) {

    fun toUiModel(attachments: List<AttachmentMetadata>): AttachmentGroupUiModel {
        val attachmentsUiModel = attachments.map(attachmentMetadataUiModelMapper::toUiModel)
        return AttachmentGroupUiModel(
            attachments = attachmentsUiModel,
            expandCollapseMode = if (attachmentsUiModel.size < DEFAULT_ATTACHMENT_LIMIT) {
                AttachmentListExpandCollapseMode.NotApplicable
            } else {
                AttachmentListExpandCollapseMode.Collapsed
            }
        )
    }
}
