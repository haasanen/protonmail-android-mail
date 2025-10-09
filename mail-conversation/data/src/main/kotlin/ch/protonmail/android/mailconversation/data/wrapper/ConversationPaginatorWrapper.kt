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

package ch.protonmail.android.mailconversation.data.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailpagination.data.mapper.toPaginationError
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import uniffi.proton_mail_uniffi.ConversationScroller
import uniffi.proton_mail_uniffi.ConversationScrollerFetchMoreResult
import uniffi.proton_mail_uniffi.ConversationScrollerGetItemsResult

class ConversationPaginatorWrapper(private val rustPaginator: ConversationScroller) {

    val supportsIncludeFilter = rustPaginator.supportsIncludeFilter()

    suspend fun nextPage(): Either<PaginationError, Unit> = when (val result = rustPaginator.fetchMore()) {
        is ConversationScrollerFetchMoreResult.Error -> result.v1.toPaginationError().left()
        is ConversationScrollerFetchMoreResult.Ok -> Unit.right()
    }

    suspend fun reload(): Either<PaginationError, Unit> = when (val result = rustPaginator.getItems()) {
        is ConversationScrollerGetItemsResult.Error -> result.v1.toPaginationError().left()
        is ConversationScrollerGetItemsResult.Ok -> Unit.right()
    }

    fun disconnect() {
        rustPaginator.handle().disconnect()
        rustPaginator.terminate()
    }
}
