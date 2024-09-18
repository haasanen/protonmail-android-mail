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
import arrow.core.getOrElse
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.model.MailboxPageKey
import ch.protonmail.android.mailmailbox.domain.usecase.GetMailboxItems
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.presentation.paging.RustPagingSource
import timber.log.Timber
import kotlin.math.max

class MailboxItemPagingSourceFactory(
    private val getMailboxItems: GetMailboxItems,
    private val rustInvalidationTracker: RustInvalidationTracker
) {

    fun create(mailboxPageKey: MailboxPageKey, type: MailboxItemType): PagingSource<MailboxPageKey, MailboxItem> =
        RustMailboxItemPagingSource(getMailboxItems, rustInvalidationTracker, mailboxPageKey, type)
}

class RustMailboxItemPagingSource(
    private val getMailboxItems: GetMailboxItems,
    rustInvalidationTracker: RustInvalidationTracker,
    private val mailboxPageKey: MailboxPageKey,
    private val type: MailboxItemType
) : RustPagingSource<MailboxPageKey, MailboxItem>(
    rustInvalidationTracker = rustInvalidationTracker
) {

    override suspend fun loadPage(params: LoadParams<MailboxPageKey>): LoadResult<MailboxPageKey, MailboxItem> {
        val key = params.key ?: mailboxPageKey
        val userId = key.userId
        val size = max(key.pageKey.size, params.loadSize)
        val pageKey = key.pageKey.copy(size = size)

        val items = getMailboxItems(userId, type, pageKey).getOrElse {
            Timber.e("Paging: loadItems: Error $it")
            return LoadResult.Page(emptyList(), null, null)
        }
        Timber.d("Paging: loadItems: ${items.size}/$size (${params.javaClass.simpleName})-> $pageKey")

        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey = null
        )
    }

    override fun getRefreshKey(state: PagingState<MailboxPageKey, MailboxItem>): MailboxPageKey? = null

}
