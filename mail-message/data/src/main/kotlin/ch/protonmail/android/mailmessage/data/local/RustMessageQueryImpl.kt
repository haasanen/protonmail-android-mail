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

import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailpagination.data.mapper.toPaginationError
import ch.protonmail.android.mailpagination.data.model.PagingEvent
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.repository.PageInvalidationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.Message
import uniffi.proton_mail_uniffi.MessageScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.MessageScrollerUpdate
import javax.inject.Inject

class RustMessageQueryImpl @Inject constructor(
    private val messagePaginatorManager: MessagePaginatorManager,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope,
    private val invalidationRepository: PageInvalidationRepository
) : RustMessageQuery {

    private val pagingEvents = MutableSharedFlow<PagingEvent<Message>>()

    private val messagesUpdatedCallback = object : MessageScrollerLiveQueryCallback {

        override fun onUpdate(update: MessageScrollerUpdate) {
            Timber.d("rust-message: messages update received $update")

            val event = when (update) {
                is MessageScrollerUpdate.Append -> PagingEvent.Append(update.v1)
                is MessageScrollerUpdate.Error -> PagingEvent.Error(update.error.toPaginationError())
                is MessageScrollerUpdate.None -> PagingEvent.Append(emptyList())
                is MessageScrollerUpdate.ReplaceBefore -> {
                    // Paging3 doesn't handle granular data updates. Invalidate to cause a full reload
                    invalidateLoadedItems()
                    PagingEvent.None
                }
                is MessageScrollerUpdate.ReplaceFrom -> {
                    when {
                        update.isReplaceAllItemsEvent() -> PagingEvent.Refresh(update.items)
                        else -> {
                            // Paging3 doesn't handle granular data updates. Invalidate to cause a full reload
                            invalidateLoadedItems()
                            PagingEvent.None
                        }
                    }

                }
            }
            coroutineScope.launch {
                pagingEvents.emit(event)
            }
        }
    }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata> {
        val paginator = messagePaginatorManager.getOrCreatePaginator(userId, pageKey, messagesUpdatedCallback) {
        }.getOrNull()

        Timber.v("rust-message: Paging: querying ${pageKey.pageToLoad.name} page for messages")

        val messages = when (pageKey.pageToLoad) {
            PageToLoad.First,
            PageToLoad.Next -> {
                paginator?.nextPage()
                pagingEvents
                    .filterIsInstance<PagingEvent.Append<Message>>()
                    .first()
                    .items
            }

            PageToLoad.All -> {
                paginator?.reload()
                pagingEvents
                    .filterIsInstance<PagingEvent.Refresh<Message>>()
                    .first()
                    .items
            }
        }

        Timber.v("rust-message: init value for messages is $messages")
        return messages
    }

    private fun invalidateLoadedItems() {
        coroutineScope.launch {
            invalidationRepository.submit(PageInvalidationEvent.MessagesInvalidated)
        }
    }

    private fun MessageScrollerUpdate.ReplaceFrom.isReplaceAllItemsEvent() = this.idx.toInt() == 0
}

