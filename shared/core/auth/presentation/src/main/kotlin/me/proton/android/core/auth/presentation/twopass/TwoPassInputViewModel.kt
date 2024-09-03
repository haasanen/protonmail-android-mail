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

package me.proton.android.core.auth.presentation.twopass

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
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import uniffi.proton_mail_uniffi.LoginFlowException
import javax.inject.Inject

@HiltViewModel
class TwoPassInputViewModel @Inject constructor() : ViewModel() {

    private val _state: MutableStateFlow<TwoPassInputState> = MutableStateFlow(TwoPassInputState.Idle)
    val state: StateFlow<TwoPassInputState> = _state.asStateFlow()

    fun submit(action: TwoPassInputAction) = viewModelScope.launch {
        when (action) {
            is TwoPassInputAction.Unlock -> onUnlock(action)
        }
    }

    private fun onUnlock(action: TwoPassInputAction.Unlock) = flow {
        emit(TwoPassInputState.Loading)
        check(InputValidationResult(action.mailboxPassword, ValidationType.Password).isValid)
        TODO("Submit mailbox password")
        emit(TwoPassInputState.Success)
    }.catch {
        when (it) {
            is IllegalStateException -> emit(TwoPassInputState.PasswordIsEmpty)
            is LoginFlowException -> emit(TwoPassInputState.LoginError(it))
            else -> throw it
        }
    }.onEach {
        _state.emit(it)
    }.launchIn(viewModelScope)
}
