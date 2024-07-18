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
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.local.RustConversationDataSource
import ch.protonmail.android.mailconversation.data.mapper.toConversation
import ch.protonmail.android.mailconversation.data.mapper.toConversationWithContext
import ch.protonmail.android.mailconversation.data.mapper.toLocalConversationId
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.mailconversation.domain.entity.ConversationWithContext
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.data.usecase.FindLocalLabelId
import ch.protonmail.android.mailpagination.domain.model.PageKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("NotImplementedDeclaration", "TooManyFunctions")
class RustConversationRepositoryImpl @Inject constructor(
    private val rustConversationDataSource: RustConversationDataSource,
    private val findLocalLabelId: FindLocalLabelId
) : ConversationRepository {

    override suspend fun getLocalConversations(userId: UserId, pageKey: PageKey): List<ConversationWithContext> {
        val rustLocalLabelId =
            if (pageKey.filter.isSystemFolder) findLocalLabelId(userId, pageKey.filter.labelId)
            else pageKey.filter.labelId.toLocalLabelId()

        Timber.d("rust-conversation: getConversations, pageKey: $pageKey rustLocalLabelId: $rustLocalLabelId")

        return rustLocalLabelId?.let { labelId ->
            rustConversationDataSource.getConversations(labelId).map {
                it.toConversationWithContext(pageKey.filter.labelId)
            }
        } // after pagination + dynamic label implementation, this should be removed
            // and error handling should be implemented
            ?: emptyList()
    }

    // This function should be handled after rust-pagination is released
    override suspend fun isLocalPageValid(
        userId: UserId,
        pageKey: PageKey,
        items: List<ConversationWithContext>
    ): Boolean = true

    // It will be implemented later on
    override suspend fun getRemoteConversations(
        userId: UserId,
        pageKey: PageKey
    ): Either<DataError.Remote, List<ConversationWithContext>> = DataError.Remote.Unknown.left()

    override suspend fun markAsStale(userId: UserId, labelId: LabelId) {
        // It will be implemented later on
    }

    override fun observeConversation(
        userId: UserId,
        id: ConversationId,
        refreshData: Boolean
    ): Flow<Either<DataError, Conversation>> = flow {
        emit(
            rustConversationDataSource.getConversation(id.toLocalConversationId())
                ?.toConversation()
                ?.right()
                ?: DataError.Local.NoDataCached.left()
        )
    }

    override fun observeCachedConversations(userId: UserId, ids: List<ConversationId>): Flow<List<Conversation>> {
        return rustConversationDataSource.observeConversations(ids.map { it.toLocalConversationId() })
            .map { localConversations ->
                localConversations.map { it.toConversation() }
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

    // It will be implemented later on
    override suspend fun move(
        userId: UserId,
        conversationIds: List<ConversationId>,
        allLabelIds: List<LabelId>,
        fromLabelIds: List<LabelId>,
        toLabelId: LabelId
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun markUnread(
        userId: UserId,
        conversationId: ConversationId,
        contextLabelId: LabelId
    ): Either<DataError, Conversation> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun markUnread(
        userId: UserId,
        conversationIds: List<ConversationId>,
        contextLabelId: LabelId
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun markRead(userId: UserId, conversationId: ConversationId): Either<DataError, Conversation> =
        DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun markRead(
        userId: UserId,
        conversationIds: List<ConversationId>
    ): Either<DataError, List<Conversation>> = DataError.Local.Unknown.left()

    // It will be implemented later on
    override suspend fun isCachedConversationRead(
        userId: UserId,
        conversationId: ConversationId
    ): Either<DataError, Boolean> = DataError.Local.Unknown.left()

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
