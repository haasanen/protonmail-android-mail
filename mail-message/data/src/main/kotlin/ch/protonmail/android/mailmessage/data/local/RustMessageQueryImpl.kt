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

import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import javax.inject.Inject

class RustMessageQueryImpl @Inject constructor(
    private val invalidationTracker: RustInvalidationTracker,
    private val messagePaginatorManager: MessagePaginatorManager
) : RustMessageQuery {

    private val messagesUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.d("rust-message: messages updated, invalidating pagination...")

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.MESSAGE, RustDataSourceId.LABELS
                )
            )
        }
    }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata>? {
        val paginator = messagePaginatorManager.getOrCreatePaginator(userId, pageKey, messagesUpdatedCallback)
            .getOrNull()

        Timber.v("rust-message: Paging: querying ${pageKey.pageToLoad.name} page for messages")
        val messages = when (pageKey.pageToLoad) {
            PageToLoad.First -> paginator?.nextPage()
            PageToLoad.Next -> paginator?.nextPage()
            PageToLoad.All -> paginator?.reload()
        }?.getOrElse { emptyList() }

        Timber.v("rust-message: init value for messages is $messages")
        return messages
    }
}
