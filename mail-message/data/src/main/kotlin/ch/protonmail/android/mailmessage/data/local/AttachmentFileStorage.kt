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

package ch.protonmail.android.mailmessage.data.local

import java.io.File
import java.io.InputStream
import java.util.UUID
import android.net.Uri
import ch.protonmail.android.mailcommon.data.file.FileInformation
import ch.protonmail.android.mailcommon.data.file.InternalFileStorage
import ch.protonmail.android.mailcommon.data.file.UriHelper
import javax.inject.Inject

class AttachmentFileStorage @Inject constructor(
    private val uriHelper: UriHelper,
    private val internalFileStorage: InternalFileStorage
) {

    suspend fun saveAttachment(attachmentFolder: String, uri: Uri): FileInformation? {
        return uriHelper.readFromUri(uri)?.let {

            // Random id to avoid conflicts on file name.
            // File display name resolved from uri and returned in the FileInformation
            val fileId = UUID.randomUUID().toString()

            saveAttachmentAsStream(attachmentFolder, fileId, it)?.let { file ->
                uriHelper.resolveFileInformation(uri, file)
            }
        }
    }

    private suspend fun saveAttachmentAsStream(
        attachmentFolder: String,
        fileId: String,
        inputStream: InputStream
    ): File? {
        return internalFileStorage.writeFileAsStream(
            InternalFileStorage.Folder.UploadFolder(attachmentFolder),
            InternalFileStorage.FileIdentifier(fileId),
            inputStream
        )
    }
}
