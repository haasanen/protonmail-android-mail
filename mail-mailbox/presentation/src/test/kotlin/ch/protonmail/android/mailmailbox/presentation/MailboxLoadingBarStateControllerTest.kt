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
package ch.protonmail.android.mailmailbox.presentation

import app.cash.turbine.test
import ch.protonmail.android.mailmailbox.domain.model.MailboxFetchNewStatus
import ch.protonmail.android.mailmailbox.domain.model.ScrollerType
import ch.protonmail.android.mailmailbox.presentation.mailbox.MailboxLoadingBarStateController
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.LoadingBarUiState
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MailboxLoadingBarStateControllerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when Started event is received, state becomes Show`() = runTest {
        // Given
        val controller = MailboxLoadingBarStateController(this)

        // When
        controller.observeState().test {
            // Then
            assertEquals(LoadingBarUiState.Hide, awaitItem())

            // When
            controller.onMailboxFetchNewStatus(
                MailboxFetchNewStatus.Started(timestampMs = 0L, ScrollerType.Conversation)
            )

            // Then
            val show = awaitItem() as LoadingBarUiState.Show
            assertEquals(MailboxLoadingBarStateController.DEFAULT_CYCLE_MS, show.cycleDurationMs)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when End is received before 1 cycle completes then Hide emitted after remaining time`() = runTest {
        // Given
        val controller = MailboxLoadingBarStateController(this)

        controller.observeState().test {
            awaitItem()

            // When
            controller.onMailboxFetchNewStatus(
                MailboxFetchNewStatus.Started(timestampMs = 0L, ScrollerType.Conversation)
            )
            awaitItem()

            controller.onMailboxFetchNewStatus(
                MailboxFetchNewStatus.Ended(timestampMs = 1000L, ScrollerType.Conversation)
            )

            // Then
            advanceTimeBy(990)
            expectNoEvents()

            advanceTimeBy(20)
            assertEquals(LoadingBarUiState.Hide, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when End arrives near cycle boundary then extra cycle is added`() = runTest {
        // Given
        val controller = MailboxLoadingBarStateController(this)
        val cycle = MailboxLoadingBarStateController.DEFAULT_CYCLE_MS
        val tolerance = MailboxLoadingBarStateController.DEFAULT_TOLERANCE_MS

        controller.observeState().test {
            awaitItem()

            // When
            controller.onMailboxFetchNewStatus(
                MailboxFetchNewStatus.Started(timestampMs = 0L, ScrollerType.Conversation)
            )
            awaitItem()

            // End at cycle - tolerance + 10ms (near-boundary logic)
            val endTs = cycle - tolerance + 10
            controller.onMailboxFetchNewStatus(
                MailboxFetchNewStatus.Ended(timestampMs = endTs, ScrollerType.Conversation)
            )

            // Then
            // Required cycles = base 1 + extra → 2 cycles
            advanceTimeBy(cycle - endTs)
            expectNoEvents()

            advanceTimeBy(cycle)
            assertEquals(LoadingBarUiState.Hide, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when End arrives after more than 1 cycle then hide is delayed to next full cycle`() = runTest {
        // Given
        val controller = MailboxLoadingBarStateController(this)

        controller.observeState().test {
            awaitItem()

            // When
            controller.onMailboxFetchNewStatus(
                MailboxFetchNewStatus.Started(timestampMs = 0L, ScrollerType.Conversation)
            )
            awaitItem()

            // End at 2100ms → 2 cycles = 4000ms
            controller.onMailboxFetchNewStatus(
                MailboxFetchNewStatus.Ended(timestampMs = 2100L, ScrollerType.Conversation)
            )

            // Then
            advanceTimeBy(1890)
            expectNoEvents()

            advanceTimeBy(20)
            assertEquals(LoadingBarUiState.Hide, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when Start followed by immediate End then Hide occurs at cycle end`() = runTest {
        // Given
        val controller = MailboxLoadingBarStateController(this)
        val cycle = MailboxLoadingBarStateController.DEFAULT_CYCLE_MS

        controller.observeState().test {
            awaitItem()

            // When
            controller.onMailboxFetchNewStatus(MailboxFetchNewStatus.Started(0, ScrollerType.Conversation))
            awaitItem()

            controller.onMailboxFetchNewStatus(MailboxFetchNewStatus.Ended(0, ScrollerType.Conversation))

            // Then
            advanceTimeBy(cycle - 10)
            expectNoEvents()

            advanceTimeBy(20)
            assertEquals(LoadingBarUiState.Hide, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
