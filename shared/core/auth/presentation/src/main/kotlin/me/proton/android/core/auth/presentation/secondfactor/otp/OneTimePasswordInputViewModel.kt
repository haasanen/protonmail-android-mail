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

package me.proton.android.core.auth.presentation.secondfactor.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorArg
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import uniffi.proton_mail_uniffi.LoginFlowException
import javax.inject.Inject

@HiltViewModel
class OneTimePasswordInputViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String by lazy {
        requireNotNull(savedStateHandle.get<String>(SecondFactorArg.ARG_USER_ID)) {
            "Missing state value for key: ${SecondFactorArg.ARG_USER_ID}"
        }
    }

    private val _mode: MutableStateFlow<OneTimePasswordInputMode> = MutableStateFlow(OneTimePasswordInputMode.Totp)
    val mode: StateFlow<OneTimePasswordInputMode> = _mode.asStateFlow()

    private val _state: MutableStateFlow<OneTimePasswordInputState> = MutableStateFlow(OneTimePasswordInputState.Idle)
    val state: StateFlow<OneTimePasswordInputState> = _state.asStateFlow()

    fun submit(action: OneTimePasswordInputAction) = viewModelScope.launch {
        when (action) {
            is OneTimePasswordInputAction.Authenticate -> onAuthenticate(action)
            is OneTimePasswordInputAction.SwitchMode -> onSwitchMode(action)
        }
    }

    private fun onAuthenticate(action: OneTimePasswordInputAction.Authenticate) = flow {
        emit(OneTimePasswordInputState.Loading)
        check(InputValidationResult(action.code, ValidationType.NotBlank).isValid)
        TODO()
        emit(OneTimePasswordInputState.Success)
    }.catch {
        when (it) {
            is IllegalStateException -> emit(OneTimePasswordInputState.CodeIsEmpty)
            is LoginFlowException -> emit(OneTimePasswordInputState.LoginError(it))
            else -> throw it
        }
    }.onEach {
        _state.emit(it)
    }.launchIn(viewModelScope)

    private suspend fun onSwitchMode(action: OneTimePasswordInputAction.SwitchMode) {
        _mode.emit(action.mode)
    }
}
