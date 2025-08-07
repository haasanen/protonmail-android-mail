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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesPaginator
import ch.protonmail.android.mailmessage.data.usecase.CreateRustSearchPaginator
import ch.protonmail.android.mailmessage.data.wrapper.MessagePaginatorWrapper
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailsession.data.mapper.toLocalUserId
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.MessageScrollerLiveQueryCallback
import javax.inject.Inject

class MessagePaginatorManager @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustMessagesPaginator: CreateRustMessagesPaginator,
    private val createRustSearchPaginator: CreateRustSearchPaginator
) {

    private var paginator: MessagePaginatorWrapper? = null
    private val paginatorMutex = Mutex()

    fun getPaginator(): MessagePaginatorWrapper? = paginator

    suspend fun getOrCreatePaginator(
        userId: UserId,
        pageKey: PageKey,
        callback: MessageScrollerLiveQueryCallback,
        onNewPaginator: suspend () -> Unit
    ): Either<DataError, MessagePaginatorWrapper> = paginatorMutex.withLock {
        if (!shouldInitPaginator(userId, pageKey)) {
            Timber.d("rust-paginator: reusing existing paginator instance...")
            return paginator.getOrError()
        }

        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-action-with-user-session: Failed to perform action, null user session")
            return DataError.Local.NoUserSession.left()
        }

        destroy()
        return when (pageKey) {
            is PageKey.DefaultPageKey -> createDefaultPaginator(session, pageKey, callback)
            is PageKey.PageKeyForSearch -> createSearchPaginator(session, pageKey, callback)
        }.onRight {
            paginator = it
            onNewPaginator()
        }
    }

    private suspend fun createDefaultPaginator(
        session: MailUserSessionWrapper,
        pageKey: PageKey.DefaultPageKey,
        callback: MessageScrollerLiveQueryCallback
    ): Either<DataError, MessagePaginatorWrapper> {
        val labelId = pageKey.labelId.toLocalLabelId()
        val unread = pageKey.readStatus == ReadStatus.Unread

        return createRustMessagesPaginator(session, labelId, unread, callback)
    }

    private suspend fun createSearchPaginator(
        session: MailUserSessionWrapper,
        pageKey: PageKey.PageKeyForSearch,
        callback: MessageScrollerLiveQueryCallback
    ): Either<DataError, MessagePaginatorWrapper> {
        val keyword = pageKey.keyword

        return createRustSearchPaginator(session, keyword, callback)
    }

    private fun destroy() {
        Timber.d("rust-paginator: destroy")
        paginator?.destroy()
        paginator = null
    }

    private fun shouldInitPaginator(userId: UserId, pageKey: PageKey) = when (pageKey) {
        is PageKey.DefaultPageKey -> {
            val unread = pageKey.readStatus == ReadStatus.Unread
            paginator == null ||
                paginator?.params?.userId != userId.toLocalUserId() ||
                paginator?.params?.labelId != pageKey.labelId.toLocalLabelId() ||
                paginator?.params?.unread != unread ||
                pageKey.pageToLoad == PageToLoad.First
        }
        is PageKey.PageKeyForSearch -> {
            val keyword = pageKey.keyword
            paginator == null ||
                paginator?.params?.userId != userId.toLocalUserId() ||
                paginator?.params?.keyword != keyword
        }
    }

    private fun MessagePaginatorWrapper?.getOrError(): Either<DataError, MessagePaginatorWrapper> =
        this?.right() ?: DataError.Local.NoDataCached.left()

}
