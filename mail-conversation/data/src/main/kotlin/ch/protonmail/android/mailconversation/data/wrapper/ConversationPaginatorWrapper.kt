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
import uniffi.proton_mail_uniffi.Conversation
import uniffi.proton_mail_uniffi.ConversationScroller
import uniffi.proton_mail_uniffi.ConversationScrollerAllItemsResult
import uniffi.proton_mail_uniffi.ConversationScrollerFetchMoreResult

class ConversationPaginatorWrapper(private val rustPaginator: ConversationScroller) {

    suspend fun nextPage(): Either<PaginationError, List<Conversation>> =
        when (val result = rustPaginator.fetchMore()) {
            is ConversationScrollerFetchMoreResult.Error -> result.v1.toPaginationError().left()
            is ConversationScrollerFetchMoreResult.Ok -> result.v1.right()
        }

    suspend fun reload(): Either<PaginationError, List<Conversation>> = when (val result = rustPaginator.allItems()) {
        is ConversationScrollerAllItemsResult.Error -> result.v1.toPaginationError().left()
        is ConversationScrollerAllItemsResult.Ok -> result.v1.right()
    }

    fun disconnect() {
        rustPaginator.handle().disconnect()
    }
}
