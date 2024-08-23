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

import ch.protonmail.android.mailcommon.domain.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

interface RustConversationDataSource {

    fun observeConversation(userId: UserId, conversationId: LocalConversationId): Flow<LocalConversation>?

    suspend fun getConversations(userId: UserId, labelId: LocalLabelId): List<LocalConversation>
    suspend fun deleteConversations(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun markRead(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun markUnread(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun starConversations(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun unStarConversations(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun relabel(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        labelsToBeRemoved: List<LocalLabelId>,
        labelsToBeAdded: List<LocalLabelId>
    )

    suspend fun moveConversations(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        toLabelId: LocalLabelId
    )

    fun getSenderImage(address: String, bimi: String?): ByteArray?

    fun disconnect()
}
