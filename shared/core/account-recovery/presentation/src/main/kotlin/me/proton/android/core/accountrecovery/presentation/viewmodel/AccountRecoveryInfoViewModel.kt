/*
 * Copyright (c) 2024 Proton Technologies AG
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

import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.android.core.account.domain.usecase.ObservePrimaryCoreAccount
import me.proton.android.core.accountrecovery.presentation.entity.UserRecovery
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryInfoViewState
import me.proton.android.core.accountrecovery.presentation.usecase.ObserveUserRecovery
import me.proton.core.compose.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AccountRecoveryInfoViewModel @Inject constructor(
    observePrimaryCoreAccount: ObservePrimaryCoreAccount,
    private val observeUserRecovery: ObserveUserRecovery
) : BaseViewModel<Unit, AccountRecoveryInfoViewState>(Unit, AccountRecoveryInfoViewState.None) {

    private val primaryUserId = observePrimaryCoreAccount().map { it?.userId }.filterNotNull().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = null
    )

    override suspend fun FlowCollector<AccountRecoveryInfoViewState>.onError(throwable: Throwable) {
        emit(AccountRecoveryInfoViewState.Error(throwable.message))
    }

    override fun onAction(action: Unit): Flow<AccountRecoveryInfoViewState> {
        return primaryUserId
            .filterNotNull()
            .flatMapLatest { observeUserRecovery(it) }
            .map { recovery ->
                when (recovery?.state?.enum) {
                    null -> AccountRecoveryInfoViewState.None
                    UserRecovery.State.None -> AccountRecoveryInfoViewState.None
                    else -> AccountRecoveryInfoViewState.Recovery(
                        recoveryState = recovery.state.enum,
                        startDate = recovery.startDateFormatted,
                        endDate = recovery.endDateFormatted,
                        durationUntilEnd = recovery.durationFormatted
                    )
                }
            }
    }
}
