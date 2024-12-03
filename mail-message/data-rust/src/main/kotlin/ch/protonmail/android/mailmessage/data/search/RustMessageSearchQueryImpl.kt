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

package ch.protonmail.android.mailmessage.data.search

import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.data.usecase.CreateRustSearchPaginator
import ch.protonmail.android.mailmessage.data.wrapper.MessagePaginatorWrapper
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LiveQueryCallback
import javax.inject.Inject

class RustMessageSearchQueryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val invalidationTracker: RustInvalidationTracker,
    private val createRustSearchPaginator: CreateRustSearchPaginator,
    private val rustMailbox: RustMailbox
) : RustMessageSearchQuery {

    private var paginator: Paginator? = null
    private val paginatorMutex = Mutex()

    private val messagesUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            Timber.d("rust-search: messages updated, invalidating pagination...")

            invalidationTracker.notifyInvalidation(
                setOf(
                    RustDataSourceId.MESSAGE,
                    RustDataSourceId.LABELS
                )
            )
        }
    }

    override suspend fun getMessages(userId: UserId, pageKey: PageKey.PageKeyForSearch): List<LocalMessageMetadata>? {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-search: trying to load message with a null session")
            return null
        }
        Timber.v("rust-search: got MailSession instance to watch messages for $userId")

        initPaginator(userId, session, pageKey.keyword)

        Timber.v("rust-search: Paging: querying ${pageKey.pageToLoad.name} page for messages")
        val messages = when (pageKey.pageToLoad) {
            PageToLoad.First -> paginator?.searchPaginator?.nextPage()
            PageToLoad.Next -> paginator?.searchPaginator?.nextPage()
            PageToLoad.All -> paginator?.searchPaginator?.reload()
        }

        Timber.v("rust-search: init value for messages is $messages")
        return messages
    }

    private suspend fun initPaginator(
        userId: UserId,
        session: MailUserSessionWrapper,
        keyword: String
    ) = paginatorMutex.withLock {
        if (!shouldInitPaginator(userId, keyword)) {
            Timber.v("rust-search: reusing existing paginator instance...")
            return
        }
        Timber.v("rust-search: [destroy and] initialize paginator instance...")
        destroy()

        // Switch to all mail location
        rustMailbox.switchToAllMailMailbox(userId)

        Timber.v("rust-search: searching for $keyword")
        paginator = Paginator(
            createRustSearchPaginator(session, keyword, messagesUpdatedCallback),
            userId,
            keyword
        )
    }

    private fun destroy() {
        Timber.d("rust-message-query: destroy")
        paginator?.searchPaginator?.destroy()
        paginator = null
    }

    private fun shouldInitPaginator(userId: UserId, keyword: String) =
        paginator == null || paginator?.userId != userId || paginator?.keyword != keyword

    private data class Paginator(val searchPaginator: MessagePaginatorWrapper, val userId: UserId, val keyword: String)
}
