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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import uniffi.proton_mail_common.LocalConversation
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.MailboxConversationLiveQuery
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import javax.inject.Inject

class RustConversationQueryImpl @Inject constructor(
    private val rustMailbox: RustMailbox,
    @AppScope private val coroutineScope: CoroutineScope
) : RustConversationQuery {

    private var conversationLiveQuery: MailboxConversationLiveQuery? = null

    private val _conversationsStatusFlow = MutableStateFlow<List<LocalConversation>>(emptyList())
    private val conversationsStatusFlow: Flow<List<LocalConversation>> = _conversationsStatusFlow.asStateFlow()

    private val conversationsUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
        override fun onUpdated() {
            _conversationsStatusFlow.value = conversationLiveQuery?.value() ?: emptyList()

            Timber.d("rust-conversation-query: onUpdated, item count: ${_conversationsStatusFlow.value.size}")
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
        _conversationsStatusFlow.value = emptyList()
    }

    override fun disconnect() {
        conversationLiveQuery?.disconnect()
        conversationLiveQuery = null
    }

    override fun observeConversations(labelId: LocalLabelId): Flow<List<LocalConversation>> {

        rustMailbox.switchToMailbox(labelId)

        return conversationsStatusFlow
    }

    override fun observeConversations(): Flow<List<LocalConversation>> = conversationsStatusFlow

    companion object {

        private const val MAX_CONVERSATION_COUNT = 50L
    }
}
