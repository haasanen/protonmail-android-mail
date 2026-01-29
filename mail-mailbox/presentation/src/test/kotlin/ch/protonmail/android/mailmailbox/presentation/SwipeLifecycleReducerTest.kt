/*
 * Copyright (c) 2026 Proton Technologies AG
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

import androidx.compose.material3.SwipeToDismissBoxValue
import ch.protonmail.android.mailmailbox.presentation.mailbox.swipe.SwipeLifecycleEvent
import ch.protonmail.android.mailmailbox.presentation.mailbox.swipe.SwipeLifecycleReducer
import ch.protonmail.android.mailmailbox.presentation.mailbox.swipe.SwipeLifecycleState
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SwipeLifecycleReducerTest {

    @Test
    fun `SwipingDisabled always returns Idle`() {
        // Given
        val initialStates = listOf(
            SwipeLifecycleState.Idle,
            SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.StartToEnd),
            SwipeLifecycleState.Armed(SwipeToDismissBoxValue.EndToStart),
            SwipeLifecycleState.ReadyToExecute(SwipeToDismissBoxValue.StartToEnd)
        )

        // When & Then
        initialStates.forEach { state ->
            val result = SwipeLifecycleReducer.reduce(state, SwipeLifecycleEvent.SwipingDisabled)
            assertEquals(SwipeLifecycleState.Idle, result)
        }
    }

    @Test
    fun `GestureStarted returns Swiping with provided direction`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Idle,
            event = SwipeLifecycleEvent.GestureStarted(direction = SwipeToDismissBoxValue.StartToEnd)
        )

        // Then
        assertEquals(SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.StartToEnd), result)
    }

    @Test
    fun `DirectionChanged from Idle with Settled stays Idle`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Idle,
            event = SwipeLifecycleEvent.DirectionChanged(SwipeToDismissBoxValue.Settled)
        )

        // Then
        assertEquals(SwipeLifecycleState.Idle, result)
    }

    @Test
    fun `DirectionChanged from Idle with non-Settled direction becomes Swiping`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Idle,
            event = SwipeLifecycleEvent.DirectionChanged(SwipeToDismissBoxValue.EndToStart)
        )

        // Then
        assertEquals(SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.EndToStart), result)
    }

    @Test
    fun `DirectionChanged while Swiping to non-Settled updates direction`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.StartToEnd),
            event = SwipeLifecycleEvent.DirectionChanged(SwipeToDismissBoxValue.EndToStart)
        )

        // Then
        assertEquals(SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.EndToStart), result)
    }

    @Test
    fun `DirectionChanged while Swiping to Settled returns Idle`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.StartToEnd),
            event = SwipeLifecycleEvent.DirectionChanged(SwipeToDismissBoxValue.Settled)
        )

        // Then
        assertEquals(SwipeLifecycleState.Idle, result)
    }

    @Test
    fun `DirectionChanged while Armed freezes direction and ignores non-Settled changes`() {
        // Given
        val initial = SwipeLifecycleState.Armed(SwipeToDismissBoxValue.StartToEnd)

        // When
        val result = SwipeLifecycleReducer.reduce(
            state = initial,
            event = SwipeLifecycleEvent.DirectionChanged(SwipeToDismissBoxValue.EndToStart)
        )

        // Then
        assertEquals(initial, result)
    }

    @Test
    fun `DirectionChanged while Armed to Settled returns Idle`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Armed(SwipeToDismissBoxValue.StartToEnd),
            event = SwipeLifecycleEvent.DirectionChanged(SwipeToDismissBoxValue.Settled)
        )

        // Then
        assertEquals(SwipeLifecycleState.Idle, result)
    }

    @Test
    fun `DirectionChanged while ReadyToExecute is ignored`() {
        // Given
        val initial = SwipeLifecycleState.ReadyToExecute(SwipeToDismissBoxValue.StartToEnd)

        // When
        val result = SwipeLifecycleReducer.reduce(
            state = initial,
            event = SwipeLifecycleEvent.DirectionChanged(SwipeToDismissBoxValue.EndToStart)
        )

        // Then
        assertEquals(initial, result)
    }

    @Test
    fun `ThresholdReached while Swiping with non-Settled arms with that direction`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.StartToEnd),
            event = SwipeLifecycleEvent.ThresholdReached(SwipeToDismissBoxValue.StartToEnd)
        )

        // Then
        assertEquals(SwipeLifecycleState.Armed(SwipeToDismissBoxValue.StartToEnd), result)
    }

    @Test
    fun `ThresholdReached is ignored when not Swiping`() {
        // Given
        val initialStates = listOf(
            SwipeLifecycleState.Idle,
            SwipeLifecycleState.Armed(SwipeToDismissBoxValue.StartToEnd),
            SwipeLifecycleState.ReadyToExecute(SwipeToDismissBoxValue.EndToStart)
        )

        // When & Then
        initialStates.forEach { state ->
            val result = SwipeLifecycleReducer.reduce(
                state = state,
                event = SwipeLifecycleEvent.ThresholdReached(SwipeToDismissBoxValue.StartToEnd)
            )
            assertEquals(state, result)
        }
    }

    @Test
    fun `ThresholdRevoked while Armed transitions back to Swiping`() {
        // Given
        val initial = SwipeLifecycleState.Armed(SwipeToDismissBoxValue.StartToEnd)

        // When
        val result = SwipeLifecycleReducer.reduce(
            state = initial,
            event = SwipeLifecycleEvent.ThresholdRevoked(SwipeToDismissBoxValue.EndToStart)
        )

        // Then
        assertEquals(SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.StartToEnd), result)
    }

    @Test
    fun `PointerReleased while Armed transitions to ReadyToExecute keeping direction`() {
        // Given & When
        val result = SwipeLifecycleReducer.reduce(
            state = SwipeLifecycleState.Armed(SwipeToDismissBoxValue.EndToStart),
            event = SwipeLifecycleEvent.PointerReleased
        )

        // Then
        assertEquals(SwipeLifecycleState.ReadyToExecute(SwipeToDismissBoxValue.EndToStart), result)
    }

    @Test
    fun `PointerReleased returns Idle when not Armed`() {
        // Given
        val initialStates = listOf(
            SwipeLifecycleState.Idle,
            SwipeLifecycleState.Swiping(SwipeToDismissBoxValue.StartToEnd),
            SwipeLifecycleState.ReadyToExecute(SwipeToDismissBoxValue.EndToStart)
        )

        // When & Then
        initialStates.forEach { state ->
            val result = SwipeLifecycleReducer.reduce(state, SwipeLifecycleEvent.PointerReleased)
            assertEquals(SwipeLifecycleState.Idle, result)
        }
    }

    @Test
    fun `ActionExecuted returns Idle`() {
        // Given
        val initial = SwipeLifecycleState.ReadyToExecute(SwipeToDismissBoxValue.StartToEnd)

        // When
        val result = SwipeLifecycleReducer.reduce(
            state = initial,
            event = SwipeLifecycleEvent.ActionExecuted
        )

        // Then
        assertEquals(SwipeLifecycleState.Idle, result)

    }
}
