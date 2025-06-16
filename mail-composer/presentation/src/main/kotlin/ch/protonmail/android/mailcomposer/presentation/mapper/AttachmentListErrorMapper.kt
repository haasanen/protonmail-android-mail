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

package ch.protonmail.android.mailcomposer.presentation.mapper

import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailattachments.domain.model.AttachmentState
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddErrorWithList

object AttachmentListErrorMapper {

    fun toAttachmentAddErrorWithList(attachments: List<AttachmentMetadataWithState>): AttachmentAddErrorWithList? {
        val itemsWithError: List<Pair<AttachmentMetadataWithState, DataError>> = attachments.mapNotNull { item ->
            val errorState = item.attachmentState as? AttachmentState.Error
            errorState?.reason?.let { reason ->
                item to reason
            }
        }

        if (itemsWithError.isEmpty()) {
            return null
        }

        val tooManyAttachmentItems = itemsWithError.filter {
            it.second is DataError.Local.AttachmentError.TooManyAttachments
        }

        val attachmentTooLargeItems = itemsWithError.filter {
            it.second is DataError.Local.AttachmentError.AttachmentTooLarge
        }

        val invalidDraftMessageItems = itemsWithError.filter {
            it.second is DataError.Local.AttachmentError.InvalidDraftMessage
        }

        val encryptionErrorItems = itemsWithError.filter {
            it.second is DataError.Local.AttachmentError.EncryptionError
        }

        return if (tooManyAttachmentItems.isNotEmpty()) {
            AttachmentAddErrorWithList(
                AttachmentAddError.TooManyAttachments,
                tooManyAttachmentItems.map { it.first }
            )
        } else if (attachmentTooLargeItems.isNotEmpty()) {
            AttachmentAddErrorWithList(
                AttachmentAddError.AttachmentTooLarge,
                attachmentTooLargeItems.map { it.first }
            )
        } else if (invalidDraftMessageItems.isNotEmpty()) {
            AttachmentAddErrorWithList(
                AttachmentAddError.InvalidDraftMessage,
                invalidDraftMessageItems.map { it.first }
            )
        } else if (encryptionErrorItems.isNotEmpty()) {
            AttachmentAddErrorWithList(
                AttachmentAddError.EncryptionError,
                encryptionErrorItems.map { it.first }
            )
        } else {
            AttachmentAddErrorWithList(
                AttachmentAddError.Unknown,
                itemsWithError.map { it.first }
            )
        }
    }
}
