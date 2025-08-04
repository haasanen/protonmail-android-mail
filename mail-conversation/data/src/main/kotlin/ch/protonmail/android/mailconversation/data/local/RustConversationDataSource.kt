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

package ch.protonmail.android.mailconversation.data.local

import arrow.core.Either
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailcommon.data.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import uniffi.proton_mail_uniffi.AllBottomBarMessageActions
import uniffi.proton_mail_uniffi.ConversationAvailableActions
import uniffi.proton_mail_uniffi.LabelAsAction
import uniffi.proton_mail_uniffi.MoveAction

interface RustConversationDataSource {

    fun observeConversation(
        userId: UserId,
        conversationId: LocalConversationId,
        labelId: LocalLabelId
    ): Flow<Either<DataError, LocalConversation>>

    fun observeConversationMessages(
        userId: UserId,
        conversationId: LocalConversationId,
        labelId: LocalLabelId
    ): Flow<Either<DataError, LocalConversationMessages>>

    suspend fun getConversations(
        userId: UserId,
        pageKey: PageKey.DefaultPageKey
    ): Either<PaginationError, List<LocalConversation>>

    suspend fun deleteConversations(
        userId: UserId,
        conversations: List<LocalConversationId>
    ): Either<DataError.Local, Unit>

    suspend fun markRead(
        userId: UserId,
        labelId: LocalLabelId,
        conversations: List<LocalConversationId>
    )

    suspend fun markUnread(
        userId: UserId,
        labelId: LocalLabelId,
        conversations: List<LocalConversationId>
    )

    suspend fun starConversations(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun unStarConversations(userId: UserId, conversations: List<LocalConversationId>)

    suspend fun moveConversations(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        toLabelId: LocalLabelId
    ): Either<DataError.Local, Unit>

    fun getSenderImage(address: String, bimi: String?): ByteArray?

    suspend fun getAvailableActions(
        userId: UserId,
        labelId: LocalLabelId,
        conversationIds: List<LocalConversationId>
    ): Either<DataError, ConversationAvailableActions>

    suspend fun getAvailableSystemMoveToActions(
        userId: UserId,
        labelId: LocalLabelId,
        conversationIds: List<LocalConversationId>
    ): Either<DataError, List<MoveAction.SystemFolder>>

    suspend fun getAvailableLabelAsActions(
        userId: UserId,
        labelId: LocalLabelId,
        conversationIds: List<LocalConversationId>
    ): Either<DataError, List<LabelAsAction>>

    suspend fun getAllAvailableBottomBarActions(
        userId: UserId,
        labelId: LocalLabelId,
        conversationIds: List<LocalConversationId>
    ): Either<DataError, AllBottomBarMessageActions>

    suspend fun labelConversations(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        selectedLabelIds: List<LocalLabelId>,
        partiallySelectedLabelIds: List<LocalLabelId>,
        shouldArchive: Boolean
    ): Either<DataError, Unit>
}
