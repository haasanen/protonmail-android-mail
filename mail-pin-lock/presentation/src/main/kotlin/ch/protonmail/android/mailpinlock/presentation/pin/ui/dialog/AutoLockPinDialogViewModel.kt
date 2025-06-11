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

package ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.presentation.autolock.model.DialogType
import ch.protonmail.android.mailpinlock.presentation.pin.reducer.AutoLockPinDialogReducer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoLockPinDialogViewModel @Inject constructor(
    private val autoLockRepository: AutoLockRepository,
    private val dialogReducer: AutoLockPinDialogReducer
) : ViewModel() {

    private val mutableState = MutableStateFlow(AutoLockDialogState(null, Effect.empty()))
    val state = mutableState.asStateFlow()

    internal val textFieldState = TextFieldState()

    fun processPin(dialogType: DialogType) {
        viewModelScope.launch {
            when (dialogType) {
                DialogType.None -> Unit
                DialogType.ChangePin -> verifyPin()
                DialogType.DisablePin,
                DialogType.MigrateToBiometrics -> removePin()
            }
        }
    }

    private suspend fun verifyPin() {
        autoLockRepository.verifyAutoLockPinCode(getCurrentPinValue()).getOrElse { error ->
            val remainingAttempts = autoLockRepository.getRemainingAttempts().getOrNull()
            return emitNewStateFrom(AutoLockPinDialogEvent.Error(error, remainingAttempts))
        }

        updateStateWithSuccess()
    }

    private suspend fun removePin() {
        autoLockRepository.deleteAutoLockPinCode(getCurrentPinValue()).getOrElse { error ->
            val remainingAttempts = autoLockRepository.getRemainingAttempts().getOrNull()
            return emitNewStateFrom(AutoLockPinDialogEvent.Error(error, remainingAttempts))
        }

        updateStateWithSuccess()
    }

    private fun updateStateWithSuccess() {
        mutableState.update { it.copy(successEffect = Effect.of(Unit)) }
    }

    private fun emitNewStateFrom(event: AutoLockPinDialogEvent) {
        mutableState.update {
            dialogReducer.newStateFrom(it, event)
        }
    }

    private fun getCurrentPinValue() = AutoLockPin(textFieldState.text.toString())
}
