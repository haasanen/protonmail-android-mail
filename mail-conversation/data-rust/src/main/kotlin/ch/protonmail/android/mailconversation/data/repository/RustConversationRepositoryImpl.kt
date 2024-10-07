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

package ch.protonmail.android.mailconversation.data.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.local.RustConversationDataSource
import ch.protonmail.android.mailconversation.data.mapper.toConversation
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.data.mapper.toConversationMessagesWithMessageToOpen
import ch.protonmail.android.mailmessage.data.mapper.toLocalConversationId
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailpagination.domain.model.PageKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RustConversationRepositoryImpl @Inject constructor(
    private val rustConversationDataSource: RustConversationDataSource
) : ConversationRepository {

    override suspend fun getLocalConversations(userId: UserId, pageKey: PageKey): List<Conversation> {
        Timber.v("rust-conversation-repo: getConversations, pageKey: $pageKey")
        return rustConversationDataSource.getConversations(userId, pageKey).map { it.toConversation() }
    }

    @MissingRustApi
    // Awaiting for rust to add structured error handling
    override fun observeConversation(
        userId: UserId,
        id: ConversationId
    ): Flow<Either<DataError, Conversation>> = rustConversationDataSource
        .observeConversation(userId, id.toLocalConversationId())
        ?.map { it.toConversation().right() }
        ?: flowOf(DataError.Local.Unknown.left())


    override fun observeConversationMessages(
        userId: UserId,
        conversationId: ConversationId
    ): Flow<Either<DataError.Local, ConversationMessages>> {
        return rustConversationDataSource.observeConversationMessages(userId, conversationId.toLocalConversationId())
            .map { conversationMessages ->
                conversationMessages
                    .toConversationMessagesWithMessageToOpen()
                    ?.right()
                    ?: DataError.Local.NoDataCached.left()
            }
    }

    // It will be implemented later on
    override suspend fun addLabel(
        userId: UserId,
        conversationId: ConversationId,
        labelId: LabelId
    ): Either<DataError, Conversation> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun addLabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelId: LabelId
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun addLabels(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelIds: List<LabelId>
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun removeLabel(
        userId: UserId,
        conversationId: ConversationId,
        labelId: LabelId
    ): Either<DataError, Conversation> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun removeLabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelId: LabelId
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun removeLabels(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelIds: List<LabelId>
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    override suspend fun move(
        userId: UserId,
        conversationIds: List<ConversationId>,
        toLabelId: LabelId
    ): Either<DataError, List<Conversation>> {
        rustConversationDataSource.moveConversations(
            userId,
            conversationIds.map {
                it.toLocalConversationId()
            },
            toLabelId.toLocalLabelId()
        )

        return emptyList<Conversation>().right()
    }

    override suspend fun markUnread(
        userId: UserId,
        conversationIds: List<ConversationId>,
        contextLabelId: LabelId
    ): Either<DataError, Unit> {
        rustConversationDataSource.markUnread(userId, conversationIds.map { it.toLocalConversationId() })

        return Unit.right()
    }

    // It will be implemented later on
    override suspend fun markRead(
        userId: UserId,
        conversationIds: List<ConversationId>
    ): Either<DataError, List<Conversation>> {
        rustConversationDataSource.markRead(userId, conversationIds.map { it.toLocalConversationId() })

        return emptyList<Conversation>().right()
    }

    override suspend fun star(
        userId: UserId,
        conversationIds: List<ConversationId>
    ): Either<DataError, List<Conversation>> {
        rustConversationDataSource.starConversations(userId, conversationIds.map { it.toLocalConversationId() })

        return emptyList<Conversation>().right()
    }

    override suspend fun unStar(
        userId: UserId,
        conversationIds: List<ConversationId>
    ): Either<DataError, List<Conversation>> {

        rustConversationDataSource.unStarConversations(userId, conversationIds.map { it.toLocalConversationId() })

        return emptyList<Conversation>().right()
    }

    // It will be implemented later on
    override suspend fun relabel(
        userId: UserId,
        conversationId: ConversationId,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError, Conversation> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun relabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun deleteConversations(
        userId: UserId,
        conversationIds: List<ConversationId>,
        contextLabelId: LabelId
    ): Either<DataError, Unit> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun deleteConversations(userId: UserId, labelId: LabelId) {
    }

    // It will be implemented later on
    override fun observeClearLabelOperation(userId: UserId, labelId: LabelId): Flow<Boolean> = flowOf(false)
}
