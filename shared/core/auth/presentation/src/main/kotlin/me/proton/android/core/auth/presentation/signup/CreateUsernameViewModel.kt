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

package me.proton.android.core.auth.presentation.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.presentation.savedstate.state
import javax.inject.Inject

@HiltViewModel
class CreateUsernameViewModel @Inject constructor(
    private val challengeManager: ChallengeManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentAccountType: AccountType by savedStateHandle.state(AccountType.External)
    private var domains: List<Domain> = emptyList()
    private val mutableAction = MutableStateFlow<CreateUsernameAction?>(null)

    val state: StateFlow<CreateUsernameState> = mutableAction.flatMapLatest { action ->
        when (action) {
            null -> flowOf(CreateUsernameState.Idle(currentAccountType))
            is CreateUsernameAction.SetNavigationDone -> onSetNavigationDone()
            is CreateUsernameAction.CreateExternalAccount -> onCreateExternalAccount()
            is CreateUsernameAction.CreateInternalAccount -> onCreateInternalAccount()
            is CreateUsernameAction.Load -> onInit(currentAccountType)
            is CreateUsernameAction.Submit -> onSubmit(action.type, action.value, action.usernameFrameDetails)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), CreateUsernameState.Loading(currentAccountType))

    private fun onInit(accountType: AccountType): Flow<CreateUsernameState> = flow {
        emitAll(
            when (accountType) {
                AccountType.Internal -> onCreateInternalAccount()
                AccountType.External -> onCreateExternalAccount()
            }
        )
    }

    private fun onCreateExternalAccount(): Flow<CreateUsernameState> = flow {
        currentAccountType = AccountType.External
        emit(CreateUsernameState.Idle(AccountType.External))
    }

    @Suppress("ForbiddenComment")
    private fun onCreateInternalAccount(): Flow<CreateUsernameState> = flow {
        currentAccountType = AccountType.Internal
        emit(CreateUsernameState.Loading(AccountType.Internal))
        // fixme: refactor to fetch from API
        domains = listOf("protonmail.com", "protonmail.ch")
        emit(CreateUsernameState.Idle(AccountType.Internal, domains))
    }

    private fun onSubmit(
        accountType: AccountType,
        username: String,
        usernameFrameDetails: ChallengeFrameDetails
    ): Flow<CreateUsernameState> = flow {
        challengeManager.addOrUpdateFrameToFlow(usernameFrameDetails)
        emit(CreateUsernameState.Success(accountType, username))
    }

    private fun onSetNavigationDone(): Flow<CreateUsernameState> = flow {
        emit(CreateUsernameState.Idle(currentAccountType, domains))
    }

    fun submit(action: CreateUsernameAction) = viewModelScope.launch {
        mutableAction.emit(action)
    }
}
