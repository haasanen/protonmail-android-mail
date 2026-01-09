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

package ch.protonmail.android.mailtrackingprotection.presentation.model

import androidx.compose.runtime.Stable
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetContentState
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetOperation

@Stable
sealed interface BlockedElementsSheetState : BottomSheetContentState {

    data class Requested(val elements: BlockedElementsUiModel?) : BlockedElementsSheetState
}

sealed interface BlockedTrackersBottomSheetOperation : BottomSheetOperation

sealed interface BlockedTrackersBottomSheetEvent : BlockedTrackersBottomSheetOperation {
    data class Ready(val elements: BlockedElementsUiModel?) : BlockedTrackersBottomSheetEvent
}
