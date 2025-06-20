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

package ch.protonmail.android.mailcomposer.presentation.usecase

import android.content.Context
import android.net.Uri
import arrow.core.Either
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.domain.usecase.AddInlineAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.AddStandardAttachment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AddAttachment @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val addStandardAttachment: AddStandardAttachment,
    private val addInlineAttachment: AddInlineAttachment
) {

    suspend operator fun invoke(fileUri: Uri): Either<AttachmentAddError, AddAttachmentResult> {
        val isNotImageMimeType = fileUri.mimeType() !in imageMimeTypes()

        if (isNotImageMimeType) {
            return addStandardAttachment(fileUri)
                .map { AddAttachmentResult.StandardAttachmentAdded }
        }

        return addInlineAttachment(fileUri)
            .map { AddAttachmentResult.InlineAttachmentAdded(it) }
    }

    suspend fun forcingStandardDisposition(
        fileUri: Uri
    ): Either<AttachmentAddError, AddAttachmentResult.StandardAttachmentAdded> {
        return addStandardAttachment(fileUri)
            .map { AddAttachmentResult.StandardAttachmentAdded }
    }

    private fun imageMimeTypes() = listOf(
        "image/jpg",
        "image/webp",
        "image/jpeg",
        "image/gif",
        "image/apng",
        "image/png"
    )

    private fun Uri.mimeType() = applicationContext.contentResolver.getType(this)

    sealed interface AddAttachmentResult {
        data object StandardAttachmentAdded : AddAttachmentResult
        data class InlineAttachmentAdded(val cid: String) : AddAttachmentResult
    }
}
