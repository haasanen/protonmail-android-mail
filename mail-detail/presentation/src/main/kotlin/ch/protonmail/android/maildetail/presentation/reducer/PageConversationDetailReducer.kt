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

package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.maildetail.presentation.model.DynamicViewPagerState
import ch.protonmail.android.maildetail.presentation.model.Page
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailState.Ready
import ch.protonmail.android.maildetail.presentation.model.addPage
import ch.protonmail.android.maildetail.presentation.model.currentPage
import ch.protonmail.android.maildetail.presentation.model.nextPage
import javax.inject.Inject

class PagedConversationDetailReducer @Inject constructor() {

    internal fun newStateFrom(
        currentState: PagedConversationDetailState,
        event: PagedConversationDetailEvent
    ): PagedConversationDetailState {
        return when (event) {
            is PagedConversationDetailEvent.Error -> {
                PagedConversationDetailState.Error
            }

            is PagedConversationDetailEvent.Ready -> Ready(
                event.autoAdvance,
                DynamicViewPagerState(
                    pages = mutableListOf<Page>()
                        .addPage(event.previousItem)
                        .addPage(event.currentItem)
                        .addPage(event.nextItem)
                ).setFocusIndexes(),
                navigationArgs = event.navigationArgs
            )

            is PagedConversationDetailEvent.UpdatePage -> reducePagerState(currentState) {
                reduceUpdatePage(
                    it,
                    event
                )
            }

            PagedConversationDetailEvent.PageChanging -> reducePagerState(currentState) {
                it.copy(userScrollEnabled = false)
            }

            PagedConversationDetailEvent.ClearFocusPage -> reducePagerState(currentState) {
                it.copy(focusPageIndex = null)
            }

            PagedConversationDetailEvent.AutoAdvanceRequested -> reducePagerState(currentState) {
                reduceAutoAdvanceRequested(it)
            }
        }
    }
}

private fun reduceAutoAdvanceRequested(currentState: DynamicViewPagerState): DynamicViewPagerState {
    return if (currentState.nextPage() != null) {
        currentState.copy(
            scrollToPage = Effect.of(Unit),
            pendingRemoval = currentState.currentPage(),
            userScrollEnabled = false
        )
    } else {
        return currentState.copy(exit = Effect.of(Unit))
    }
}

private fun reduceUpdatePage(
    currentState: DynamicViewPagerState,
    event: PagedConversationDetailEvent.UpdatePage
): DynamicViewPagerState = currentState.copy(
    pages = mutableListOf<Page>()
        .addPage(event.previousItem)
        .addPage(event.currentItem)
        .addPage(event.nextItem),
    userScrollEnabled = true,
    pendingRemoval = null
).setFocusIndexes()

private fun reducePagerState(
    currentState: PagedConversationDetailState,
    block: (currentPagerState: DynamicViewPagerState) -> DynamicViewPagerState
) = when (currentState) {
    is Ready ->
        currentState.copy(dynamicViewPagerState = block(currentState.dynamicViewPagerState))

    else -> {
        PagedConversationDetailState.Error
    }
}

private fun DynamicViewPagerState.setFocusIndexes(): DynamicViewPagerState {
    val newFocusIndex = if (this.pages.size < 3) 0 else 1
    return this.copy(
        currentPageIndex = newFocusIndex,
        focusPageIndex = newFocusIndex
    )
}
