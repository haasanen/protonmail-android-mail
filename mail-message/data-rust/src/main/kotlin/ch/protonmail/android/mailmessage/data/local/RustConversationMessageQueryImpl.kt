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

package ch.protonmail.android.mailmessage.data.local

import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_common.LocalMessageMetadata
import uniffi.proton_mail_uniffi.ConversationMessagesLiveQueryResult
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import javax.inject.Inject

class RustConversationMessageQueryImpl @Inject constructor(
    private val rustMailbox: RustMailbox,
    private val invalidationTracker: RustInvalidationTracker,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustConversationMessageQuery {

    private var conversationMessagesLiveQueryResult: ConversationMessagesLiveQueryResult? = null
    private var conversationMessagesMutableStatusFlow = MutableStateFlow<LocalConversationMessages?>(null)
    private val conversationMessagesStatusFlow: Flow<LocalConversationMessages> = conversationMessagesMutableStatusFlow
        .asStateFlow()
        .filterNotNull()

    private val conversationMessagesLiveQueryCallback = object : MailboxLiveQueryUpdatedCallback {
        override fun onUpdated() {
            val messages = conversationMessagesLiveQueryResult?.query?.value()
            val messageIdToOpen = conversationMessagesLiveQueryResult?.messageIdToOpen

            if (messages != null && messageIdToOpen != null) {
                conversationMessagesMutableStatusFlow.value = LocalConversationMessages(messageIdToOpen, messages)
            } else {
                Timber.w("rust-conversation-messages: Failed to update conversation messages!")
            }

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.CONVERSATION,
                    RustDataSourceId.LABELS
                )
            )

        }

    }

    private fun initConversationMessagesLiveQuery(conversationId: LocalConversationId) {
        rustMailbox
            .observeConversationMailbox()
            .onEach { mailbox ->
                destroy()

                conversationMessagesLiveQueryResult = mailbox.newConversationMessagesLiveQuery(
                    conversationId,
                    conversationMessagesLiveQueryCallback
                )

                val value = conversationMessagesLiveQueryResult?.let {
                    val messages = it.query.value()
                    val messageIdToOpen = it.messageIdToOpen

                    LocalConversationMessages(messageIdToOpen, messages)

                }
                conversationMessagesMutableStatusFlow.value = value
            }
            .launchIn(coroutineScope)
    }

    private fun destroy() {
        Timber.d("rust-message-query: destroy")
        disconnect()
        conversationMessagesMutableStatusFlow.value = null
    }

    override fun disconnect() {
        conversationMessagesLiveQueryResult?.query?.disconnect()
    }

    override fun observeConversationMessages(
        userId: UserId,
        conversationId: LocalConversationId
    ): Flow<List<LocalMessageMetadata>> {
        initConversationMessagesLiveQuery(conversationId)

        return conversationMessagesStatusFlow
            .map { conversationMessages ->
                conversationMessages.messages
            }
    }

    companion object {

        private const val MAX_MESSAGE_COUNT = 50L
    }

}
