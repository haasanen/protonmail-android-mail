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
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.navigation.model.LauncherState
import ch.protonmail.android.navigation.model.LauncherState.AccountNeeded
import ch.protonmail.android.navigation.model.LauncherState.PrimaryExist
import ch.protonmail.android.navigation.model.LauncherState.Processing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@SuppressWarnings("NotImplementedDeclaration", "UnusedPrivateMember")
class RustLauncherViewModel @Inject constructor(
    userSessionRepository: UserSessionRepository
) : BaseLauncherViewModel() {

    override val state: StateFlow<LauncherState> = userSessionRepository.observeCurrentUserId()
        .mapLatest { userId ->
            if (userId == null) {
                Timber.d("rust-launcher: User session not found!")
                AccountNeeded
            } else {
                Timber.d("rust-launcher: User session found.")
                PrimaryExist
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = Processing
        )

    override fun register(context: AppCompatActivity) {
        Timber.d("rust-launcher: Not implemented in ET.")
    }

    override fun submit(action: Action) {
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

    override fun onAddAccount() {
        TODO("ET - Not yet implemented")
    }

    override suspend fun onOpenPasswordManagement() {
        TODO("ET - Not yet implemented")
    }

    override suspend fun onOpenRecoveryEmail() {
        TODO("ET - Not yet implemented")
    }

    override suspend fun onOpenReport() {
        TODO("ET - Not yet implemented")
    }

    override suspend fun onOpenSubscription() {
        TODO("ET - Not yet implemented")
    }

    override suspend fun onSignIn(userId: UserId?) {
        TODO("ET - Not yet implemented")
    }

    override suspend fun onSwitch(userId: UserId) {
        TODO("ET - Not yet implemented")
    }

}
