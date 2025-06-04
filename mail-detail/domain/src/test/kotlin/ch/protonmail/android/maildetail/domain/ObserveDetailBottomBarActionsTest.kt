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

package ch.protonmail.android.maildetail.domain

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.ObserveAllConversationBottomBarActions
import ch.protonmail.android.maildetail.domain.usecase.ObserveDetailBottomBarActions
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class ObserveDetailBottomBarActionsTest {

    private val observeAllConversationBottomBarActions = mockk<ObserveAllConversationBottomBarActions>()

    private val observeDetailActions = ObserveDetailBottomBarActions(observeAllConversationBottomBarActions)

    @Test
    fun `returns visible bottom bar actions when use case succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Invoices
        val allActions = AllBottomBarActions(
            hiddenActions = listOf(Action.Star, Action.Label),
            visibleActions = listOf(Action.Spam, Action.Archive)
        )
        coEvery {
            observeAllConversationBottomBarActions(userId, labelId, conversationId)
        } returns flowOf(allActions.right())

        // When
        observeDetailActions(userId, labelId, conversationId).test {
            // Then
            val expected = listOf(Action.Spam, Action.Archive)
            assertEquals(expected.right(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns updated bottom bar actions when use new data is available`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Invoices

        val flow = MutableSharedFlow<Either<DataError, AllBottomBarActions>>()

        coEvery {
            observeAllConversationBottomBarActions(userId, labelId, conversationId)
        } returns flow

        val actionsSet = AllBottomBarActions(
            visibleActions = listOf(Action.Spam, Action.Archive),
            hiddenActions = listOf(Action.Star, Action.Label)
        )

        val firstExpectedSet = listOf(Action.Spam, Action.Archive)

        val finalActionsSet = AllBottomBarActions(
            visibleActions = listOf(Action.Star, Action.Label),
            hiddenActions = listOf(Action.Spam, Action.Archive)
        )

        val finalExpectedSet = listOf(Action.Star, Action.Label)

        // When + Then
        observeDetailActions(userId, labelId, conversationId).test {
            flow.emit(actionsSet.right())
            assertEquals(firstExpectedSet.right(), awaitItem())

            flow.emit(finalActionsSet.right())
            assertEquals(finalExpectedSet.right(), awaitItem())
        }
    }

    @Test
    fun `returns error when failing to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Invoices
        val error = DataError.Local.Unknown.left()
        coEvery { observeDetailActions(userId, labelId, conversationId) } returns flowOf(error)

        // When
        observeDetailActions(userId, labelId, conversationId).test {
            // Then
            assertEquals(error, awaitItem())
            awaitComplete()
        }
    }
}
