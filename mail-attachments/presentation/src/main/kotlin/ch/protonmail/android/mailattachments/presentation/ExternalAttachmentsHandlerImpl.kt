/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailattachments.presentation

import java.io.IOException
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailattachments.presentation.ui.SaveAttachmentInput
import ch.protonmail.android.mailattachments.presentation.usecase.GenerateUniqueFileName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExternalAttachmentsHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val generateUniqueFileName: GenerateUniqueFileName
) : ExternalAttachmentsHandler {

    override suspend fun copyUriToDestination(
        sourceUri: Uri,
        destinationUri: Uri
    ): Either<ExternalAttachmentErrorResult, Unit> = either {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream, bufferSize = 8 * 1024)
                }
            } ?: raise(ExternalAttachmentErrorResult.UnableToCopy)
        }
    }

    override suspend fun saveFileToDownloadsFolder(
        attachmentInput: SaveAttachmentInput
    ): Either<ExternalAttachmentErrorResult, Unit> = either {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, generateUniqueFileName(attachmentInput.fileName))
                put(MediaStore.Downloads.MIME_TYPE, attachmentInput.mimeType)
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: raise(ExternalAttachmentErrorResult.UnableToCreateUri)

            try {
                copyUriToDestination(sourceUri = attachmentInput.uri, destinationUri = uri)
            } catch (_: IOException) {
                raise(ExternalAttachmentErrorResult.UnableToCopy)
            }
        }
    }
}
