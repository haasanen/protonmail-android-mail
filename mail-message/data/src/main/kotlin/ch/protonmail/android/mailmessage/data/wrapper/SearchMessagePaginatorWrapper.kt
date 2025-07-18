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

package ch.protonmail.android.mailmessage.data.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailmessage.data.model.PaginatorParams
import ch.protonmail.android.mailpagination.data.mapper.toPaginationError
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import uniffi.proton_mail_uniffi.SearchScroller
import uniffi.proton_mail_uniffi.SearchScrollerFetchMoreResult
import uniffi.proton_mail_uniffi.SearchScrollerRefreshResult

class SearchMessagePaginatorWrapper(
    private val rustPaginator: SearchScroller,
    override val params: PaginatorParams
) : MessagePaginatorWrapper {

    override suspend fun nextPage(): Either<PaginationError, Unit> = when (val result = rustPaginator.fetchMore()) {
        is SearchScrollerFetchMoreResult.Error -> result.v1.toPaginationError().left()
        is SearchScrollerFetchMoreResult.Ok -> Unit.right()
    }

    override suspend fun reload(): Either<PaginationError, Unit> = when (val result = rustPaginator.refresh()) {
        is SearchScrollerRefreshResult.Error -> result.v1.toPaginationError().left()
        is SearchScrollerRefreshResult.Ok -> Unit.right()
    }

    override fun destroy() {
        rustPaginator.handle().disconnect()
    }
}
