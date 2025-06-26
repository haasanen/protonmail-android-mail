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

import arrow.core.Either
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import ch.protonmail.android.mailmessage.data.wrapper.MessagePaginatorWrapper
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import ch.protonmail.android.mailmessage.data.MessageRustCoroutineScope
import ch.protonmail.android.mailpagination.domain.cache.PagingCacheWithInvalidationFilter
import ch.protonmail.android.mailpagination.domain.cache.PagingFetcher
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import javax.inject.Inject

class RustMessageQueryImpl @Inject constructor(
    private val messagePaginatorManager: MessagePaginatorManager,
    @MessageRustCoroutineScope private val coroutineScope: CoroutineScope,
    private val cacheWithInvalidationFilter: PagingCacheWithInvalidationFilter<LocalMessageMetadata>
) : RustMessageQuery {

    private val messagesUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.d("rust-message: messages updated, submitting page invalidation")

            coroutineScope.launch {
                cacheWithInvalidationFilter.submitInvalidation(
                    pageInvalidationEvent = PageInvalidationEvent.MessagesInvalidated,
                    fetcher = DefaultMessageFetcher(messagePaginatorManager.getPaginator())
                )
            }
        }
    }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata>? {
        val paginator = messagePaginatorManager.getOrCreatePaginator(userId, pageKey, messagesUpdatedCallback) {
            cacheWithInvalidationFilter.reset()
        }.getOrNull()

        Timber.v("rust-message: Paging: querying ${pageKey.pageToLoad.name} page for messages")

        val messages = when (pageKey.pageToLoad) {

            PageToLoad.First ->
                paginator
                    ?.loadNextPageAndCache { cacheWithInvalidationFilter.replaceData(it, markAsSeen = true) }
                    ?: emptyList()

            PageToLoad.Next ->
                paginator
                    ?.loadNextPageAndCache { cacheWithInvalidationFilter.storeNextPage(it) }
                    ?: emptyList()

            PageToLoad.All -> reloadAllData()
        }

        Timber.v("rust-message: init value for messages is $messages")
        return messages
    }

    private suspend fun reloadAllData(): List<LocalMessageMetadata> {
        Timber.v("rust-message: reload all data")

        val fetcher = DefaultMessageFetcher(messagePaginatorManager.getPaginator())

        // Return unseen data from the cache, if present.
        cacheWithInvalidationFilter.popUnseenData(
            PageInvalidationEvent.MessagesInvalidated, fetcher
        )?.let { return it }

        // Otherwise fetch fresh data and update the cache.
        val freshData = fetcher.reload()
        cacheWithInvalidationFilter.replaceData(freshData, markAsSeen = true)
        return freshData
    }

    private class DefaultMessageFetcher(
        private val paginatorWrapper: MessagePaginatorWrapper?
    ) : PagingFetcher<LocalMessageMetadata> {

        override suspend fun reload(): List<LocalMessageMetadata> {
            val paginator = paginatorWrapper ?: return emptyList()
            val result = paginator.reload()
            return result
                .reloadOnDirtyData(paginator)
                .getOrElse { emptyList() }
        }
    }
}

/**
 * Due to internal state management, the rust lib requires clients to call `all_items` (reload)
 * before any `fetch_items` (nextPage) when the Dirty state happens in order to recover from such state.
 * Here we force the reload and let the paging3 lib call nextPage as needed.
 *
 * Note that dirty state shouldn't happen, as paging3 is already calling a reload each time data is invalidated!
 */
private suspend fun Either<PaginationError, List<LocalMessageMetadata>>.reloadOnDirtyData(
    paginator: MessagePaginatorWrapper?
) = this.onLeft { error ->
    if (error is PaginationError.DirtyPaginationData) {
        paginator?.reload()
    }
}

private suspend fun MessagePaginatorWrapper.loadNextPageAndCache(
    cache: suspend (List<LocalMessageMetadata>) -> Unit
): List<LocalMessageMetadata> = nextPage().onRight { results ->
    cache(results)
}.reloadOnDirtyData(this)
    .getOrElse { emptyList() }
