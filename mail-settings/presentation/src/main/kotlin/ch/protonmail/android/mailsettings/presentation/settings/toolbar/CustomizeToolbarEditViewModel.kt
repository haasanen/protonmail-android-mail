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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.domain.repository.InMemoryToolbarActionsRepository
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditOperation
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditState
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.SaveEvent
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.ToolbarActionsRefreshSignal
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.reducer.CustomizeToolbarEditActionsReducer
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui.CustomizeToolbarEditScreen
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.usecase.UpdateToolbarPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject

@HiltViewModel
internal class CustomizeToolbarEditViewModel @Inject constructor(
    private val temporaryPrefs: InMemoryToolbarActionsRepository,
    private val reducer: CustomizeToolbarEditActionsReducer,
    private val updateToolbarPreferences: UpdateToolbarPreferences,
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val toolbarRefreshSignal: ToolbarActionsRefreshSignal,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val toolbarType by lazy {
        savedStateHandle.get<String>(CustomizeToolbarEditScreen.OpenMode)
            ?.deserialize<ToolbarType>()
            ?: ToolbarType.List
    }

    private val preferences = temporaryPrefs.inMemoryPreferences(toolbarType)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), replay = 1)

    private val saveEffect = MutableStateFlow<SaveEvent>(SaveEvent.None)

    val state: StateFlow<CustomizeToolbarEditState> = combine(
        preferences,
        saveEffect
    ) { prefsResponse, effect ->
        prefsResponse.fold(
            ifLeft = { CustomizeToolbarEditState.Error },
            ifRight = { reducer.toNewState(it.actionsList.current, toolbarType, effect) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = CustomizeToolbarEditState.Loading
    )

    internal fun submit(action: CustomizeToolbarEditOperation) {
        when (action) {
            is CustomizeToolbarEditOperation.ActionRemoved -> temporaryPrefs.toggleSelection(
                action.action,
                toggled = false
            )

            is CustomizeToolbarEditOperation.ActionSelected -> temporaryPrefs.toggleSelection(
                action.action,
                toggled = true
            )

            CustomizeToolbarEditOperation.ResetToDefaultConfirmed -> temporaryPrefs.resetToDefault()
            is CustomizeToolbarEditOperation.ActionMoved -> temporaryPrefs.reorder(
                fromIndex = action.fromIndex,
                toIndex = action.toIndex
            )

            CustomizeToolbarEditOperation.SaveClicked -> savePreferences()
        }
    }

    private fun savePreferences() = viewModelScope.launch {
        val userId = observePrimaryUserId().firstOrNull()
            ?: return@launch saveEffect.emit(SaveEvent.Error)

        val preferences = preferences.firstOrNull()?.getOrNull()
            ?: return@launch saveEffect.emit(SaveEvent.Error)

        updateToolbarPreferences(userId, toolbarType, preferences).fold(
            ifLeft = { saveEffect.emit(SaveEvent.Error) },
            ifRight = {
                toolbarRefreshSignal.refresh()
                saveEffect.emit(SaveEvent.Success)
            }
        )
    }
}
