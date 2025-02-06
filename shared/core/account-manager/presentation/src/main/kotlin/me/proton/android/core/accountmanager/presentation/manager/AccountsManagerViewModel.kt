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

package me.proton.android.core.accountmanager.presentation.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountListItem
import javax.inject.Inject

@HiltViewModel
class AccountsManagerViewModel @Inject constructor(
    private val observeAccountListItems: ObserveAccountListItems
) : ViewModel() {

    private val actions: MutableSharedFlow<AccountsManagerAction> = MutableSharedFlow(replay = 1)

    val state: StateFlow<AccountsManagerState> = actions
        .onStart { actions.emit(AccountsManagerAction.Load()) }
        .flatMapLatest {
            when (it) {
                is AccountsManagerAction.Load -> onLoad()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), AccountsManagerState.Loading)

    private fun onLoad() = flow {
        emit(AccountsManagerState.Loading)
        emitAll(
            observeAccountListItems().map { items ->
                val signedInAccounts = items.filterIsInstance<AccountListItem.Ready>()
                val disabledAccounts = items.filterIsInstance<AccountListItem.Disabled>()
                AccountsManagerState.Idle(signedInAccounts = signedInAccounts, disabledAccounts = disabledAccounts)
            }
        )
    }
}
