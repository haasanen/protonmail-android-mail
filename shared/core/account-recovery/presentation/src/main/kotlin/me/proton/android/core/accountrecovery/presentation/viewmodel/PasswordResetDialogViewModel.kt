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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.proton.android.core.account.domain.model.CoreAccount
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.account.domain.usecase.ObserveCoreAccounts
import me.proton.android.core.accountrecovery.presentation.ui.Arg
import me.proton.android.core.accountrecovery.presentation.ui.PasswordResetDialogAction
import me.proton.android.core.accountrecovery.presentation.ui.PasswordResetDialogViewState
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
class PasswordResetDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCoreAccounts: ObserveCoreAccounts
) : ViewModel() {

    private val userId = CoreUserId(requireNotNull(savedStateHandle.get<String>(Arg.UserId)))

    private val currentUser =
        observeCoreAccounts()
            .map { accounts ->
                accounts.firstOrNull { it.userId == userId }
            }
            .stateIn(viewModelScope, Eagerly, null)

    private val currentAction = MutableStateFlow<PasswordResetDialogAction>(PasswordResetDialogAction.ObserveState)

    val state: StateFlow<PasswordResetDialogViewState> = currentAction.flatMapLatest { action ->
        when (action) {
            is PasswordResetDialogAction.ObserveState -> observeState()
            is PasswordResetDialogAction.RequestReset -> requestReset()
        }
    }.catch {
        emit(PasswordResetDialogViewState.Error(it.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = PasswordResetDialogViewState.Loading()
    )

    private fun CoreAccount.getEmail() = primaryEmailAddress ?: username ?: displayName ?: ""

    private fun observeState(): Flow<PasswordResetDialogViewState> = flow {
        emit(PasswordResetDialogViewState.Loading(currentUser.value?.getEmail()))
        emitAll(currentUser.filterNotNull().mapLatest { PasswordResetDialogViewState.Ready(it.getEmail()) })
    }

    private suspend fun requestReset(): Flow<PasswordResetDialogViewState> = flowWithResultContext {
//        onResultEnqueueObservability("account_recovery.start") { AccountRecoveryStartTotal(this) }

        emit(PasswordResetDialogViewState.Loading(currentUser.value?.getEmail()))
//        startRecovery(userId)
        emit(PasswordResetDialogViewState.ResetRequested)
    }

    fun perform(action: PasswordResetDialogAction) = currentAction.tryEmit(action)
}
