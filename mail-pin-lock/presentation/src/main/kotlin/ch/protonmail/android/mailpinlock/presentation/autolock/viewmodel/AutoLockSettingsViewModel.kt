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

package ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailpinlock.presentation.autolock.mapper.AutoLockSettingsUiMapper
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsEffects
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsEvent
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsUiState
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockSettingsViewAction
import ch.protonmail.android.mailpinlock.presentation.autolock.model.BiometricsOperationFollowUp
import ch.protonmail.android.mailpinlock.presentation.autolock.reducer.AutoLockSettingsReducer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoLockSettingsViewModel @Inject constructor(
    private val autoLockRepository: AutoLockRepository,
    private val reducer: AutoLockSettingsReducer
) : ViewModel() {

    private val _effects = MutableStateFlow(AutoLockSettingsEffects())
    val effects = _effects.asStateFlow()

    val state = autoLockRepository
        .observeAppLock()
        .map { autoLock ->
            val uiModel = AutoLockSettingsUiMapper.toUiModel(autoLock)
            AutoLockSettingsUiState.Data(settings = uiModel)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(stopTimeoutMillis),
            AutoLockSettingsUiState.Loading
        )

    internal fun submit(action: AutoLockSettingsViewAction) {
        viewModelScope.launch {
            when (action) {
                is AutoLockSettingsViewAction.RequestProtectionRemoval -> resolveProtectionRemoval()
                is AutoLockSettingsViewAction.RequestPinProtection -> resolvePinProtection()
                is AutoLockSettingsViewAction.RequestPinProtectionChange -> resolvePinProtectionChange()
                is AutoLockSettingsViewAction.MigrateFromPinToBiometrics -> disablePinProtection()
                is AutoLockSettingsViewAction.RequestBiometricsProtection -> resolveBiometricProtection()
                is AutoLockSettingsViewAction.SetPinPreference -> applyPinPreference()
                is AutoLockSettingsViewAction.SetBiometricsPreference -> applyBiometricPreference()
                is AutoLockSettingsViewAction.RemoveBiometricsProtection -> disableBiometricPreference()
            }
        }
    }

    private suspend fun disablePinProtection() {
        applyBiometricPreference()
        emitNewEffectFrom(AutoLockSettingsEvent.PinRemovalRequested)
    }

    private suspend fun resolveBiometricProtection() = withCurrentLockPolicy { autoLock ->
        val followUpEvent = when (autoLock.protectionType) {
            Protection.None -> BiometricsOperationFollowUp.SetBiometrics
            Protection.Pin -> BiometricsOperationFollowUp.RemovePinAndSetBiometrics
            Protection.Biometrics -> return
        }

        emitNewEffectFrom(AutoLockSettingsEvent.BiometricAuthRequested(followUpEvent))
    }

    private suspend fun resolvePinProtection() = withCurrentLockPolicy { autoLock ->
        val event = when (autoLock.protectionType) {
            Protection.Pin,
            Protection.None -> AutoLockSettingsEvent.PinCreationRequested

            Protection.Biometrics -> AutoLockSettingsEvent.BiometricAuthRequested(BiometricsOperationFollowUp.SetPin)
        }

        emitNewEffectFrom(event)
    }

    private fun resolvePinProtectionChange() {
        emitNewEffectFrom(AutoLockSettingsEvent.PinChangeRequested)
    }

    private suspend fun resolveProtectionRemoval() = withCurrentLockPolicy { autoLock ->
        val event = when (autoLock.protectionType) {
            Protection.Pin -> AutoLockSettingsEvent.PinRemovalRequested
            Protection.Biometrics -> AutoLockSettingsEvent.BiometricAuthRequested(BiometricsOperationFollowUp.SetNone)
            Protection.None -> return
        }

        emitNewEffectFrom(event)
    }

    private suspend fun disableBiometricPreference() {
        autoLockRepository.setBiometricProtection(false).onLeft {
            emitNewEffectFrom(AutoLockSettingsEvent.Error.BiometricsUnsetError)
        }
    }

    private suspend fun applyBiometricPreference() = withCurrentLockPolicy { autoLock ->
        if (autoLock.protectionType == Protection.Biometrics) return

        autoLockRepository.setBiometricProtection(true).onLeft {
            emitNewEffectFrom(AutoLockSettingsEvent.Error.BiometricsSetError)
        }
    }

    private suspend fun applyPinPreference() = withCurrentLockPolicy { autoLock ->
        if (autoLock.protectionType != Protection.Pin) {
            return emitNewEffectFrom(AutoLockSettingsEvent.PinCreationRequested)
        }
    }

    private suspend inline fun withCurrentLockPolicy(action: (AutoLock) -> Unit) {
        val currentAutoLock = autoLockRepository.observeAppLock().firstOrNull()
            ?: return emitNewEffectFrom(AutoLockSettingsEvent.Error.UnknownLockPolicy)

        action(currentAutoLock)
    }

    private fun emitNewEffectFrom(event: AutoLockSettingsEvent) {
        _effects.update { reducer.newStateFrom(it, event) }
    }
}
