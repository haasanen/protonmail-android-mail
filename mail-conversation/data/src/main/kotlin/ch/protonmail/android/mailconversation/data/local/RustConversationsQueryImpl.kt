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

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailpagination.data.extension.appendEventToEither
import ch.protonmail.android.mailpagination.data.extension.filterAppendEvents
import ch.protonmail.android.mailpagination.data.extension.filterRefreshEvents
import ch.protonmail.android.mailpagination.data.extension.refreshEventToEither
import ch.protonmail.android.mailpagination.data.mapper.toPaginationError
import ch.protonmail.android.mailpagination.data.model.PagingEvent
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailpagination.domain.repository.PageInvalidationRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.Conversation
import uniffi.proton_mail_uniffi.ConversationScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.ConversationScrollerUpdate
import javax.inject.Inject

class RustConversationsQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustConversationPaginator: CreateRustConversationPaginator,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope,
    private val invalidationRepository: PageInvalidationRepository
) : RustConversationsQuery {

    private var paginatorState: PaginatorState? = null
    private val paginatorMutex = Mutex()
    private val pagingEvents = MutableSharedFlow<PagingEvent<Conversation>>()

    private val conversationsUpdatedCallback = object : ConversationScrollerLiveQueryCallback {
        override fun onUpdate(update: ConversationScrollerUpdate) {
            Timber.d("rust-conversation-query: paging: conversations update received: $update")

            val event = when (update) {
                is ConversationScrollerUpdate.Append -> PagingEvent.Append(update.v1)
                is ConversationScrollerUpdate.Error -> PagingEvent.Error(update.error.toPaginationError())
                is ConversationScrollerUpdate.None -> PagingEvent.Append(emptyList())
                is ConversationScrollerUpdate.ReplaceBefore -> {
                    // Paging3 doesn't handle granular data updates. Invalidate to cause a full reload
                    invalidateLoadedItems()
                    PagingEvent.Invalidate
                }

                is ConversationScrollerUpdate.ReplaceFrom -> {
                    when {
                        update.isReplaceAllItemsEvent() -> PagingEvent.Refresh(update.items)
                        else -> {
                            // Paging3 doesn't handle granular data updates. Invalidate to cause a full reload
                            invalidateLoadedItems()
                            PagingEvent.Invalidate
                        }
                    }

                }
            }
            coroutineScope.launch {
                pagingEvents.emit(event)
            }

        }
    }

    override suspend fun getConversations(
        userId: UserId,
        pageKey: PageKey.DefaultPageKey
    ): Either<PaginationError, List<LocalConversation>> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-conversation-query: trying to load conversation with a null session")
            return PaginationError.Other(DataError.Local.NoUserSession).left()
        }

        val labelId = pageKey.labelId
        val unread = pageKey.readStatus == ReadStatus.Unread
        Timber.v("rust-conversation-query: observe conversations for labelId $labelId unread: $unread")

        val pageDescriptor = pageKey.toPageDescriptor(userId)

        paginatorMutex.withLock {
            if (shouldInitPaginator(pageDescriptor, pageKey)) {
                initPaginator(pageDescriptor, session)
            }
        }

        Timber.v("rust-conversation-query: Paging: querying ${pageKey.pageToLoad.name} page for conversation")

        return when (pageKey.pageToLoad) {
            PageToLoad.First,
            PageToLoad.Next -> {
                paginatorState?.paginatorWrapper?.nextPage()
                pagingEvents
                    .filterAppendEvents()
                    .appendEventToEither()
            }

            PageToLoad.All -> {
                paginatorState?.paginatorWrapper?.reload()
                pagingEvents
                    .filterRefreshEvents()
                    .refreshEventToEither()
            }
        }
    }

    private suspend fun initPaginator(pageDescriptor: PageDescriptor, session: MailUserSessionWrapper) {

        Timber.v("rust-conversation-query: [destroy and] initialize paginator instance...")
        destroy()

        createRustConversationPaginator(
            session,
            pageDescriptor.labelId.toLocalLabelId(),
            pageDescriptor.unread,
            conversationsUpdatedCallback
        )
            .onRight {
                paginatorState = PaginatorState(
                    paginatorWrapper = it,
                    pageDescriptor = pageDescriptor
                )
            }
    }

    private fun shouldInitPaginator(pageDescriptor: PageDescriptor, pageKey: PageKey.DefaultPageKey) =
        paginatorState == null ||
            paginatorState?.pageDescriptor != pageDescriptor ||
            pageKey.pageToLoad == PageToLoad.First

    private fun destroy() {
        Timber.v("rust-conversation-query: disconnecting and destroying watcher")
        paginatorState?.paginatorWrapper?.disconnect()
        paginatorState = null
    }

    private fun invalidateLoadedItems() {
        coroutineScope.launch {
            invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated)
        }
    }

    private data class PaginatorState(
        val paginatorWrapper: ConversationPaginatorWrapper,
        val pageDescriptor: PageDescriptor
    )

    private data class PageDescriptor(
        val userId: UserId,
        val labelId: LabelId,
        val unread: Boolean
    )

    private fun PageKey.DefaultPageKey.toPageDescriptor(userId: UserId): PageDescriptor {
        return PageDescriptor(
            userId = userId,
            labelId = this.labelId,
            unread = this.readStatus == ReadStatus.Unread
        )
    }

    private fun ConversationScrollerUpdate.ReplaceFrom.isReplaceAllItemsEvent() = this.idx.toInt() == 0

}
