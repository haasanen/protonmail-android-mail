/*
 * Copyright (c) 2022 Proton Technologies AG
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

package ch.protonmail.android.mailpinlock.presentation.autolock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailpinlock.domain.AutolockRepository
import ch.protonmail.android.mailpinlock.presentation.autolock.mapper.AutolockSettingsUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoLockSettingsViewModel @Inject constructor(
    private val autolockRepository: AutolockRepository
) : ViewModel() {

    private val _effects = MutableStateFlow(AutoLockSettingsEffects())
    val effects = _effects.asStateFlow()

    val state = autolockRepository
        .observeAppLock()
        .map { autolock ->
            val uiModel = AutolockSettingsUiMapper.toUiModel(autolock)
            AutolockSettingsUiState.Data(settings = uiModel)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), AutolockSettingsUiState.Loading)

    internal fun submit(action: AutoLockSettingsViewAction) {
        viewModelScope.launch {
            when (action) {
                is AutoLockSettingsViewAction.ToggleAutoLockPreference -> updateAutoLockEnabledValue(action.newValue)

                // TODO inegrate the biometrics
                //is AutoLockSettingsViewAction.ToggleAutoLockBiometricsPreference ->
                // updateBiometricsEnabledValue(action.autoLockBiometricsUiModel)
                else -> {}
            }
        }
    }

    private suspend fun updateAutoLockEnabledValue(enabled: Boolean) {
        // if turn off autolock - go to pin flow verify and turn off
        // if turn on autolock - go to pin flow to set pin
    }

    private suspend fun updateBiometricsEnabledValue(enabled: Boolean) {
        if (enabled) {
            // both go to pin flow, need to confirm pin if disabling
            // open pin verify flow
        }
    }
}

