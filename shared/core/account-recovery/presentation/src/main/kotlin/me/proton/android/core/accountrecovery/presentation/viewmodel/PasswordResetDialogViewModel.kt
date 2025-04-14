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

package me.proton.android.core.accountrecovery.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountrecovery.presentation.ui.Arg
import me.proton.android.core.accountrecovery.presentation.ui.PasswordResetDialogAction
import me.proton.android.core.accountrecovery.presentation.ui.PasswordResetDialogViewState
import me.proton.android.core.accountrecovery.presentation.usecase.ObserveUserEmail
import me.proton.android.core.accountrecovery.presentation.usecase.StartRecovery
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
class PasswordResetDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeUserEmail: ObserveUserEmail,
    private val startRecovery: StartRecovery
) : BaseViewModel<PasswordResetDialogAction, PasswordResetDialogViewState>(
    initialAction = PasswordResetDialogAction.ObserveState,
    initialState = PasswordResetDialogViewState.Loading()
) {
    private val userId = CoreUserId(requireNotNull(savedStateHandle.get<String>(Arg.UserId)))
    private val userEmail = observeUserEmail(userId).stateIn(viewModelScope, Eagerly, null)

    override suspend fun FlowCollector<PasswordResetDialogViewState>.onError(throwable: Throwable) {
        emit(PasswordResetDialogViewState.Error(throwable.message))
    }

    override fun onAction(action: PasswordResetDialogAction): Flow<PasswordResetDialogViewState> {
        return when (action) {
            is PasswordResetDialogAction.ObserveState -> observeState(userEmail.value)
            is PasswordResetDialogAction.RequestReset -> requestReset(userEmail.value)
        }
    }

    private fun observeState(email: String?): Flow<PasswordResetDialogViewState> = flow {
        emit(PasswordResetDialogViewState.Loading(email))
        emit(PasswordResetDialogViewState.Ready(email ?: ""))
    }

    private fun requestReset(email: String?): Flow<PasswordResetDialogViewState> = flowWithResultContext {
        // add observability
        emit(PasswordResetDialogViewState.Loading(email))
        startRecovery(userId)
        emit(PasswordResetDialogViewState.ResetRequested)
    }
}
