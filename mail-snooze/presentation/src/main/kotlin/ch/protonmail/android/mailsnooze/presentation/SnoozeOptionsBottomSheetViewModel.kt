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

package ch.protonmail.android.mailmessage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailmessage.presentation.model.snooze.toSnoozeOptionUiModel
import ch.protonmail.android.mailsnooze.domain.model.SnoozeOption
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOptionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SnoozeOptionsBottomSheetViewModel @Inject constructor() : ViewModel() {

    // whilst there is no rust backend
    val snoozeRepository = flowOf(
        Pair(
            listOf(
                SnoozeOption.Tomorrow("09:00"),
                SnoozeOption.LaterThisWeek("Sun, 09:00"),
                SnoozeOption.ThisWeekend("Sat, 09:00"),
                SnoozeOption.NextWeek("Mon, 09:00")
            ),
            SnoozeOption.UpgradeRequired
        )
    )

    val state: Flow<SnoozeOptionsState> = snoozeRepository // .getSnoozeOptions()
        .mapLatest { snoozeOptions ->
            SnoozeOptionsState.Data(
                snoozeOptions =
                snoozeOptions.first.map { it.toSnoozeOptionUiModel() },
                customSnoozeOption = snoozeOptions.second.toSnoozeOptionUiModel(),
                showUnSnooze = true

            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis),
            SnoozeOptionsState.Loading
        )
}
