/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailpagination.data.extension

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailpagination.data.model.AppendEvent
import ch.protonmail.android.mailpagination.data.model.PagingEvent
import ch.protonmail.android.mailpagination.data.model.RefreshEvent
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

fun <T> Flow<PagingEvent<T>>.filterAppendEvents(): Flow<AppendEvent<T>> = this.filter { event ->
    when (event) {
        is PagingEvent.Append -> true
        is PagingEvent.Error -> true
        else -> false
    }
}.map { event ->
    when (event) {
        is PagingEvent.Append -> AppendEvent.Append(event.items)
        is PagingEvent.Error -> AppendEvent.Error(event.error)
        else -> throw IllegalStateException("Illegal event type: \"$event\" for Append Event")
    }
}

suspend fun <T> Flow<AppendEvent<T>>.appendEventToEither(): Either<PaginationError, List<T>> = this
    .first()
    .let {
        when (it) {
            is AppendEvent.Append -> it.items.right()
            is AppendEvent.Error -> it.error.left()
        }
    }

fun <T> Flow<PagingEvent<T>>.filterRefreshEvents(): Flow<RefreshEvent<T>> = this.filter { event ->
    when (event) {
        is PagingEvent.Refresh -> true
        is PagingEvent.Error -> true
        else -> false
    }
}.map { event ->
    when (event) {
        is PagingEvent.Refresh -> RefreshEvent.Refresh(event.items)
        is PagingEvent.Error -> RefreshEvent.Error(event.error)
        else -> throw IllegalStateException("Illegal event type: \"$event\" for Refresh Event")
    }
}

suspend fun <T> Flow<RefreshEvent<T>>.refreshEventToEither(): Either<PaginationError, List<T>> = this
    .first()
    .let {
        when (it) {
            is RefreshEvent.Refresh -> it.items.right()
            is RefreshEvent.Error -> it.error.left()
        }
    }
