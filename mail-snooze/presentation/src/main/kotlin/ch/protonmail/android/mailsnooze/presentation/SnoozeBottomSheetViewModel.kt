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
import ch.protonmail.android.mailsnooze.domain.SnoozeRepository
import ch.protonmail.android.mailsnooze.domain.model.SnoozeTime
import ch.protonmail.android.mailsnooze.presentation.model.Custom
import ch.protonmail.android.mailsnooze.presentation.model.PredefinedChoice
import ch.protonmail.android.mailsnooze.presentation.model.SelectionType
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOperationViewAction
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOptionsEffects
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOptionsState
import ch.protonmail.android.mailsnooze.presentation.model.mapper.DayTimeMapper
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeErrorMapper.toUIModel
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeOptionUiModelMapper.toSnoozeOptionUiModel
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeSuccessMapper.snoozeSuccessMessage
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeSuccessMapper.toSuccessMessage
import ch.protonmail.android.mailsnooze.presentation.model.onErrorEffect
import ch.protonmail.android.mailsnooze.presentation.model.onNavigateToUpsell
import ch.protonmail.android.mailsnooze.presentation.model.onSuccessEffect
import ch.protonmail.android.mailsnooze.presentation.model.toConversationId
import ch.protonmail.android.mailsnooze.presentation.usecase.GetFirstDayOfWeekStart
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SnoozeBottomSheetViewModel.Factory::class)
class SnoozeBottomSheetViewModel @AssistedInject constructor(
    @Assisted val initialData: SnoozeBottomSheet.InitialData,
    val snoozeRepository: SnoozeRepository,
    val dayTimeMapper: DayTimeMapper,
    val getFirstDayOfWeekStart: GetFirstDayOfWeekStart
) : ViewModel() {

    private val _effects = MutableStateFlow(SnoozeOptionsEffects())
    val effects = _effects.asStateFlow()

    private val bottomSheetState = MutableStateFlow<SelectionType>(PredefinedChoice)
    val state: Flow<SnoozeOptionsState> =
        combine(
            flow {
                emit(
                    snoozeRepository.getAvailableSnoozeActions(
                        userId = initialData.userId,
                        weekStart = getFirstDayOfWeekStart(),
                        conversationIds = initialData.items.map { it.toConversationId() }
                    )
                )
            }.map { it.getOrNull() } // error case should not exist
                .filterNotNull(),
            bottomSheetState
        ) { options, state ->
            SnoozeOptionsState.Loaded(
                snoozeOptions =
                options.map { it.toSnoozeOptionUiModel(dayTimeMapper) },
                snoozeBottomSheet = state
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis),
            SnoozeOptionsState.Loading
        )


    private fun onSelectSnoozeTime(snoozeTime: SnoozeTime) {
        viewModelScope.launch {
            snoozeRepository.snoozeConversation(
                userId = initialData.userId,
                labelId = initialData.labelId,
                conversationIds = initialData.items.map { it.toConversationId() },
                snoozeTime = snoozeTime
            ).onLeft { error ->
                _effects.update { it.onErrorEffect(error.toUIModel()) }
            }.onRight {
                _effects.update { it.onSuccessEffect(snoozeTime.toSuccessMessage(dayTimeMapper)) }
            }
        }
    }

    private fun onUnsnooze() {
        viewModelScope.launch {
            snoozeRepository.unSnoozeConversation(
                userId = initialData.userId,
                labelId = initialData.labelId,
                conversationIds = initialData.items.map { it.toConversationId() }
            ).onLeft { error ->
                _effects.update { it.onErrorEffect(error.toUIModel()) }
            }.onRight {
                _effects.update { it.onSuccessEffect(snoozeSuccessMessage()) }
            }
        }
    }

    fun onAction(action: SnoozeOperationViewAction) {
        viewModelScope.launch {
            when (action) {
                is SnoozeOperationViewAction.SnoozeUntil -> onSelectSnoozeTime(action.snoozeTime)
                is SnoozeOperationViewAction.PickSnooze -> {
                    bottomSheetState.emit(Custom)
                }

                is SnoozeOperationViewAction.UnSnooze -> {
                    onUnsnooze()
                }

                is SnoozeOperationViewAction.Upgrade -> {
                    _effects.update { it.onNavigateToUpsell(action.type) }
                }
                SnoozeOperationViewAction.CancelPicker -> bottomSheetState.emit(PredefinedChoice)
            }
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(payload: SnoozeBottomSheet.InitialData): SnoozeBottomSheetViewModel
    }
}
