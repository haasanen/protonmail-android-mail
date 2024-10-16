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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSource
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMessage
import ch.protonmail.android.mailmessage.data.mapper.toMessageBody
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageAttachment
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageWithBody
import ch.protonmail.android.mailmessage.domain.model.RefreshedMessageWithBody
import ch.protonmail.android.mailmessage.domain.model.SenderImage
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailpagination.domain.model.PageKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import uniffi.proton_mail_uniffi.BlockQuote
import uniffi.proton_mail_uniffi.RemoteContent
import uniffi.proton_mail_uniffi.TransformOpts
import javax.inject.Inject

@Suppress("NotImplementedDeclaration", "TooManyFunctions")
class RustMessageRepositoryImpl @Inject constructor(
    private val rustMessageDataSource: RustMessageDataSource,
    private val selectedMailLabelId: SelectedMailLabelId
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

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<Message> {
        return rustMessageDataSource.getMessages(userId, pageKey)
            .map { it.toMessage() }
    }

    @MissingRustApi
    // Observing is currently faked! This won't reflect changes to the message after the first emission
    override fun observeMessage(userId: UserId, messageId: MessageId): Flow<Either<DataError.Local, Message>> =
        flow {
            val message = rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId())?.toMessage()

            emit(message?.right() ?: DataError.Local.NoDataCached.left())
        }

    override fun observeMessageWithBody(
        userId: UserId,
        messageId: MessageId
    ): Flow<Either<DataError, MessageWithBody>> = flow {
        emit(getMessageWithBody(userId, messageId))
    }

    override fun observeMessageAttachments(userId: UserId, messageId: MessageId): Flow<List<MessageAttachment>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessageWithBody(userId: UserId, messageId: MessageId): Either<DataError, MessageWithBody> =
        getLocalMessageWithBody(userId, messageId)?.right() ?: DataError.Local.NoDataCached.left()

    override suspend fun getLocalMessageWithBody(userId: UserId, messageId: MessageId): MessageWithBody? {
        val localMessageId = messageId.toLocalMessageId()
        val message = rustMessageDataSource.getMessage(userId, localMessageId)?.toMessage()
        val currentLabelId = selectedMailLabelId.flow.value.labelId.toLocalLabelId()
        val decryptedBody = rustMessageDataSource.getMessageBody(userId, localMessageId, currentLabelId)

        return if (message != null && decryptedBody != null) {
            val bodyOutput = decryptedBody.body(TransformOpts(BlockQuote.STRIP, RemoteContent.DEFAULT))
            MessageWithBody(message, bodyOutput.toMessageBody(messageId, decryptedBody.mimeType()))
        } else {
            null
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
    ): Either<DataError.Local, Unit> = rustMessageDataSource.moveMessages(
        userId,
        messageIds.map { it.toLocalMessageId() },
        toLabel.toLocalLabelId()
    )

    override suspend fun markUnread(userId: UserId, messageIds: List<MessageId>): Either<DataError.Local, Unit> =
        rustMessageDataSource.markUnread(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun markRead(userId: UserId, messageIds: List<MessageId>): Either<DataError.Local, Unit> =
        rustMessageDataSource.markRead(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun starMessages(userId: UserId, messageIds: List<MessageId>): Either<DataError.Local, Unit> =
        rustMessageDataSource.starMessages(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun unStarMessages(userId: UserId, messageIds: List<MessageId>): Either<DataError.Local, Unit> =
        rustMessageDataSource.unStarMessages(userId, messageIds.map { it.toLocalMessageId() })

    override suspend fun isMessageRead(userId: UserId, messageId: MessageId): Either<DataError.Local, Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun relabel(
        userId: UserId,
        messageId: MessageId,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError.Local, Message> {
        TODO("Not yet implemented")
    }

    override suspend fun relabel(
        userId: UserId,
        messageIds: List<MessageId>,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError.Local, List<Message>> {
        TODO("Not yet implemented")
    }

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
}
