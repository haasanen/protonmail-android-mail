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

import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.mapper.LocalMessageMetadata
import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesWatcher
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.WatchedMessages
import javax.inject.Inject

class RustMessageQueryImpl @Inject constructor(
    private val mailSessionRepository: MailSessionRepository,
    private val invalidationTracker: RustInvalidationTracker,
    private val createRustMessagesWatcher: CreateRustMessagesWatcher,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustMessageQuery {

    private var messagesWatcher: WatchedMessages? = null

    private val mutableMessageStatusFlow = MutableStateFlow<List<LocalMessageMetadata>>(emptyList())
    private val messagesStatusFlow: Flow<List<LocalMessageMetadata>> = mutableMessageStatusFlow.asStateFlow()

    private val messagesUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            mutableMessageStatusFlow.value = messagesWatcher?.messages ?: emptyList()

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.MESSAGE,
                    RustDataSourceId.LABELS
                )
            )
            Timber.d("rust-message: onUpdated, item count: ${mutableMessageStatusFlow.value.size}")
        }
    }

    override fun observeMessages(userId: UserId, labelId: LocalLabelId): Flow<List<LocalMessageMetadata>> {
        coroutineScope.launch {
            val mailSession = mailSessionRepository.getMailSession()
            Timber.v("rust-message: got MailSession instance to watch messages for $userId")

            destroy()
            messagesWatcher = createRustMessagesWatcher(mailSession, labelId, messagesUpdatedCallback)
        }

        return messagesStatusFlow
    }

    override fun disconnect() {
        messagesWatcher?.handle?.disconnect()
        messagesWatcher = null
    }

    private fun destroy() {
        Timber.d("rust-message-query: destroy")
        disconnect()
        mutableMessageStatusFlow.value = emptyList()
    }

}
