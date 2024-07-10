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
import uniffi.proton_api_mail.LabelId
import uniffi.proton_mail_common.LocalConversation
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalLabelId

interface RustConversationDataSource {
    suspend fun getConversations(labelId: LocalLabelId): List<LocalConversation>
    suspend fun deleteConversations(conversations: List<LocalConversationId>)
    suspend fun markRead(conversations: List<LocalConversationId>)
    suspend fun markUnread(conversations: List<LocalConversationId>)
    suspend fun starConversations(conversations: List<LocalConversationId>)
    suspend fun unStarConversations(conversations: List<LocalConversationId>)
    fun observeConversations(conversationIds: List<LocalConversationId>): Flow<List<LocalConversation>>
    suspend fun relabel(
        conversationIds: List<LocalConversationId>,
        labelsToBeRemoved: List<LocalLabelId>,
        labelsToBeAdded: List<LocalLabelId>
    )
    suspend fun getConversation(conversationId: LocalConversationId): LocalConversation?

    suspend fun moveConversations(conversationIds: List<LocalConversationId>, toLabelId: LocalLabelId)
    suspend fun moveConversationsWithRemoteId(conversationIds: List<LocalConversationId>, toRemoteLabelId: LabelId)

    fun getSenderImage(address: String, bimi: String?): ByteArray?

    fun disconnect()
}
