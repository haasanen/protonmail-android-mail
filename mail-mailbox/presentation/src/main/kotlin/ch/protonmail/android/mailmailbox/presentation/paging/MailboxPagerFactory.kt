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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.model.MailboxPageKey
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class MailboxPagerFactory @Inject constructor(
    private val pagingSourceFactory: MailboxItemPagingSourceFactory
) {

    fun create(
        userId: UserId,
        selectedMailLabelId: MailLabelId,
        filterUnread: Boolean,
        type: MailboxItemType,
        searchQuery: String
    ): Pager<MailboxPageKey, MailboxItem> {
        Timber.v("Paging: creating new paginator for ${selectedMailLabelId.labelId}")
        val mailboxPageKey = buildPageKey(filterUnread, selectedMailLabelId, userId, searchQuery)
        return Pager(
            config = PagingConfig(DEFAULT_PAGE_SIZE, initialLoadSize = 1),
            pagingSourceFactory = { pagingSourceFactory.create(mailboxPageKey, type) }
        )
    }

    private fun buildPageKey(
        filterUnread: Boolean,
        selectedMailLabelId: MailLabelId,
        userId: UserId,
        searchQuery: String
    ) = MailboxPageKey(
        userId = userId,
        pageKey = PageKey(
            labelId = selectedMailLabelId.labelId,
            read = if (filterUnread) ReadStatus.Unread else ReadStatus.All
        )
    )

    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }
}
