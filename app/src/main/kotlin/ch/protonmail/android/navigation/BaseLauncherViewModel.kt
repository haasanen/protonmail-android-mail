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

package ch.protonmail.android.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.navigation.model.LauncherState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId

abstract class BaseLauncherViewModel : ViewModel() {

    abstract val state: StateFlow<LauncherState>

    open fun register(context: AppCompatActivity) {}

    open fun submit(action: Action) {
        viewModelScope.launch {
            when (action) {
                Action.AddAccount -> onAddAccount()
                Action.OpenPasswordManagement -> onOpenPasswordManagement()
                Action.OpenRecoveryEmail -> onOpenRecoveryEmail()
                Action.OpenReport -> onOpenReport()
                Action.OpenSubscription -> onOpenSubscription()
                is Action.SignIn -> onSignIn(action.userId)
                is Action.Switch -> onSwitch(action.userId)
            }
        }
    }

    // These methods can be overridden in the subclasses
    protected open fun onAddAccount() {}
    protected open suspend fun onOpenPasswordManagement() {}
    protected open suspend fun onOpenRecoveryEmail() {}
    protected open suspend fun onOpenReport() {}
    protected open suspend fun onOpenSubscription() {}
    protected open suspend fun onSignIn(userId: UserId?) {}
    protected open suspend fun onSwitch(userId: UserId) {}

    sealed interface Action {
        object AddAccount : Action
        object OpenPasswordManagement : Action
        object OpenRecoveryEmail : Action
        object OpenReport : Action
        object OpenSubscription : Action
        data class SignIn(val userId: UserId?) : Action
        data class Switch(val userId: UserId) : Action
    }
}
