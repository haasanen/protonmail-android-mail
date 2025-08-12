/*
 * Copyright (c) 2024 Proton AG
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

package ch.protonmail.android.mailsession.presentation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.repository.onAccountState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UserSessionObserver(
    internal val userSessionRepository: UserSessionRepository,
    internal val lifecycle: Lifecycle,
    internal val minActiveState: Lifecycle.State = Lifecycle.State.CREATED
) {

    internal val scope = lifecycle.coroutineScope

    internal fun addAccountStateListener(
        state: AccountState,
        initialState: Boolean,
        block: suspend (Account) -> Unit
    ) {
        userSessionRepository.onAccountState(state, initialState = initialState)
            .flowWithLifecycle(lifecycle, minActiveState)
            .onEach { block(it) }
            .launchIn(scope)
    }
}

fun UserSessionRepository.observe(lifecycle: Lifecycle, minActiveState: Lifecycle.State = Lifecycle.State.CREATED) =
    UserSessionObserver(this, lifecycle, minActiveState)

fun UserSessionObserver.onAccountReady(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): UserSessionObserver {
    addAccountStateListener(AccountState.Ready, initialState, block)
    return this
}

fun UserSessionObserver.onAccountDisabled(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): UserSessionObserver {
    addAccountStateListener(AccountState.Disabled, initialState, block)
    return this
}

fun UserSessionObserver.onAccountTwoFactorNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): UserSessionObserver {
    addAccountStateListener(AccountState.TwoFactorNeeded, initialState, block)
    return this
}

fun UserSessionObserver.onAccountTwoPasswordNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): UserSessionObserver {
    addAccountStateListener(AccountState.TwoPasswordNeeded, initialState, block)
    return this
}

fun UserSessionObserver.onAccountNewPasswordNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): UserSessionObserver {
    addAccountStateListener(AccountState.NewPassNeeded, initialState, block)
    return this
}
