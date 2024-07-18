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

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import uniffi.proton_api_mail.LabelId
import uniffi.proton_mail_common.LocalConversation
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalLabelId

interface RustConversationDataSource {

    suspend fun getConversations(userId: UserId, labelId: LocalLabelId): List<LocalConversation>
    suspend fun deleteConversations(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun markRead(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun markUnread(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun starConversations(userId: UserId, conversations: List<LocalConversationId>)
    suspend fun unStarConversations(userId: UserId, conversations: List<LocalConversationId>)
    fun observeConversations(userId: UserId, conversationIds: List<LocalConversationId>): Flow<List<LocalConversation>>
    suspend fun relabel(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        labelsToBeRemoved: List<LocalLabelId>,
        labelsToBeAdded: List<LocalLabelId>
    )

    suspend fun getConversation(userId: UserId, conversationId: LocalConversationId): LocalConversation?

    suspend fun moveConversations(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        toLabelId: LocalLabelId
    )
    suspend fun moveConversationsWithRemoteId(
        userId: UserId,
        conversationIds: List<LocalConversationId>,
        toRemoteLabelId: LabelId
    )

    fun getSenderImage(address: String, bimi: String?): ByteArray?

    fun disconnect()
}
