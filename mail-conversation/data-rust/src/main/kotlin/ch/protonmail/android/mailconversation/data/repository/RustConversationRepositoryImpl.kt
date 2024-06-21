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
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.mailconversation.domain.entity.ConversationWithContext
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.mailpagination.domain.model.PageKey
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("NotImplementedDeclaration", "TooManyFunctions")
class RustConversationRepositoryImpl @Inject constructor() : ConversationRepository {

    override suspend fun getLocalConversations(userId: UserId, pageKey: PageKey): List<ConversationWithContext> {
        TODO("Not yet implemented")
    }

    override suspend fun isLocalPageValid(
        userId: UserId,
        pageKey: PageKey,
        items: List<ConversationWithContext>
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getRemoteConversations(
        userId: UserId,
        pageKey: PageKey
    ): Either<DataError.Remote, List<ConversationWithContext>> {
        TODO("Not yet implemented")
    }

    override suspend fun markAsStale(userId: UserId, labelId: LabelId) {
        TODO("Not yet implemented")
    }

    override fun observeConversation(
        userId: UserId,
        id: ConversationId,
        refreshData: Boolean
    ): Flow<Either<DataError, Conversation>> {
        TODO("Not yet implemented")
    }

    override fun observeCachedConversations(userId: UserId, ids: List<ConversationId>): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun addLabel(
        userId: UserId,
        conversationId: ConversationId,
        labelId: LabelId
    ): Either<DataError, Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun addLabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelId: LabelId
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun addLabels(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelIds: List<LabelId>
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun removeLabel(
        userId: UserId,
        conversationId: ConversationId,
        labelId: LabelId
    ): Either<DataError, Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun removeLabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelId: LabelId
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun removeLabels(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelIds: List<LabelId>
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun move(
        userId: UserId,
        conversationIds: List<ConversationId>,
        allLabelIds: List<LabelId>,
        fromLabelIds: List<LabelId>,
        toLabelId: LabelId
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun markUnread(
        userId: UserId,
        conversationId: ConversationId,
        contextLabelId: LabelId
    ): Either<DataError, Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun markUnread(
        userId: UserId,
        conversationIds: List<ConversationId>,
        contextLabelId: LabelId
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun markRead(userId: UserId, conversationId: ConversationId): Either<DataError, Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun markRead(
        userId: UserId,
        conversationIds: List<ConversationId>
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun isCachedConversationRead(
        userId: UserId,
        conversationId: ConversationId
    ): Either<DataError, Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun relabel(
        userId: UserId,
        conversationId: ConversationId,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError, Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun relabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError, List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteConversations(
        userId: UserId,
        conversationIds: List<ConversationId>,
        contextLabelId: LabelId
    ): Either<DataError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteConversations(userId: UserId, labelId: LabelId) {
        TODO("Not yet implemented")
    }

    override fun observeClearLabelOperation(userId: UserId, labelId: LabelId): Flow<Boolean> {
        TODO("Not yet implemented")
    }

}
