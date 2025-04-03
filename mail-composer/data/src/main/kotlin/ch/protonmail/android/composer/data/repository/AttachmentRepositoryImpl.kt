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

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import ch.protonmail.android.composer.data.local.RustAttachmentDataSource
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.AttachmentRepository
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.MessageId
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

@MissingRustApi
class AttachmentRepositoryImpl @Inject constructor(
    private val rustAttachmentDataSource: RustAttachmentDataSource
) : AttachmentRepository {

    override suspend fun observeAttachments(): Flow<Either<DataError, List<AttachmentMetadata>>> =
        rustAttachmentDataSource.observeAttachments()

    override suspend fun deleteAttachment(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Either<DataError, Unit> = either {
        Timber.w("rust-attachment: missing implementation!")
    }

    override suspend fun createAttachment(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId,
        fileName: String,
        mimeType: String,
        content: ByteArray
    ): Either<DataError, Unit> = either {

        Timber.w("rust-attachment: missing implementation!")
        return DataError.Local.Unknown.left()
    }

}
