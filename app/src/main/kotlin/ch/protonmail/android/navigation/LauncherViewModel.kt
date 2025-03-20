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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailnotifications.permissions.NotificationsPermissionOrchestrator
import ch.protonmail.android.mailsession.data.mapper.toLocalUserId
import ch.protonmail.android.mailsession.data.mapper.toUserId
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.usecase.SetPrimaryAccount
import ch.protonmail.android.mailsession.presentation.observe
import ch.protonmail.android.mailsession.presentation.onAccountTwoFactorNeeded
import ch.protonmail.android.mailsession.presentation.onAccountTwoPasswordNeeded
import ch.protonmail.android.navigation.model.LauncherState
import ch.protonmail.android.navigation.model.LauncherState.AccountNeeded
import ch.protonmail.android.navigation.model.LauncherState.PrimaryExist
import ch.protonmail.android.navigation.model.LauncherState.Processing
import ch.protonmail.android.navigation.model.LauncherState.StepNeeded
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.AuthOrchestrator
import me.proton.android.core.auth.presentation.login.LoginInput
import me.proton.android.core.auth.presentation.onAddAccountResult
import me.proton.android.core.auth.presentation.onLoginResult
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@HiltViewModel
@SuppressWarnings("NotImplementedDeclaration", "UnusedPrivateMember")
class LauncherViewModel @Inject constructor(
    private val authOrchestrator: AuthOrchestrator,
    private val setPrimaryAccount: SetPrimaryAccount,
    private val userSessionRepository: UserSessionRepository,
    private val notificationsPermissionOrchestrator: NotificationsPermissionOrchestrator
) : ViewModel() {

    val state: StateFlow<LauncherState> = userSessionRepository.observeAccounts()
        .mapLatest { accounts ->
            when {
                accounts.isEmpty() || accounts.all { it.state == AccountState.Disabled } -> AccountNeeded
                accounts.any { it.state == AccountState.TwoPasswordNeeded } -> StepNeeded
                accounts.any { it.state == AccountState.TwoFactorNeeded } -> StepNeeded
                accounts.any { it.state == AccountState.Ready } -> PrimaryExist
                else -> Processing
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = Processing
        )

    override fun onCleared() {
        authOrchestrator.unregister()
        notificationsPermissionOrchestrator.unregister()
        super.onCleared()
    }

    fun register(context: AppCompatActivity) {
        with(authOrchestrator) {
            register(context)
            onAddAccountResult { result -> if (!result) context.finish() }
            onLoginResult { result -> if (result != null) onSwitchToAccount(result.userId.toUserId()) }
            userSessionRepository.observe(context.lifecycle, minActiveState = Lifecycle.State.RESUMED)
                .onAccountTwoFactorNeeded { startSecondFactorWorkflow(it.userId.toLocalUserId()) }
                .onAccountTwoPasswordNeeded { startTwoPassModeWorkflow(it.userId.toLocalUserId()) }
        }

        notificationsPermissionOrchestrator.register(context)
    }

    fun submit(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.AddAccount -> onAddAccount()
                is Action.OpenPasswordManagement -> onOpenPasswordManagement()
                is Action.OpenRecoveryEmail -> onOpenRecoveryEmail()
                is Action.OpenReport -> onOpenReport()
                is Action.OpenSecurityKeys -> onOpenSecurityKeys()
                is Action.OpenSubscription -> onOpenSubscription()
                is Action.RequestNotificationPermission -> onRequestNotificationPermission()
                is Action.SignIn -> onSignIn(action.userId)
                is Action.SwitchToAccount -> onSwitchToAccount(action.userId)
            }
        }
    }

    private fun onAddAccount() {
        authOrchestrator.startAddAccountWorkflow()
    }

    private fun onOpenPasswordManagement() {
        TODO("ET - Not yet implemented")
    }

    private fun onOpenRecoveryEmail() {
        TODO("ET - Not yet implemented")
    }

    private fun onOpenReport() {
        TODO("ET - Not yet implemented")
    }

    private fun onOpenSubscription() {
        TODO("ET - Not yet implemented")
    }

    private fun onOpenSecurityKeys() {
        TODO("ET - Not yet implemented")
    }

    private fun onSignIn(userId: UserId?) = viewModelScope.launch {
        val address = userId?.let {
            userSessionRepository.getAccount(it)?.primaryAddress
        }
        authOrchestrator.startLoginWorkflow(LoginInput(username = address))
    }

    private fun onSwitchToAccount(userId: UserId) = viewModelScope.launch {
        setPrimaryAccount(userId)
    }

    private fun onRequestNotificationPermission() {
        notificationsPermissionOrchestrator.requestPermissionIfRequired()
    }

    sealed interface Action {

        data object AddAccount : Action
        data object OpenPasswordManagement : Action
        data object OpenRecoveryEmail : Action
        data object OpenReport : Action
        data object OpenSecurityKeys : Action
        data object OpenSubscription : Action
        data object RequestNotificationPermission : Action
        data class SignIn(val userId: UserId?) : Action
        data class SwitchToAccount(val userId: UserId) : Action
    }
}
