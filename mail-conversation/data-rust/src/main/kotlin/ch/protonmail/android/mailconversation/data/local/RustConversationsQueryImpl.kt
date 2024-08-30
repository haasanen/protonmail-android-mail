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

import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailcommon.datarust.mapper.LocalConversation
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationForLabelWatcher
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.WatchedConversations
import javax.inject.Inject

class RustConversationsQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val invalidationTracker: RustInvalidationTracker,
    private val createRustConversationForLabelWatcher: CreateRustConversationForLabelWatcher,
    private val rustMailbox: RustMailbox,
    @AppScope private val coroutineScope: CoroutineScope
) : RustConversationsQuery {

    private var conversationsWatcher: WatchedConversations? = null

    private val conversationsMutableStatusFlow = MutableStateFlow<List<LocalConversation>?>(null)
    private val conversationsStatusFlow = conversationsMutableStatusFlow
        .asStateFlow()
        .filterNotNull()

    private val conversationsUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.v("rust-conversations-query: callback fired")
            val conversations = conversationsWatcher?.conversations
            conversationsMutableStatusFlow.value = conversations

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.CONVERSATION,
                    RustDataSourceId.LABELS
                )
            )

            Timber.d("rust-conversations-query: onUpdated, item count ${conversations?.count()}")
        }
    }

    override fun observeConversationsByLabel(userId: UserId, labelId: LocalLabelId): Flow<List<LocalConversation>> {
        destroy()

        coroutineScope.launch {
            Timber.v("rust-conversation-query: observe conversations for labelId $labelId")
            rustMailbox.switchToMailbox(userId, labelId)

            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-conversation-query: trying to load message with a null session")
                return@launch
            }
            conversationsWatcher = createRustConversationForLabelWatcher(session, labelId, conversationsUpdatedCallback)

            val conversations = conversationsWatcher?.conversations
            Timber.v("rust-conversation-query: init value for conversations is $conversations")
            conversationsMutableStatusFlow.value = conversations
        }

        Timber.v("rust-conversation-query: returning conversation status flow...")
        return conversationsStatusFlow
    }


    private fun destroy() {
        Timber.v("rust-conversation-query: disconnecting and destroying watcher")
        conversationsWatcher?.handle?.disconnect()
        conversationsWatcher = null
        conversationsMutableStatusFlow.value = null
    }

}
