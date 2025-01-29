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

package ch.protonmail.android.mailmessage.data.mapper

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import ch.protonmail.android.mailcommon.datarust.mapper.LocalDecryptedAttachment
import ch.protonmail.android.mailmessage.domain.model.DecryptedAttachment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import timber.log.Timber
import javax.inject.Inject

class DecryptedAttachmentMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun toDomainModel(localDecryptedAttachment: LocalDecryptedAttachment): Either<DataError, DecryptedAttachment> {
        val file = File(localDecryptedAttachment.dataPath)

        if (!file.exists()) {
            Timber.e("Attachment file does not exist at path: ${file.path}")
            return DataError.Local.FailedToReadFile.left()
        }

        return try {
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            DecryptedAttachment(
                metadata = localDecryptedAttachment.attachmentMetadata.toAttachmentMetadata(),
                fileUri = fileUri
            ).right()

        } catch (e: IllegalArgumentException) {
            Timber.e("File is outside the paths supported by the provider: $e")
            DataError.Local.FailedToReadFile.left()
        }
    }
}
