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

package ch.protonmail.android.mailcomposer.presentation.ui.util

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Velocity
import timber.log.Timber

/**
 * EdgeBoundedNestedScrollConnection
 *
 * Problem:
 *
 * When the plain-text content inside a BasicTextField is long and the cursor is
 * positioned at the top or bottom edge, fast reverse scrolling (e.g., trying to
 * scroll further down while already at the bottom) causes BasicTextField to emit
 * nested-scroll deltas even though it cannot consume them internally.
 *
 * These unconsumed deltas propagate to the parent scroll container, producing a
 * reverse-direction fling, which can suddenly scroll the entire composer
 * all the way to the opposite end (top or bottom).
 *
 * Reproduction steps:
 * 1. Scroll BasicTextField to the bottom.
 * 2. Drag upward quickly (trying to scroll even further down).
 * 3. BasicTextField fails to consume the gesture, bubbles it up, and the parent
 *    scroll interprets it as a valid fling → jumps upward unexpectedly.
 *
 * Solution:
 *
 * This NestedScrollConnection inspects incoming fling velocities and blocks
 * reverse fling events when the ScrollState is already at its edge
 *
 */
class EdgeGuardNestedScrollConnection(
    private val scrollState: ScrollState
) : NestedScrollConnection {

    override suspend fun onPreFling(available: Velocity): Velocity {
        val vy = available.y
        val atTop = scrollState.value <= 0
        val atBottom = scrollState.value >= scrollState.maxValue

        return when {
            atTop && vy < 0f -> {
                Timber.d("EdgeGuard: blocking reverse fling at TOP vy=$vy value=${scrollState.value}")
                available
            }

            atBottom && vy > 0f -> {
                Timber.d("EdgeGuard: blocking reverse fling at BOTTOM vy=$vy value=${scrollState.value}")
                available
            }

            else -> Velocity.Zero
        }
    }
}
