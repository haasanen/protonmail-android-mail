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
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_common.LocalConversation
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.MailboxConversationLiveQuery
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import javax.inject.Inject

class RustConversationQueryImpl @Inject constructor(
    private val rustMailbox: RustMailbox,
    private val invalidationTracker: RustInvalidationTracker,
    @AppScope private val coroutineScope: CoroutineScope
) : RustConversationQuery {

    private var conversationLiveQuery: MailboxConversationLiveQuery? = null

    private val conversationsMutableStatusFlow = MutableStateFlow<List<LocalConversation>>(emptyList())
    private val conversationsStatusFlow: Flow<List<LocalConversation>> = conversationsMutableStatusFlow.asStateFlow()

    private val conversationsUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
        override fun onUpdated() {
            conversationsMutableStatusFlow.value = conversationLiveQuery?.value() ?: emptyList()

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.CONVERSATION,
                    RustDataSourceId.LABELS
                )
            )

            Timber.d(
                "rust-conversation-query: onUpdated, item count: " +
                    "${conversationsMutableStatusFlow.value.size}"
            )
        }
    }

    init {
        Timber.d("rust-conversation-query: init")

        rustMailbox
            .observeConversationMailbox()
            .onEach { mailbox ->
                destroy()

                conversationLiveQuery = mailbox.newConversationLiveQuery(
                    MAX_CONVERSATION_COUNT,
                    conversationsUpdatedCallback
                )
            }
            .launchIn(coroutineScope)
    }

    private fun destroy() {
        Timber.d("rust-conversation-query: destroy")
        disconnect()
        conversationsMutableStatusFlow.value = emptyList()
    }

    override fun disconnect() {
        conversationLiveQuery?.disconnect()
        conversationLiveQuery = null
    }

    override fun observeConversations(userId: UserId, labelId: LocalLabelId): Flow<List<LocalConversation>> {

        rustMailbox.switchToMailbox(userId, labelId)

        return conversationsStatusFlow
    }

    companion object {

        private const val MAX_CONVERSATION_COUNT = 50L
    }
}
