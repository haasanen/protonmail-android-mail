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

package me.proton.android.core.auth.presentation.secondfactor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorArg.getUserId
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorInputAction.Close
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import uniffi.proton_mail_uniffi.MailSession
import javax.inject.Inject

@HiltViewModel
class SecondFactorInputViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionInterface: MailSession
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<SecondFactorInputAction?>(null)

    val state: StateFlow<SecondFactorInputState> = mutableAction.flatMapLatest { action ->
        when (action) {
            null -> flowOf(SecondFactorInputState.Idle)
            is Close -> onClose()
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), SecondFactorInputState.Idle)

    fun submit(action: SecondFactorInputAction) = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private suspend fun onClose(): Flow<SecondFactorInputState> = flow {
        sessionInterface.deleteAccount(userId)
        emit(SecondFactorInputState.Close)
    }
}
