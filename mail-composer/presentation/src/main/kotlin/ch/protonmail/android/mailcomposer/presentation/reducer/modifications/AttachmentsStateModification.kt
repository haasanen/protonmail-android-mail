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

package ch.protonmail.android.mailcomposer.presentation.reducer.modifications

import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.NO_ATTACHMENT_LIMIT

internal sealed interface AttachmentsStateModification : ComposerStateModification<ComposerState.Attachments> {
    data class ListUpdated(val list: List<AttachmentMetadataWithState>) : AttachmentsStateModification {

        override fun apply(state: ComposerState.Attachments): ComposerState.Attachments = state.copy(
            uiModel = AttachmentGroupUiModel(
                limit = NO_ATTACHMENT_LIMIT,
                attachments = list.map {
                    AttachmentMetadataUiModelMapper().toUiModel(
                        attachmentMetadata = it.attachmentMetadata,
                        isDeletable = true,
                        status = it.attachmentState
                    )
                }
            )
        )
    }
}
