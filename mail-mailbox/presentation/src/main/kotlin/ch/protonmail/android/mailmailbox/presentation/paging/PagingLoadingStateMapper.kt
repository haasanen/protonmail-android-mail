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

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import ch.protonmail.android.mailmailbox.presentation.mailbox.MailboxScreenState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemUiModel
import ch.protonmail.android.mailmailbox.presentation.paging.exception.PaginationErrorException
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import timber.log.Timber

fun LazyPagingItems<MailboxItemUiModel>.mapToUiStates(refreshRequested: Boolean): MailboxScreenState {

    // When the user performs Pull to Refresh (refreshRequested = true), we will ignore AppendLoading state, because
    // Paging Library performs Refresh Loading --> Gets New Data --> Starts Append Loading
    if (refreshRequested) {
        return when {
            isPageLoadingNoData() -> MailboxScreenState.Loading
            isPageLoadingWithData() -> MailboxScreenState.LoadingWithData
            isPageRefreshFailed() -> refreshErrorToUiState(this)
            isPageEmpty() -> MailboxScreenState.Empty
            else -> MailboxScreenState.Data(this)
        }
    }

    // In other cases, we should give priority to AppendLoading state in order to show the loading
    // indicator at the bottom
    return when {
        isPageLoadingNoData() -> MailboxScreenState.Loading
        isPageAppendLoading() -> MailboxScreenState.AppendLoading
        // Ignore isPageLoadingWithData() case to create MailboxScreenState.LoadingWithData,
        // we do not want to display a Loading spinner at the top
        // unless user performs Pull to Refresh
        isPageAppendFailed() -> appendErrorToUiState(this)
        isPageRefreshFailed() -> refreshErrorToUiState(this)
        isPageEmpty() -> MailboxScreenState.Empty
        else -> MailboxScreenState.Data(this)
    }
}

fun appendErrorToUiState(pagingItems: LazyPagingItems<MailboxItemUiModel>): MailboxScreenState {
    val exception = (pagingItems.loadState.append as? LoadState.Error)?.error
    if (exception !is PaginationErrorException) {
        return MailboxScreenState.UnexpectedError
    }

    return when {
        exception.error is PaginationError.Offline -> MailboxScreenState.AppendOfflineError
        else -> MailboxScreenState.AppendError
    }
}

fun refreshErrorToUiState(pagingItems: LazyPagingItems<MailboxItemUiModel>): MailboxScreenState {
    val exception = (pagingItems.loadState.refresh as? LoadState.Error)?.error
    if (exception !is PaginationErrorException) {
        return MailboxScreenState.UnexpectedError
    }

    Timber.d("Refresh error being mapped to UI state: ${exception.error}")
    val listHasItems = pagingItems.itemCount > 0
    if (listHasItems) {
        return when {
            exception.error is PaginationError.Offline -> MailboxScreenState.OfflineWithData
            else -> MailboxScreenState.ErrorWithData
        }
    }

    return when {
        exception.error is PaginationError.Offline -> MailboxScreenState.Loading
        else -> MailboxScreenState.Error
    }
}


// General Loading State (When there is data)
private fun LazyPagingItems<MailboxItemUiModel>.isPageLoadingWithData(): Boolean =
    this.loadState.isLoading() && this.itemCount > 0

// General Loading State (When there is NO data)
private fun LazyPagingItems<MailboxItemUiModel>.isPageLoadingNoData(): Boolean =
    this.loadState.isLoading() && this.itemCount == 0

private fun LazyPagingItems<MailboxItemUiModel>.isPageAppendLoading(): Boolean = this.loadState.isAppendLoading()

fun LazyPagingItems<MailboxItemUiModel>.isPageAppendFailed(): Boolean = this.loadState.append is LoadState.Error

fun LazyPagingItems<MailboxItemUiModel>.isPageRefreshFailed(): Boolean = this.loadState.refresh is LoadState.Error

fun LazyPagingItems<MailboxItemUiModel>.isPageEmpty(): Boolean = this.itemCount == 0

fun LazyPagingItems<MailboxItemUiModel>.isPageInError(): Boolean = isPageRefreshFailed() || isPageAppendFailed()


// Source is Loading
private fun CombinedLoadStates.isLoading() = this.isSourceLoading()
fun CombinedLoadStates.isSourceLoading() = this.source.refresh is LoadState.Loading
private fun CombinedLoadStates.isAppendLoading() = this.append is LoadState.Loading
