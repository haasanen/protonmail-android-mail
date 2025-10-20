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
import ch.protonmail.android.mailattachments.domain.model.AddAttachmentError
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailattachments.domain.model.ConvertAttachmentError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentDeleteError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    private val rustAttachmentDataSource: RustAttachmentDataSource
) : AttachmentRepository {

    override suspend fun observeAttachments(): Flow<Either<DataError, List<AttachmentMetadataWithState>>> =
        rustAttachmentDataSource.observeAttachments()


    override suspend fun deleteAttachment(attachmentId: AttachmentId): Either<DataError, Unit> =
        rustAttachmentDataSource.removeAttachment(attachmentId)

    override suspend fun deleteInlineAttachment(contentId: String): Either<AttachmentDeleteError, Unit> =
        rustAttachmentDataSource.removeInlineAttachment(contentId)

    override suspend fun addAttachment(fileUri: Uri): Either<AddAttachmentError, Unit> =
        rustAttachmentDataSource.addAttachment(fileUri)

    override suspend fun addInlineAttachment(fileUri: Uri): Either<AddAttachmentError, String> =
        rustAttachmentDataSource.addInlineAttachment(fileUri)

    override suspend fun convertToAttachment(cid: String): Either<ConvertAttachmentError, Unit> =
        rustAttachmentDataSource.convertToAttachment(cid)

}
