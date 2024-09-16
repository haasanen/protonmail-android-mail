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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesWatcher
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MessagePaginator
import javax.inject.Inject

class RustMessageQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val invalidationTracker: RustInvalidationTracker,
    private val createRustMessagesWatcher: CreateRustMessagesWatcher,
    private val rustMailbox: RustMailbox,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustMessageQuery {

    private var paginator: MessagePaginator? = null

    private val mutableMessageStatusFlow = MutableStateFlow<List<LocalMessageMetadata>?>(null)
    private val messagesStatusFlow: Flow<List<LocalMessageMetadata>> = mutableMessageStatusFlow
        .asStateFlow()
        .filterNotNull()

    private val messagesUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.d("rust-message: messages updated, invalidating pagination...")

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.MESSAGE,
                    RustDataSourceId.LABELS
                )
            )
        }
    }

    override fun observeMessages(userId: UserId, pageKey: PageKey): Flow<List<LocalMessageMetadata>> {
        destroy()

        coroutineScope.launch {
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-message: trying to load message with a null session")
                return@launch
            }
            Timber.v("rust-message: got MailSession instance to watch messages for $userId")

            val labelId = pageKey.labelId.toLocalLabelId()
            rustMailbox.switchToMailbox(userId, labelId)
            Timber.v("rust-message: switching mailbox to $labelId if needed...")

            paginator = createRustMessagesWatcher(session, Id(labelId.value), messagesUpdatedCallback)

            val messages = paginator?.currentPage()
            Timber.v("rust-message: init value for messages is $messages")
            mutableMessageStatusFlow.value = messages
        }

        Timber.v("rust-messages: returning messages status flow...")
        return messagesStatusFlow
    }

    override fun disconnect() {
        paginator?.handle()?.disconnect()
        paginator = null
    }

    private fun destroy() {
        Timber.d("rust-message-query: destroy")
        disconnect()
        mutableMessageStatusFlow.value = null
    }

}
