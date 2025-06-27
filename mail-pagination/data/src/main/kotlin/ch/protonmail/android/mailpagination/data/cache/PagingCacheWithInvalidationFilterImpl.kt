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

package ch.protonmail.android.mailpagination.data.cache

import ch.protonmail.android.mailpagination.data.PagingCoroutineScope
import ch.protonmail.android.mailpagination.domain.cache.PagingCacheWithInvalidationFilter
import ch.protonmail.android.mailpagination.domain.cache.PagingFetcher
import timber.log.Timber
import javax.inject.Inject
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.repository.PageInvalidationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PagingCacheWithInvalidationFilterImpl<T> @Inject constructor(
    private val pageInvalidationRepository: PageInvalidationRepository,
    @PagingCoroutineScope private val coroutineScope: CoroutineScope
) : PagingCacheWithInvalidationFilter<T> {

    private var hasUnseenData = false
    private val mutex = Mutex()
    private var data: List<T> = emptyList()

    override suspend fun submitInvalidation(pageInvalidationEvent: PageInvalidationEvent, fetcher: PagingFetcher<T>) {
        Timber.d("paging-cache-invalidation: Received invalidation event")

        if (data.isEmpty()) {
            Timber.d("paging-cache-invalidation: No data to check for invalidation, forwarding event.")
            pageInvalidationRepository.submit(pageInvalidationEvent)
            return
        }

        refreshDataIfChanged(pageInvalidationEvent, fetcher)
    }

    private suspend fun refreshDataIfChanged(pageInvalidationEvent: PageInvalidationEvent, fetcher: PagingFetcher<T>) {
        val newData = fetcher.reload()
        Timber.d("paging-cache-invalidation: Fetched new data, size: ${newData.size}")

        if (newData != data) {
            Timber.d("paging-cache-invalidation: Data changed — invalidation accepted.")
            mutex.withLock {
                data = newData
                hasUnseenData = true
            }
            pageInvalidationRepository.submit(pageInvalidationEvent)
        } else {
            Timber.d("paging-cache-invalidation: No change — invalidation skipped.")
        }
    }

    override suspend fun replaceData(newData: List<T>, markAsSeen: Boolean) {
        mutex.withLock {
            data = newData
            if (markAsSeen) hasUnseenData = false
        }
    }

    override suspend fun storeNextPage(newItems: List<T>) {
        mutex.withLock {
            data = data + newItems
        }
    }

    override suspend fun popUnseenData(
        pageInvalidationEvent: PageInvalidationEvent,
        fetcher: PagingFetcher<T>
    ): List<T>? {
        return mutex.withLock {
            if (hasUnseenData) {
                hasUnseenData = false

                coroutineScope.launch {
                    Timber.d("paging-cache-invalidation: Refreshing unseen data in background.")
                    runCatching {
                        refreshDataIfChanged(pageInvalidationEvent, fetcher)
                    }.onFailure {
                        Timber.e(it, "paging-cache-invalidation: Failed to refresh unseen data")
                    }
                }

                Timber.d("paging-cache-invalidation: Popped unseen data, size: ${data.size}")
                data
            } else {
                Timber.d("paging-cache-invalidation: No unseen data to pop.")
                null
            }
        }
    }

    override suspend fun reset() {
        Timber.d("paging-cache-invalidation: Resetting cache.")
        mutex.withLock {
            data = emptyList()
            hasUnseenData = false
        }
    }

    override suspend fun hasUnseenData(): Boolean = mutex.withLock { hasUnseenData }
}
