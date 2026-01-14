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

import java.util.concurrent.atomic.AtomicBoolean
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalConversationId
import ch.protonmail.android.mailpagination.data.mapper.toPaginationError
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import timber.log.Timber
import uniffi.proton_mail_uniffi.ConversationScroller
import uniffi.proton_mail_uniffi.ConversationScrollerCursorResult
import uniffi.proton_mail_uniffi.ConversationScrollerFetchMoreResult
import uniffi.proton_mail_uniffi.ConversationScrollerGetItemsResult
import uniffi.proton_mail_uniffi.ConversationScrollerSupportsIncludeFilterResult
import uniffi.proton_mail_uniffi.IncludeSwitch
import uniffi.proton_mail_uniffi.ReadFilter

class ConversationPaginatorWrapper(private val rustPaginator: ConversationScroller) {

    private val disconnected = AtomicBoolean(false)

    val isDisconnected: Boolean
        get() = disconnected.get()

    suspend fun supportsIncludeFilter(): Boolean {
        if (isDisconnected) return false

        return when (val result = rustPaginator.supportsIncludeFilter()) {
            is ConversationScrollerSupportsIncludeFilterResult.Error -> {
                Timber.w("conversation-paginator: failed to define supportsIncludeFilter: $result")
                false
            }
            is ConversationScrollerSupportsIncludeFilterResult.Ok -> result.v1
        }
    }

    suspend fun nextPage(): Either<PaginationError, Unit> {
        if (isDisconnected) return PaginationError.PaginatorAlreadyTerminated.left()

        return when (val result = rustPaginator.fetchMore()) {
            is ConversationScrollerFetchMoreResult.Error -> result.v1.toPaginationError().left()
            is ConversationScrollerFetchMoreResult.Ok -> Unit.right()
        }
    }

    suspend fun reload(): Either<PaginationError, Unit> {
        if (isDisconnected) return PaginationError.PaginatorAlreadyTerminated.left()

        return when (val result = rustPaginator.getItems()) {
            is ConversationScrollerGetItemsResult.Error -> result.v1.toPaginationError().left()
            is ConversationScrollerGetItemsResult.Ok -> Unit.right()
        }
    }

    suspend fun getCursor(conversationId: LocalConversationId): Either<PaginationError, ConversationCursorWrapper> {
        if (isDisconnected) return PaginationError.PaginatorAlreadyTerminated.left()

        return when (val result = rustPaginator.cursor(conversationId)) {
            is ConversationScrollerCursorResult.Error -> result.v1.toPaginationError().left()
            is ConversationScrollerCursorResult.Ok -> ConversationCursorWrapper(result.v1).right()
        }
    }

    fun filterUnread(filterUnread: Boolean) {
        if (isDisconnected) return

        val filter = if (filterUnread) ReadFilter.UNREAD else ReadFilter.ALL
        rustPaginator.changeFilter(filter)
    }

    fun showSpamAndTrash(show: Boolean) {
        if (isDisconnected) return

        val includeSwitch = if (show) IncludeSwitch.WITH_SPAM_AND_TRASH else IncludeSwitch.DEFAULT
        rustPaginator.changeInclude(includeSwitch)
    }

    fun disconnect() {
        if (!isDisconnected) {
            Timber.d("conversation-paginator: disconnecting scroller")
            rustPaginator.handle().disconnect()
            rustPaginator.terminate()

            disconnected.set(true)
        } else {
            Timber.d("conversation-paginator: scroller already disconnected")
        }
    }
}
