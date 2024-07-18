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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_common.LocalMessageMetadata
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import uniffi.proton_mail_uniffi.MailboxMessageLiveQuery
import javax.inject.Inject

class RustMessageQueryImpl @Inject constructor(
    private val rustMailbox: RustMailbox,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustMessageQuery {

    private var messageLiveQuery: MailboxMessageLiveQuery? = null
    private var _messagesStatusFlow = MutableStateFlow<List<LocalMessageMetadata>>(emptyList())
    private val messagesStatusFlow: Flow<List<LocalMessageMetadata>> = _messagesStatusFlow.asStateFlow()

    private val messagesUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
        override fun onUpdated() {
            _messagesStatusFlow.value = messageLiveQuery?.value() ?: emptyList()

            Timber.d("rust-message: onUpdated, item count: ${_messagesStatusFlow.value.size}")
        }
    }

    init {
        Timber.d("rust-message-query: init")

        rustMailbox
            .observeMessageMailbox()
            .onEach { mailbox ->
                destroy()

                messageLiveQuery = mailbox.newMessageLiveQuery(
                    MAX_MESSAGE_COUNT,
                    messagesUpdatedCallback
                )
            }
            .launchIn(coroutineScope)
    }

    private fun destroy() {
        Timber.d("rust-message-query: destroy")
        disconnect()
        _messagesStatusFlow.value = emptyList()
    }

    override fun disconnect() {
        messageLiveQuery?.disconnect()
        messageLiveQuery = null
    }

    override fun observeMessages(userId: UserId, labelId: LocalLabelId): Flow<List<LocalMessageMetadata>> {

        rustMailbox.switchToMailbox(userId, labelId)

        return messagesStatusFlow
    }

    companion object {

        private const val MAX_MESSAGE_COUNT = 50L
    }

}
