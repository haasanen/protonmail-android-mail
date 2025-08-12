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

package me.proton.android.core.auth.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.android.core.auth.presentation.IODispatcher
import me.proton.android.core.auth.presentation.challenge.toUserBehavior
import me.proton.android.core.auth.presentation.flow.UiEventFlow
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import uniffi.proton_account_uniffi.LoginError
import uniffi.proton_account_uniffi.LoginFlowLoginResult
import uniffi.proton_account_uniffi.LoginFlowUserIdResult
import uniffi.proton_mail_uniffi.LoginScreenId
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetSessionResult
import uniffi.proton_mail_uniffi.MailSessionNewLoginFlowResult
import uniffi.proton_mail_uniffi.MailSessionToUserSessionResult
import uniffi.proton_mail_uniffi.StoredSession
import uniffi.proton_mail_uniffi.recordLoginScreenView
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val sessionInterface: MailSession,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val loginFlowResult = viewModelScope.async { sessionInterface.newLoginFlow() }
    private val mutableState: MutableStateFlow<LoginViewState> = MutableStateFlow(LoginViewState.Idle)

    val state: StateFlow<LoginViewState> = mutableState.asStateFlow()

    val uiEvent: UiEventFlow<LoginEvent> = UiEventFlow()

    fun submit(action: LoginAction) {
        viewModelScope.launch {
            when (action) {
                is LoginAction.Login -> onLogin(action)
                is LoginAction.Close -> onClose()
            }
        }
    }

    private suspend fun getLoginFlow() = (loginFlowResult.await() as MailSessionNewLoginFlowResult.Ok).v1

    private suspend fun onLogin(action: LoginAction.Login) {
        val loginFlowResult = loginFlowResult.await()
        return when {
            action.username.isBlank() -> mutableState.emit(LoginViewState.Error.Validation)
            loginFlowResult is MailSessionNewLoginFlowResult.Error -> {
                uiEvent.emit(LoginEvent.FailedToLogin(loginFlowResult.v1.getErrorMessage(context)))
            }

            else -> performLogin(
                username = action.username,
                usernameFrameDetails = action.usernameFrameDetails,
                password = action.password
            )
        }
    }

    private suspend fun performLogin(
        username: String,
        usernameFrameDetails: ChallengeFrameDetails,
        password: String
    ) = withContext(ioDispatcher) {
        mutableState.emit(LoginViewState.LoggingIn)

        val result = getLoginFlow().login(
            username = username,
            password = password,
            userBehavior = usernameFrameDetails.toUserBehavior()
        )
        when (result) {
            is LoginFlowLoginResult.Error -> mutableState.emit(getErrorState(result.v1))
            is LoginFlowLoginResult.Ok -> onSuccess()
        }
    }

    private suspend fun onSuccess() {
        getLoginViewState()
    }

    private suspend fun getErrorState(error: LoginError): LoginViewState {
        return when (error) {
            is LoginError.DuplicateSession -> {
                sessionInterface.getSession(error.v1).getOrNull()
                    ?.let { LoginViewState.Error.AlreadyLoggedIn(it.userId()) }
                    ?: uiEvent.emit(
                        LoginEvent.FailedToLogin(error.getErrorMessage(context))
                    ).run { LoginViewState.Idle }
            }

            else -> {
                uiEvent.emit(LoginEvent.FailedToLogin(error.getErrorMessage(context)))
                LoginViewState.Idle
            }
        }
    }

    private suspend fun getLoginViewState(): LoginViewState {
        val userId: String = when (val result = getLoginFlow().userId()) {
            is LoginFlowUserIdResult.Error -> {
                return when (val error = result.v1) {
                    is LoginError.DuplicateSession -> {
                        val session = sessionInterface.getSession(error.v1).getOrNull()
                        if (session != null) {
                            LoginViewState.Error.AlreadyLoggedIn(session.userId())
                        } else {
                            uiEvent.emit(LoginEvent.FailedToLogin(error.getErrorMessage(context)))
                            LoginViewState.Idle
                        }
                    }

                    else -> {
                        uiEvent.emit(LoginEvent.FailedToLogin(error.getErrorMessage(context)))
                        LoginViewState.Idle
                    }
                }
            }

            is LoginFlowUserIdResult.Ok -> result.v1
        }
        return when {
            getLoginFlow().isAwaitingMailboxPassword() -> onTwoPass(userId)
            getLoginFlow().isAwaiting2fa() -> onTwoFa(userId)
            getLoginFlow().isLoggedIn() -> onLoggedIn(userId)
            else -> LoginViewState.Idle
        }
    }

    private fun onTwoPass(userId: String): LoginViewState.Awaiting2Pass = LoginViewState.Awaiting2Pass(userId)

    private fun onTwoFa(userId: String): LoginViewState.Awaiting2fa = LoginViewState.Awaiting2fa(userId)

    private suspend fun onLoggedIn(userId: String): LoginViewState {
        val loginFlow = getLoginFlow()
        return when (val result = sessionInterface.toUserSession(loginFlow)) {
            is MailSessionToUserSessionResult.Error -> {
                uiEvent.emit(LoginEvent.FailedToLogin("${result.v1}"))
                LoginViewState.Idle
            }

            is MailSessionToUserSessionResult.Ok -> LoginViewState.LoggedIn(userId)
        }
    }

    private suspend fun onClose() {
        getLoginFlow().destroy()
    }

    fun onScreenView() = viewModelScope.launch {
        recordLoginScreenView(LoginScreenId.SIGN_IN_WITH_USERNAME_PASSWORD)
    }

    private inline fun MailSessionGetSessionResult.getOrNull(): StoredSession? {
        return when (this) {
            is MailSessionGetSessionResult.Ok -> this.v1
            is MailSessionGetSessionResult.Error -> null
        }
    }
}
