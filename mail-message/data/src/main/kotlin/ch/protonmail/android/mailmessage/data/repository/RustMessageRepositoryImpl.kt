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
import arrow.core.Either
import arrow.core.flatMap
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSource
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMessage
import ch.protonmail.android.mailmessage.data.mapper.toRemoteMessageId
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageAttachment
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageWithBody
import ch.protonmail.android.mailmessage.domain.model.RefreshedMessageWithBody
import ch.protonmail.android.mailmessage.domain.model.RemoteMessageId
import ch.protonmail.android.mailmessage.domain.model.SenderImage
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailpagination.domain.model.PageKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

@Suppress("NotImplementedDeclaration", "TooManyFunctions")
class RustMessageRepositoryImpl @Inject constructor(
    private val rustMessageDataSource: RustMessageDataSource
) : MessageRepository {

    override suspend fun getSenderImage(
        userId: UserId,
        address: String,
        bimi: String?
    ): SenderImage? {
        return rustMessageDataSource.getSenderImage(userId, address, bimi)?.let { imageString ->
            SenderImage(File(imageString))
        }
    }

    override suspend fun getEmbeddedImage(
        userId: UserId,
        messageId: MessageId,
        contentId: String
    ): Either<DataError, EmbeddedImage> =
        rustMessageDataSource.getEmbeddedImage(userId, messageId.toLocalMessageId(), contentId)
            .map { localEmbeddedImage ->
                Timber.d("RustMessage: Loaded embedded image: $contentId; mime ${localEmbeddedImage.mime};")
                EmbeddedImage(localEmbeddedImage.data, localEmbeddedImage.mime)
            }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<Message> {
        return rustMessageDataSource.getMessages(userId, pageKey)
            .map { it.toMessage() }
    }

    @MissingRustApi
    // Observing is currently faked! This won't reflect changes to the message after the first emission
    override fun observeMessage(userId: UserId, messageId: MessageId): Flow<Either<DataError, Message>> =
        flow {
            val message = rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId())
                .map { it.toMessage() }

            emit(message)
        }

    @MissingRustApi
    // Observing is currently faked! This won't reflect changes to the message after the first emission
    override fun observeMessage(userId: UserId, remoteMessageId: RemoteMessageId): Flow<Either<DataError, Message>> =
        flow {
            val message = rustMessageDataSource.getMessage(userId, remoteMessageId.toRemoteMessageId())
                .map { it.toMessage() }

            emit(message)
        }

    override fun observeMessageAttachments(userId: UserId, messageId: MessageId): Flow<List<MessageAttachment>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessageWithBody(
        userId: UserId,
        messageId: MessageId,
        messageBodyTransformations: MessageBodyTransformations
    ): Either<DataError, MessageWithBody> {
        val localMessageId = messageId.toLocalMessageId()

        return rustMessageDataSource.getMessage(userId, localMessageId).flatMap { localMessage ->
            rustMessageDataSource.getMessageBody(userId, localMessageId, messageBodyTransformations)
                .map { MessageWithBody(localMessage.toMessage(), it) }
        }
    }

    override suspend fun getRefreshedMessageWithBody(userId: UserId, messageId: MessageId): RefreshedMessageWithBody? {
        TODO("Not yet implemented")
    }

    override suspend fun upsertMessageWithBody(userId: UserId, messageWithBody: MessageWithBody): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun moveTo(
        userId: UserId,
        messageId: MessageId,
        fromLabel: LabelId?,
        toLabel: LabelId
    ): Either<DataError.Local, Message> {
        TODO("Not yet implemented")
    }

    override suspend fun moveTo(
        userId: UserId,
        messageIds: List<MessageId>,
        toLabel: LabelId
    ): Either<DataError, Unit> = rustMessageDataSource.moveMessages(
        userId,
        messageIds.map { it.toLocalMessageId() },
        toLabel.toLocalLabelId()
    )

    override suspend fun markUnread(userId: UserId, messageIds: List<MessageId>): Either<DataError, Unit> =
        rustMessageDataSource.markUnread(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun markRead(userId: UserId, messageIds: List<MessageId>): Either<DataError, Unit> =
        rustMessageDataSource.markRead(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun starMessages(userId: UserId, messageIds: List<MessageId>): Either<DataError, Unit> =
        rustMessageDataSource.starMessages(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun unStarMessages(userId: UserId, messageIds: List<MessageId>): Either<DataError, Unit> =
        rustMessageDataSource.unStarMessages(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun isMessageRead(userId: UserId, messageId: MessageId): Either<DataError.Local, Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun labelAs(
        userId: UserId,
        messageIds: List<MessageId>,
        selectedLabels: List<LabelId>,
        partiallySelectedLabels: List<LabelId>,
        shouldArchive: Boolean
    ): Either<DataError, Unit> = rustMessageDataSource.labelMessages(
        userId,
        messageIds.map { it.toLocalMessageId() },
        selectedLabels.map { it.toLocalLabelId() },
        partiallySelectedLabels.map { it.toLocalLabelId() },
        shouldArchive
    )

    override suspend fun updateDraftRemoteIds(
        userId: UserId,
        localDraftId: MessageId,
        apiAssignedId: MessageId,
        conversationId: ConversationId
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessages(
        userId: UserId,
        messageIds: List<MessageId>,
        currentLabelId: LabelId
    ): Either<DataError, Unit> = rustMessageDataSource.deleteMessages(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun deleteMessages(userId: UserId, labelId: LabelId) {
        TODO("Not yet implemented")
    }

    // Mailbox requires this function to be implemented
    override fun observeClearLabelOperation(userId: UserId, labelId: LabelId): Flow<Boolean> = flowOf(false)

    override suspend fun reportPhishing(
        userId: UserId,
        decryptedMessageBody: DecryptedMessageBody
    ): Either<DataError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun markMessageAsLegitimate(userId: UserId, messageId: MessageId): Either<DataError, Unit> =
        rustMessageDataSource.markMessageAsLegitimate(userId, messageId.toLocalMessageId())

    override suspend fun unblockSender(userId: UserId, email: String): Either<DataError, Unit> =
        rustMessageDataSource.unblockSender(userId, email)
}
