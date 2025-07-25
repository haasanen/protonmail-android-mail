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

package ch.protonmail.android.mailmailbox.presentation.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.model.MailboxPageKey
import ch.protonmail.android.mailmailbox.domain.usecase.GetMailboxItems
import ch.protonmail.android.mailmailbox.presentation.paging.exception.PaginationErrorException
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.copyWithNewPageToLoad
import ch.protonmail.android.mailpagination.presentation.paging.RustPagingSource
import timber.log.Timber

class MailboxItemPagingSourceFactory(
    private val getMailboxItems: GetMailboxItems
) {

    fun create(mailboxPageKey: MailboxPageKey, type: MailboxItemType): PagingSource<MailboxPageKey, MailboxItem> =
        RustMailboxItemPagingSource(getMailboxItems, mailboxPageKey, type)
}

class RustMailboxItemPagingSource(
    private val getMailboxItems: GetMailboxItems,
    private val mailboxPageKey: MailboxPageKey,
    private val type: MailboxItemType
) : RustPagingSource<MailboxPageKey, MailboxItem>() {

    override val keyReuseSupported: Boolean
        get() = true

    override suspend fun loadPage(params: LoadParams<MailboxPageKey>): LoadResult<MailboxPageKey, MailboxItem> {
        val key = params.key ?: mailboxPageKey
        val pageKey = key.pageKey

        return getMailboxItems(key.userId, type, pageKey)
            .fold(
                ifLeft = {
                    Timber.e("Paging: loadItems: Error $it")
                    LoadResult.Error(PaginationErrorException(it))
                },
                ifRight = { items ->
                    val nextPageKey = getNextPageKey(items, key)
                    logPageLoaded(items, params, pageKey, nextPageKey?.pageKey)

                    LoadResult.Page(
                        data = items,
                        prevKey = null,
                        nextKey = nextPageKey
                    )
                }
            )
    }

    private fun getNextPageKey(items: List<MailboxItem>, key: MailboxPageKey): MailboxPageKey? {
        val hasNoMoreItems = items.isEmpty()
        if (hasNoMoreItems) {
            return null
        }
        val pageKey = key.pageKey.copyWithNewPageToLoad(pageToLoad = PageToLoad.Next)
        return key.copy(pageKey = pageKey)
    }

    override fun getRefreshKey(state: PagingState<MailboxPageKey, MailboxItem>): MailboxPageKey {
        Timber.d("Paging: getting refresh key")
        return mailboxPageKey.copy(pageKey = mailboxPageKey.pageKey.copyWithNewPageToLoad(pageToLoad = PageToLoad.All))
    }

    private fun logPageLoaded(
        items: List<MailboxItem>,
        params: LoadParams<MailboxPageKey>,
        currentPage: PageKey,
        nextPage: PageKey?
    ) {
        val (firstId, lastId) = items.firstOrNull()?.id to items.lastOrNull()?.id
        Timber.d(
            """
                | Paging: loaded ${items.size} items: ($firstId to $lastId) load type ${params.javaClass.simpleName}
                |   Current:  $currentPage
                |   Next:     $nextPage
            """.trimMargin()
        )
    }
}
