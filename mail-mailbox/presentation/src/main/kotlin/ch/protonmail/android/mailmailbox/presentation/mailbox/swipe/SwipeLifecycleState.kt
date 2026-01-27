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

internal sealed interface SwipeLifecycleState {
    val direction: SwipeToDismissBoxValue

    val scaleTarget: Float
        get() = if (this is Armed || this is ReadyToExecute) 1f else 0.75f

    data object Idle : SwipeLifecycleState {
        override val direction: SwipeToDismissBoxValue = SwipeToDismissBoxValue.Settled
    }

    data class Swiping(
        override val direction: SwipeToDismissBoxValue
    ) : SwipeLifecycleState

    data class Armed(
        override val direction: SwipeToDismissBoxValue
    ) : SwipeLifecycleState

    data class ReadyToExecute(
        override val direction: SwipeToDismissBoxValue
    ) : SwipeLifecycleState
}
