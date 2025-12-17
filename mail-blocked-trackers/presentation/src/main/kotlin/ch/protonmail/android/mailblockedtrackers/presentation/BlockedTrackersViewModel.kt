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

package ch.protonmail.android.mailblockedtrackers.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailblockedtrackers.presentation.model.BlockedTrackersState
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsShowBlockedTrackersEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailmessage.domain.model.MessageId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedTrackersViewModel @Inject constructor(
    @IsShowBlockedTrackersEnabled val showBlockedTrackersFeatureFlag: FeatureFlag<Boolean>
) : ViewModel() {

    private val mutableState = MutableStateFlow<BlockedTrackersState>(BlockedTrackersState.Unknown)

    val state: StateFlow<BlockedTrackersState> = mutableState

    fun loadBlockedTrackers(messageId: MessageId) {
        viewModelScope.launch {
            if (showBlockedTrackersFeatureFlag.get()) {
                mutableState.emit(BlockedTrackersState.TrackersBlocked(TrackersUiModelSample.oneTrackerBlocked))
            } else {
                mutableState.emit(BlockedTrackersState.Unknown)
            }
        }
    }

}
