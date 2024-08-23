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

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailmessage.data.usecase.CreateRustConversationMessagesWatcher
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.WatchedConversation
import javax.inject.Inject

class RustConversationMessageQueryImpl @Inject constructor(
    private val rustMailbox: RustMailbox,
    private val createRustConversationMessagesWatcher: CreateRustConversationMessagesWatcher,
    private val invalidationTracker: RustInvalidationTracker,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustConversationMessageQuery {

    private var conversationWatcher: WatchedConversation? = null
    private var conversationMessagesMutableStatusFlow = MutableStateFlow<LocalConversationMessages?>(null)
    private val conversationMessagesStatusFlow: Flow<LocalConversationMessages> = conversationMessagesMutableStatusFlow
        .asStateFlow()
        .filterNotNull()

    private val conversationMessagesLiveQueryCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            val messages = conversationWatcher?.messages
            // TODO: get message to open from rust when exposed
            val messageIdToOpen = conversationWatcher?.messages?.firstOrNull()?.localId

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

    override fun observeConversationMessages(
        userId: UserId,
        conversationId: LocalConversationId
    ): Flow<LocalConversationMessages> {
        destroy()
        initConversationMessagesLiveQuery(conversationId)

        return conversationMessagesStatusFlow
    }

    override fun disconnect() {
        conversationWatcher?.messagesHandle?.disconnect()
        conversationWatcher?.conversationHandle?.disconnect()
    }

    @MissingRustApi
    // MessageIdToOpen not returned with conversations anymore?
    private fun initConversationMessagesLiveQuery(conversationId: LocalConversationId) {
        rustMailbox
            .observeConversationMailbox()
            .onEach { mailbox ->
                destroy()

                conversationWatcher = createRustConversationMessagesWatcher(
                    mailbox,
                    conversationId,
                    conversationMessagesLiveQueryCallback
                )

                val value = conversationWatcher?.let {
                    val messages = it.messages
                    val messageIdToOpen = it.messages.firstOrNull()?.localId ?: 0uL

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
}
