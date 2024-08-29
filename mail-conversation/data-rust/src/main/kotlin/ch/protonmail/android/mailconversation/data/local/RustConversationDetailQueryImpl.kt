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
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationWatcher
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.WatchedConversation
import javax.inject.Inject

class RustConversationDetailQueryImpl @Inject constructor(
    private val rustMailbox: RustMailbox,
    private val createRustConversationWatcher: CreateRustConversationWatcher,
    @AppScope private val coroutineScope: CoroutineScope
) : RustConversationDetailQuery {

    private var conversationWatcher: WatchedConversation? = null

    private val conversationMutableStatusFlow = MutableStateFlow<LocalConversation?>(null)
    private val conversationStatusFlow = conversationMutableStatusFlow
        .asStateFlow()
        .filterNotNull()

    private val conversationUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            conversationMutableStatusFlow.value = conversationWatcher?.conversation
            Timber.d("rust-conversation-query: onUpdated, item ${conversationMutableStatusFlow.value}")
        }
    }

    override fun observeConversation(userId: UserId, conversationId: LocalConversationId): Flow<LocalConversation> {
        destroy()

        coroutineScope.launch {

            val mailbox = rustMailbox.observeConversationMailbox().firstOrNull()
            if (mailbox == null) {
                Timber.e("rust-conversation: Failed to observe conversation, null mailbox")
                return@launch
            }

            conversationWatcher = createRustConversationWatcher(mailbox, conversationId, conversationUpdatedCallback)
            val conversation = conversationWatcher?.conversation ?: run {
                Timber.w("rust-conversation: init value for $conversationId from $conversationWatcher is null")
                null
            }
            conversationMutableStatusFlow.value = conversation
            Timber.d("rust-conversation: conversation watcher created, initial value is $conversation")
        }

        return conversationStatusFlow
    }

    override fun disconnect() {
        conversationWatcher?.conversationHandle?.disconnect()
        conversationWatcher = null
    }

    private fun destroy() {
        Timber.d("rust-conversation-query: destroy")
        disconnect()
        conversationMutableStatusFlow.value = null
    }

}
