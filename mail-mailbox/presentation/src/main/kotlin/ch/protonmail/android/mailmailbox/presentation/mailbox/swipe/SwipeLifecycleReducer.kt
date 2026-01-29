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

package ch.protonmail.android.mailmailbox.presentation.mailbox.swipe

import androidx.compose.material3.SwipeToDismissBoxValue

internal object SwipeLifecycleReducer {

    fun reduce(state: SwipeLifecycleState, event: SwipeLifecycleEvent): SwipeLifecycleState {
        return when (event) {
            SwipeLifecycleEvent.SwipingDisabled -> SwipeLifecycleState.Idle

            is SwipeLifecycleEvent.GestureStarted -> {
                SwipeLifecycleState.Swiping(direction = event.direction)
            }

            is SwipeLifecycleEvent.DirectionChanged -> {
                val direction = event.direction
                when (state) {
                    SwipeLifecycleState.Idle -> {
                        if (direction == SwipeToDismissBoxValue.Settled) state
                        else SwipeLifecycleState.Swiping(direction)
                    }

                    is SwipeLifecycleState.Swiping -> {
                        if (direction == SwipeToDismissBoxValue.Settled) SwipeLifecycleState.Idle
                        else state.copy(direction = direction)
                    }

                    is SwipeLifecycleState.Armed -> {
                        // Freeze direction once armed
                        if (direction == SwipeToDismissBoxValue.Settled) SwipeLifecycleState.Idle else state
                    }

                    is SwipeLifecycleState.ReadyToExecute -> {
                        // Ignore direction changes until effect runs.
                        state
                    }
                }
            }

            is SwipeLifecycleEvent.ThresholdReached -> {
                val direction = event.direction
                when (state) {
                    is SwipeLifecycleState.Swiping -> {
                        if (direction == SwipeToDismissBoxValue.Settled) state
                        else SwipeLifecycleState.Armed(direction = direction)
                    }

                    else -> state
                }
            }

            is SwipeLifecycleEvent.ThresholdRevoked -> {
                // User swiped back under threshold
                when (state) {
                    is SwipeLifecycleState.Armed -> SwipeLifecycleState.Swiping(direction = state.direction)
                    else -> state
                }
            }

            SwipeLifecycleEvent.PointerReleased -> {
                when (state) {
                    is SwipeLifecycleState.Armed -> SwipeLifecycleState.ReadyToExecute(direction = state.direction)
                    else -> SwipeLifecycleState.Idle
                }
            }

            SwipeLifecycleEvent.ActionExecuted -> SwipeLifecycleState.Idle
        }
    }
}

