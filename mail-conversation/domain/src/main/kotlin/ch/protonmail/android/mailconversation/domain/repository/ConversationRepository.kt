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

package ch.protonmail.android.mailconversation.domain.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.entity.Conversation
import ch.protonmail.android.mailmessage.domain.model.ConversationMessages
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailpagination.domain.model.PageKey
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.maillabel.domain.model.LabelId

@Suppress("TooManyFunctions", "ComplexInterface")
interface ConversationRepository {

    /**
     * Load all [Conversation] from local cache for [userId].
     */
    suspend fun getLocalConversations(userId: UserId, pageKey: PageKey = PageKey()): List<Conversation>

    /**
     * Return true if all [Conversation] are considered locally valid according the given [pageKey].
     */
    suspend fun isLocalPageValid(
        userId: UserId,
        pageKey: PageKey,
        items: List<Conversation>
    ): Boolean

    /**
     * Mark local data as stale for [userId], by [labelId].
     */
    suspend fun markAsStale(userId: UserId, labelId: LabelId)

    /**
     * Get a conversation.
     * Returns any conversation data that is available locally right away.
     * Message metadata is fetched and returned as available
     */
    fun observeConversation(
        userId: UserId,
        id: ConversationId,
        refreshData: Boolean
    ): Flow<Either<DataError, Conversation>>

    /**
     * Get all the [Message]s metadata for a given [ConversationId], for [userId] from the local storage
     */
    fun observeConversationMessages(
        userId: UserId,
        conversationId: ConversationId
    ): Flow<Either<DataError.Local, ConversationMessages>>

    /**
     * Adds the given [labelId] to the message with the given [conversationId]
     */
    suspend fun addLabel(
        userId: UserId,
        conversationId: ConversationId,
        labelId: LabelId
    ): Either<DataError, Conversation>

    suspend fun addLabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelId: LabelId
    ): Either<DataError, List<Conversation>>

    suspend fun addLabels(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelIds: List<LabelId>
    ): Either<DataError, List<Conversation>>

    suspend fun removeLabel(
        userId: UserId,
        conversationId: ConversationId,
        labelId: LabelId
    ): Either<DataError, Conversation>

    suspend fun removeLabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelId: LabelId
    ): Either<DataError, List<Conversation>>

    suspend fun removeLabels(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelIds: List<LabelId>
    ): Either<DataError, List<Conversation>>

    suspend fun move(
        userId: UserId,
        conversationIds: List<ConversationId>,
        toLabelId: LabelId
    ): Either<DataError, List<Conversation>>

    suspend fun markUnread(
        userId: UserId,
        conversationIds: List<ConversationId>,
        contextLabelId: LabelId
    ): Either<DataError, List<Conversation>>

    suspend fun markRead(userId: UserId, conversationIds: List<ConversationId>): Either<DataError, List<Conversation>>

    suspend fun star(userId: UserId, conversationIds: List<ConversationId>): Either<DataError, List<Conversation>>

    suspend fun unStar(userId: UserId, conversationIds: List<ConversationId>): Either<DataError, List<Conversation>>

    suspend fun relabel(
        userId: UserId,
        conversationId: ConversationId,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError, Conversation>

    suspend fun relabel(
        userId: UserId,
        conversationIds: List<ConversationId>,
        labelsToBeRemoved: List<LabelId>,
        labelsToBeAdded: List<LabelId>
    ): Either<DataError, List<Conversation>>

    suspend fun deleteConversations(
        userId: UserId,
        conversationIds: List<ConversationId>,
        contextLabelId: LabelId
    ): Either<DataError, Unit>

    suspend fun deleteConversations(userId: UserId, labelId: LabelId)

    fun observeClearLabelOperation(userId: UserId, labelId: LabelId): Flow<Boolean>
}
