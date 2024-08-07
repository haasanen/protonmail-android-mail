/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.auth.presentation.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uniffi.proton_mail_uniffi.LoginFlowException
import uniffi.proton_mail_uniffi.MailSessionInterface
import uniffi.proton_mail_uniffi.MailUserSession
import java.io.Closeable
import javax.inject.Inject

/**
 * Performs the login process.
 * Usage:
 * 1. Observe the [state].
 * 2. [invoke] a [Action.LogIn] action.
 * 3. React according to the received [state].
 * 4. Call [close] when the instance is no longer needed.
 */
public class PerformLogin @Inject constructor(
    private val session: MailSessionInterface
) : Closeable {
    private val loginFlow = LazySuspendable { session.newLoginFlow() }

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    public val state: StateFlow<State> = _state.asStateFlow()

    override fun close() {
        loginFlow.value?.close()
    }

    public suspend operator fun invoke(action: Action) {
        try {
            when (action) {
                is Action.LogIn -> onLogin(action)
                is Action.SubmitTotp -> onSubmitTotp(action)
            }
        } catch (e: LoginFlowException) {
            _state.emit(State.Error(cause = e))
        }
    }

    private suspend fun onLogin(action: Action.LogIn) {
        _state.emit(State.LoggingIn)
        loginFlow().login(email = action.name, password = action.password)
        checkLoginFlowResult()
    }

    private suspend fun onSubmitTotp(action: Action.SubmitTotp) {
        check(loginFlow().isAwaiting2fa()) { "The login flow is not awaiting for 2FA." }
        _state.emit(State.SubmittingTotp)
        loginFlow().submitTotp(action.code)
        checkLoginFlowResult()
    }

    private suspend fun checkLoginFlowResult() {
        when {
            loginFlow().isAwaiting2fa() -> State.Awaiting2fa
            loginFlow().isLoggedIn() -> State.LoggedIn(loginFlow().toUserContext())
            else -> State.Idle
        }.also {
            _state.emit(it)
        }
    }

    public sealed class Action {
        public data class LogIn(val name: String, val password: String) : Action()
        public data class SubmitTotp(public val code: String) : Action()
    }

    public sealed class State {
        public data object Idle : State()
        public data object LoggingIn : State()
        public data object Awaiting2fa : State()
        public data object SubmittingTotp : State()
        public data class Error(val cause: LoginFlowException) : State()
        public data class LoggedIn(val session: MailUserSession) : State()
    }
}

/** Similar to [kotlin.lazy], but allows to have a suspendable [initializer]. */
private class LazySuspendable<T : Any>(val initializer: suspend () -> T) {
    private val mutex = Mutex()
    var value: T? = null

    suspend operator fun invoke(): T = mutex.withLock {
        value ?: initializer().also { value = it }
    }
}
