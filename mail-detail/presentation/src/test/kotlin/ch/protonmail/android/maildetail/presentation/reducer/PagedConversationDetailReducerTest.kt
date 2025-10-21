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

import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.CursorId
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailconversation.domain.entity.ConversationDetailEntryPoint
import ch.protonmail.android.maildetail.presentation.model.DynamicViewPagerState
import ch.protonmail.android.maildetail.presentation.model.NavigationArgs
import ch.protonmail.android.maildetail.presentation.model.Page
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailState
import ch.protonmail.android.maillabel.domain.model.LabelId
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class PagedConversationDetailReducerTest(
    private val testName: String,
    private val testInput: TestParams.TestInput
) {

    private val pagedConversationDetailReducer = PagedConversationDetailReducer()

    @Test
    fun `should produce the expected new state`() = with(testInput) {
        val actualState = pagedConversationDetailReducer.newStateFrom(testInput.currentState, testInput.event)

        assertEquals(testInput.expectedState, actualState, testName)
    }

    companion object {

        private val readyState = PagedConversationDetailState.Ready(
            autoAdvanceEnabled = true,
            DynamicViewPagerState(
                currentPageIndex = 1,
                currentPage = Page.Conversation(CursorId(ConversationId("300"), null)),
                nextPage = Page.Conversation(CursorId(ConversationId("400"))),
                previousPage = Page.Conversation(CursorId(ConversationId("400")))
            ),
            NavigationArgs(
                singleMessageMode = false,
                LabelId("1"),
                ConversationDetailEntryPoint.Mailbox
            )
        )

        private val transitionsFromLoadingState = listOf(
            TestParams(
                "from loading to conversation data",
                TestParams.TestInput(
                    currentState = PagedConversationDetailState.Loading,
                    event = PagedConversationDetailEvent.Ready(
                        autoAdvance = true,
                        count = 3,
                        currentIndex = 1,
                        currentItem = Page.Conversation(CursorId(ConversationId("300"))),
                        nextItem = Page.Conversation(CursorId(ConversationId("400"))),
                        previousItem = Page.Conversation(CursorId(ConversationId("400"))),
                        navigationArgs = NavigationArgs(
                            singleMessageMode = false,
                            LabelId("1"),
                            ConversationDetailEntryPoint.Mailbox
                        )
                    ),
                    expectedState = PagedConversationDetailState.Ready(
                        autoAdvanceEnabled = true,
                        DynamicViewPagerState(
                            currentPageIndex = 1,
                            currentPage = Page.Conversation(CursorId(ConversationId("300"))),
                            nextPage = Page.Conversation(CursorId(ConversationId("400"))),
                            previousPage = Page.Conversation(CursorId(ConversationId("400")))
                        ),
                        navigationArgs = NavigationArgs(
                            singleMessageMode = false,
                            LabelId("1"),
                            ConversationDetailEntryPoint.Mailbox
                        )
                    )
                )
            )
        )
        private val transitionsFromDataState = listOf(
            TestParams(
                "on update page",
                TestParams.TestInput(
                    currentState = readyState,
                    event = PagedConversationDetailEvent.UpdatePage(
                        currentIndex = 2,
                        count = 4,
                        currentItem = Page.Conversation(CursorId(ConversationId("500"))),
                        nextItem = Page.Conversation(CursorId(ConversationId("600"))),
                        previousItem = Page.Conversation(CursorId(ConversationId("900")))
                    ),
                    expectedState =
                        readyState.copy(
                            dynamicViewPagerState = readyState.dynamicViewPagerState.copy(
                                currentPage = Page.Conversation(CursorId(ConversationId("500"))),
                                nextPage = Page.Conversation(CursorId(ConversationId("600"))),
                                previousPage = Page.Conversation(CursorId(ConversationId("900"))),
                                currentPageIndex = 2,
                                pageCount = 4,
                                focusPage = Effect.of(2)
                            )
                        )
                )
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = (transitionsFromLoadingState + transitionsFromDataState)
            .map { arrayOf(it.testName, it.testInput) }
    }

    data class TestParams(
        val testName: String,
        val testInput: TestInput
    ) {

        data class TestInput(
            val currentState: PagedConversationDetailState,
            val event: PagedConversationDetailEvent,
            val expectedState: PagedConversationDetailState
        )
    }
}
