/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.accountrecovery.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountrecovery.presentation.LogTag
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryViewState
import me.proton.android.core.accountrecovery.presentation.ui.Arg
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class AccountRecoveryDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId = CoreUserId(requireNotNull(savedStateHandle.get<String>(Arg.UserId)))
    private val ackFlow = MutableStateFlow(false)
    private val shouldShowRecoveryReset = MutableStateFlow(false)

    val initialState = AccountRecoveryViewState.Loading

    val state: StateFlow<AccountRecoveryViewState> = ackFlow.flatMapLatest { ack ->
        shouldShowRecoveryReset.flatMapLatest { showRecovery ->
            when {
                ack -> flowOf(AccountRecoveryViewState.Closed())
                showRecovery -> flowOf(AccountRecoveryViewState.StartPasswordManager(userId))
                else -> observeState()
            }
        }
    }.catch {
        CoreLogger.e(LogTag.ERROR_OBSERVING_STATE, it)
        emit(AccountRecoveryViewState.Error(it.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = initialState
    )

    private fun observeState() = flowOf<AccountRecoveryViewState>()

    internal fun userAcknowledged() {
        ackFlow.update { true }
    }
}
