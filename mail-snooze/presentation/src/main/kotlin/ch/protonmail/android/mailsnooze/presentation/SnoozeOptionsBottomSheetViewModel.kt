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

package ch.protonmail.android.mailsnooze.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailsnooze.domain.SnoozeRepository
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOptionsState
import ch.protonmail.android.mailsnooze.presentation.model.mapper.DayTimeMapper
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeOptionUiModelMapper.toSnoozeOptionUiModel
import ch.protonmail.android.mailsnooze.presentation.usecase.GetFirstDayOfWeekStart
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = SnoozeOptionsBottomSheetViewModel.Factory::class)
class SnoozeOptionsBottomSheetViewModel @AssistedInject constructor(
    @Assisted val initialData: SnoozeBottomSheet.InitialData,
    snoozeRepository: SnoozeRepository,
    val dayTimeMapper: DayTimeMapper,
    val getFirstDayOfWeekStart: GetFirstDayOfWeekStart
) : ViewModel() {

    val state: Flow<SnoozeOptionsState> =
        flow {
            emit(
                snoozeRepository.getAvailableSnoozeActions(
                    userId = initialData.userId,
                    weekStart = getFirstDayOfWeekStart(),
                    conversationIds = initialData.items.map { ConversationId(it.value) }
                )
            )
        }.map { it.getOrNull() } // error case should not exist
            .filterNotNull()
            .map { options ->
                SnoozeOptionsState.Data(
                    snoozeOptions =
                    options.map { it.toSnoozeOptionUiModel(dayTimeMapper) }
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis),
                SnoozeOptionsState.Loading
            )

    @AssistedFactory
    interface Factory {

        fun create(payload: SnoozeBottomSheet.InitialData): SnoozeOptionsBottomSheetViewModel
    }
}
