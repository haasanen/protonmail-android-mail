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
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailconversation.data.ConversationRustCoroutineScope
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailpagination.domain.cache.PagingCacheWithInvalidationFilter
import ch.protonmail.android.mailpagination.domain.cache.PagingFetcher
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.Conversation
import uniffi.proton_mail_uniffi.LiveQueryCallback
import javax.inject.Inject

class RustConversationsQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustConversationPaginator: CreateRustConversationPaginator,
    @ConversationRustCoroutineScope private val coroutineScope: CoroutineScope,
    private val cacheWithInvalidationFilter: PagingCacheWithInvalidationFilter<LocalConversation>
) : RustConversationsQuery {

    private var paginatorState: PaginatorState? = null
    private val paginatorMutex = Mutex()

    private val conversationsUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.d("rust-conversation-query: paging: conversations updated, invalidating paginator...")

            coroutineScope.launch {
                cacheWithInvalidationFilter.submitInvalidation(
                    pageInvalidationEvent = PageInvalidationEvent.ConversationsInvalidated,
                    fetcher = DefaultConversationFetcher(paginatorState?.paginatorWrapper)
                )
            }
        }
    }

    override suspend fun getConversations(userId: UserId, pageKey: PageKey.DefaultPageKey): List<LocalConversation>? {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-conversation-query: trying to load conversation with a null session")
            return null
        }

        val labelId = pageKey.labelId
        val unread = pageKey.readStatus == ReadStatus.Unread
        Timber.v("rust-conversation-query: observe conversations for labelId $labelId unread: $unread")

        val pageDescriptor = pageKey.toPageDescriptor(userId)

        paginatorMutex.withLock {
            if (shouldInitPaginator(pageDescriptor, pageKey)) {
                initPaginator(pageDescriptor, session)
                cacheWithInvalidationFilter.reset()
            }
        }

        Timber.v("rust-conversation-query: Paging: querying ${pageKey.pageToLoad.name} page for conversation")

        val conversations = when (pageKey.pageToLoad) {

            PageToLoad.First ->
                paginatorState?.paginatorWrapper
                    ?.loadNextPageAndCache { cacheWithInvalidationFilter.replaceData(it, markAsSeen = true) }
                    ?: emptyList()

            PageToLoad.Next ->
                paginatorState?.paginatorWrapper
                    ?.loadNextPageAndCache { cacheWithInvalidationFilter.storeNextPage(it) }
                    ?: emptyList()

            PageToLoad.All -> reloadAllData()
        }

        return conversations
    }

    private suspend fun reloadAllData(): List<LocalConversation> {
        Timber.v("rust-conversation-query: reload all data")

        val fetcher = DefaultConversationFetcher(paginatorState?.paginatorWrapper)

        // Return unseen data from the cache, if present.
        cacheWithInvalidationFilter.popUnseenData(
            PageInvalidationEvent.ConversationsInvalidated, fetcher
        )?.let { return it }

        // Otherwise fetch fresh data and update the cache.
        val freshData = fetcher.reload()
        cacheWithInvalidationFilter.replaceData(freshData, markAsSeen = true)
        return freshData
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

    private data class PaginatorState(
        val paginatorWrapper: ConversationPaginatorWrapper,
        val pageDescriptor: PageDescriptor
    )

    private data class PageDescriptor(
        val userId: UserId,
        val labelId: LabelId,
        val unread: Boolean
    )

    private class DefaultConversationFetcher(
        private val paginatorWrapper: ConversationPaginatorWrapper?
    ) : PagingFetcher<LocalConversation> {

        override suspend fun reload(): List<LocalConversation> {
            val paginator = paginatorWrapper ?: return emptyList()
            val result = paginator.reload()
            return result
                .reloadOnDirtyData(paginator)
                .getOrElse { emptyList() }
        }
    }

    private fun PageKey.DefaultPageKey.toPageDescriptor(userId: UserId): PageDescriptor {
        return PageDescriptor(
            userId = userId,
            labelId = this.labelId,
            unread = this.readStatus == ReadStatus.Unread
        )
    }
}


/**
 * Due to internal state management, the rust lib requires clients to call `all_items` (reload)
 * before any `fetch_items` (nextPage) when the Dirty state happens in order to recover from such state.
 * Here we force the reload and let the paging3 lib call nextPage as needed.
 *
 * Note that dirty state shouldn't happen, as paging3 is already calling a reload each time data is invalidated!
 */
private suspend fun Either<PaginationError, List<Conversation>>.reloadOnDirtyData(
    paginator: ConversationPaginatorWrapper?
) = this.onLeft { error ->
    if (error is PaginationError.DirtyPaginationData) {
        Timber.w("rust-conversation-query: Paginator in dirty state $error")
        paginator?.reload()
    }
}

private suspend fun ConversationPaginatorWrapper.loadNextPageAndCache(
    cache: suspend (List<LocalConversation>) -> Unit
): List<LocalConversation> = nextPage().onRight { results ->
    cache(results)
}.reloadOnDirtyData(this)
    .getOrElse { emptyList() }
