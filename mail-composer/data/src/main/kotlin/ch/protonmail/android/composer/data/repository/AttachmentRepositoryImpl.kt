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

package ch.protonmail.android.composer.data.repository

import android.net.Uri
import arrow.core.Either
import ch.protonmail.android.composer.data.local.RustAttachmentDataSource
import ch.protonmail.android.mailattachments.domain.model.AttachmentError
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    private val rustAttachmentDataSource: RustAttachmentDataSource
) : AttachmentRepository {

    override suspend fun observeAttachments(): Flow<Either<DataError, List<AttachmentMetadataWithState>>> =
        rustAttachmentDataSource.observeAttachments()


    override suspend fun deleteAttachment(attachmentId: AttachmentId): Either<DataError, Unit> =
        rustAttachmentDataSource.removeAttachment(attachmentId)

    override suspend fun deleteInlineAttachment(contentId: String): Either<DataError, Unit> =
        rustAttachmentDataSource.removeInlineAttachment(contentId).mapLeft { error ->
            when (error) {
                AttachmentError.AttachmentTooLarge,
                AttachmentError.EncryptionError,
                AttachmentError.TooManyAttachments,
                AttachmentError.InvalidState -> {
                    // All these errors are exposed by the rust lib but do not make sense for observing
                    Timber.w("Attachment repo got an unexpected error deleting attachments: $error")
                    DataError.Local.Unknown
                }
                AttachmentError.InvalidDraftMessage -> DataError.Local.Unknown
                is AttachmentError.Other -> error.error
            }
        }

    override suspend fun addAttachment(fileUri: Uri): Either<AttachmentError, Unit> =
        rustAttachmentDataSource.addAttachment(fileUri)

    override suspend fun addInlineAttachment(fileUri: Uri): Either<AttachmentError, String> =
        rustAttachmentDataSource.addInlineAttachment(fileUri)

}
