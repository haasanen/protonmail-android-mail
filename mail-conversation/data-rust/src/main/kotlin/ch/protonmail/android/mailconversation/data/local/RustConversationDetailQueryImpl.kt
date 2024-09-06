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
import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationWatcher
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.WatchedConversation
import javax.inject.Inject

class RustConversationDetailQueryImpl @Inject constructor(
    private val rustMailbox: RustMailbox,
    private val createRustConversationWatcher: CreateRustConversationWatcher,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustConversationDetailQuery {

    private var conversationWatcher: WatchedConversation? = null
    private var currentConversationId: LocalConversationId? = null
    private val mutex = Mutex()
    private val conversationMutableStatusFlow = MutableStateFlow<LocalConversation?>(null)
    private val conversationStatusFlow = conversationMutableStatusFlow
        .asStateFlow()
        .filterNotNull()

    private var conversationMessagesMutableStatusFlow = MutableStateFlow<LocalConversationMessages?>(null)
    private val conversationMessagesStatusFlow: Flow<LocalConversationMessages> = conversationMessagesMutableStatusFlow
        .asStateFlow()
        .filterNotNull()

    private val conversationUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            conversationMutableStatusFlow.value = conversationWatcher?.conversation
            val messages = conversationWatcher?.messages
            val messageIdToOpen = conversationWatcher?.messageIdToOpen

            if (messages != null && messageIdToOpen != null) {
                val localConversationMessages = LocalConversationMessages(messageIdToOpen, messages)
                conversationMessagesMutableStatusFlow.value = localConversationMessages
                Timber.d("rust-conversation-messages: new messages value is $localConversationMessages")
            } else {
                Timber.w("rust-conversation-messages: Failed to update conversation messages!")
            }

            Timber.d("rust-conversation-detail-query: onUpdated, item ${conversationMutableStatusFlow.value}")
        }
    }

    override fun observeConversation(userId: UserId, conversationId: LocalConversationId): Flow<LocalConversation> {

        coroutineScope.launch {
            mutex.withLock {
                if (currentConversationId != conversationId || conversationWatcher == null) {
                    // If the conversationId is different or there's no active watcher, destroy and create a new one
                    destroy()

                    val mailbox = rustMailbox.observeConversationMailbox().firstOrNull()
                    if (mailbox == null) {
                        Timber.e("rust-conversation-detail-query: Failed to observe conversation, null mailbox")
                        return@withLock
                    }

                    conversationWatcher =
                        createRustConversationWatcher(mailbox, conversationId, conversationUpdatedCallback)
                    val conversation = conversationWatcher?.conversation ?: run {
                        Timber.w(
                            "rust-conversation-detail-query: init value for " +
                                "$conversationId from $conversationWatcher is null"
                        )
                        null
                    }
                    conversationMutableStatusFlow.value = conversation
                    currentConversationId = conversationId

                    val messages = conversationWatcher?.messages
                    val messageIdToOpen = conversationWatcher?.messageIdToOpen
                    if (messages != null && messageIdToOpen != null) {
                        val localConversationMessages = LocalConversationMessages(messageIdToOpen, messages)
                        conversationMessagesMutableStatusFlow.value = localConversationMessages
                        Timber.d("rust-conversation-messages: new messages value is $localConversationMessages")
                    } else {
                        Timber.w("rust-conversation-messages: Failed to update conversation messages!")
                    }

                    Timber.d("rust-conversation-detail-query: conversation watcher created for $conversationId")
                }
            }
        }

        return conversationStatusFlow
    }

    override fun observeConversationMessages(
        userId: UserId,
        conversationId: LocalConversationId
    ): Flow<LocalConversationMessages> {
        Timber.v("rust-conversation-detail-query: Observe conversation query starting...")

        return conversationMessagesStatusFlow
    }

    override fun disconnect() {
        Timber.d(
            "rust-conversation-detail-query: disconnecting conversation watcher for " +
                "$currentConversationId"
        )

        conversationWatcher?.handle?.disconnect()
        conversationWatcher = null
        currentConversationId = null
    }

    private fun destroy() {
        Timber.d("rust-conversation-detail-query: destroy")
        disconnect()
        conversationMessagesMutableStatusFlow.value = null
        conversationMutableStatusFlow.value = null
    }

}
