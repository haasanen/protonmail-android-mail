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

package ch.protonmail.android.mailmessage.data.repository

import java.io.File
import android.net.Uri
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.local.RustAttachmentDataSource
import ch.protonmail.android.mailmessage.data.mapper.DecryptedAttachmentMapper
import ch.protonmail.android.mailmessage.data.mapper.toLocalAttachmentId
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.DecryptedAttachment
import ch.protonmail.android.mailmessage.domain.model.MessageAttachment
import ch.protonmail.android.mailmessage.domain.model.MessageAttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    private val rustAttachmentDataSource: RustAttachmentDataSource,
    private val decryptedAttachmentMapper: DecryptedAttachmentMapper
) : AttachmentRepository {

    override suspend fun getAttachment(
        userId: UserId,
        attachmentId: AttachmentId
    ): Either<DataError, DecryptedAttachment> {
        return rustAttachmentDataSource
            .getAttachment(userId, attachmentId.toLocalAttachmentId())
            .flatMap { decryptedAttachmentMapper.toDomainModel(it) }
    }

    override suspend fun getAttachmentFromRemote(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Either<DataError, ByteArray> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun saveMimeAttachmentToPublicStorage(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Either<DataError, Uri> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun getEmbeddedImage(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Either<DataError, ByteArray> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun getDownloadingAttachmentsForMessages(
        userId: UserId,
        messageIds: List<MessageId>
    ): List<MessageAttachmentMetadata> {
        Timber.w("Not implemented")
        return emptyList()
    }

    override suspend fun observeAttachmentMetadata(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Flow<MessageAttachmentMetadata?> {
        Timber.w("Not implemented")
        return flowOf()
    }

    override suspend fun saveAttachment(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId,
        uri: Uri
    ): Either<DataError, Unit> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun saveAttachmentToFile(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId,
        content: ByteArray
    ): Either<DataError, File> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun saveMimeAttachment(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId,
        content: ByteArray,
        attachment: MessageAttachment
    ): Either<DataError, Unit> {

        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun getFileSizeFromUri(uri: Uri): Either<DataError, Long> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun readFileFromStorage(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Either<DataError, File> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun getAttachmentInfo(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ): Either<DataError, MessageAttachment> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun updateMessageAttachment(
        userId: UserId,
        messageId: MessageId,
        localAttachmentId: AttachmentId,
        attachment: MessageAttachment
    ): Either<DataError, Unit> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }

    override suspend fun copyMimeAttachmentsToMessage(
        userId: UserId,
        sourceMessageId: MessageId,
        targetMessageId: MessageId,
        attachmentIds: List<AttachmentId>
    ): Either<DataError.Local, Unit> {
        Timber.w("Not implemented")
        return DataError.Local.Unknown.left()
    }
}
