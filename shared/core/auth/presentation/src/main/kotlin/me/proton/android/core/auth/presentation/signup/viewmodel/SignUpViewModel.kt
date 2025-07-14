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

package me.proton.android.core.auth.presentation.signup.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import me.proton.android.core.auth.presentation.LogTag
import me.proton.android.core.auth.presentation.login.getErrorMessage
import me.proton.android.core.auth.presentation.signup.CreatePasswordAction
import me.proton.android.core.auth.presentation.signup.CreatePasswordState
import me.proton.android.core.auth.presentation.signup.CreateRecoveryAction
import me.proton.android.core.auth.presentation.signup.CreateRecoveryState
import me.proton.android.core.auth.presentation.signup.CreateUsernameAction
import me.proton.android.core.auth.presentation.signup.CreateUsernameState
import me.proton.android.core.auth.presentation.signup.SignUpAction
import me.proton.android.core.auth.presentation.signup.SignUpAction.CreateUser
import me.proton.android.core.auth.presentation.signup.SignUpAction.FinalizeSignup
import me.proton.android.core.auth.presentation.signup.SignUpState
import me.proton.android.core.auth.presentation.signup.SignUpState.LoginSuccess
import me.proton.android.core.auth.presentation.signup.SignUpState.SignUpError
import me.proton.android.core.auth.presentation.signup.SignUpState.SignUpSuccess
import me.proton.android.core.auth.presentation.signup.SignUpState.SigningUp
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.presentation.savedstate.state
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import uniffi.proton_account_uniffi.PasswordValidatorService
import uniffi.proton_account_uniffi.SignupException
import uniffi.proton_account_uniffi.SignupFlow
import uniffi.proton_account_uniffi.SignupFlowCompleteResult
import uniffi.proton_account_uniffi.SignupFlowCreateResult
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetAccountResult
import uniffi.proton_mail_uniffi.MailSessionGetAccountSessionsResult
import uniffi.proton_mail_uniffi.MailSessionNewSignupFlowResult
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import javax.inject.Inject

@Suppress("TooGenericExceptionCaught")
@HiltViewModel
class SignUpViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    requiredAccountType: AccountType,
    private val sessionInterface: MailSession
) : BaseViewModel<SignUpAction, SignUpState>(
    initialAction = CreateUsernameAction.LoadData(
        savedStateHandle.get<AccountType>("accountType") ?: requiredAccountType
    ),
    initialState = CreateUsernameState.Load(savedStateHandle.get<AccountType>("accountType") ?: requiredAccountType),
    sharingStarted = SharingStarted.Lazily
) {

    private var currentAccountType: AccountType by savedStateHandle.state(requiredAccountType)
    private val signUpFlowDeferred = viewModelScope.async {
        sessionInterface.newSignupFlow()
    }
    private val usernameHandler = UsernameHandler.create(
        getFlow = { getSignUpFlow() },
        getCurrentAccountType = { currentAccountType },
        updateAccountType = { type -> currentAccountType = type }
    )

    private val passwordHandler = PasswordHandler.create(
        getFlow = { getSignUpFlow() }
    )

    private val recoveryHandler = RecoveryHandler.create(
        getFlow = { getSignUpFlow() }
    )

    @Inject
    internal lateinit var scopeProvider: CoroutineScopeProvider

    private suspend fun getSignUpFlow(): SignupFlow {
        return when (val result = signUpFlowDeferred.await()) {
            is MailSessionNewSignupFlowResult.Ok -> result.v1
            is MailSessionNewSignupFlowResult.Error -> error(result.v1.getErrorMessage(context))
        }
    }

    override fun onAction(action: SignUpAction) = when (action) {
        is CreateUsernameAction -> usernameHandler.handleAction(action)
        is CreatePasswordAction -> passwordHandler.handleAction(action)
        is CreateRecoveryAction -> recoveryHandler.handleAction(action)
        is CreateUser -> handleCreateUser()
        is FinalizeSignup -> finalizeSignUp()
        else -> emptyFlow()
    }

    override suspend fun FlowCollector<SignUpState>.onError(throwable: Throwable) {
        val currentState = state.value
        val errorState = when (currentState) {
            is CreateUsernameState -> usernameHandler.handleError(throwable)
            is CreatePasswordState -> passwordHandler.handleError(throwable)
            is CreateRecoveryState -> recoveryHandler.handleError(throwable)
            else -> SignUpError(throwable.message)
        }
        emit(errorState)
    }

    suspend fun getPasswordValidatorService(): PasswordValidatorService =
        requireNotNull(getSignUpFlow().passwordValidator()) {
            "Could not get password validator service."
        }

    private fun handleCreateUser() = flow {
        emit(SigningUp)
        when (val result = getSignUpFlow().create()) {
            is SignupFlowCreateResult.Error -> emitAll(result.v1.onSignUpError())
            is SignupFlowCreateResult.Ok -> emit(SignUpSuccess)
        }
    }

    private fun finalizeSignUp() = flow {
        when (val result = getSignUpFlow().complete()) {
            is SignupFlowCompleteResult.Error -> {
                emitAll(result.v1.onSignUpError())
            }

            is SignupFlowCompleteResult.Ok -> {
                val userId = result.v1.userId
                getSession(getAccount(userId))?.firstOrNull()
                emit(LoginSuccess(userId))
                clearUp()
            }
        }
    }

    private fun SignupException.onSignUpError() = flow {
        getSignUpFlow().stepBack()
        emit(SignUpError(message = getErrorMessage()))
    }

    private suspend fun getSession(account: StoredAccount?): List<StoredSession>? {
        if (account == null) {
            return null
        }

        return when (val result = sessionInterface.getAccountSessions(account)) {
            is MailSessionGetAccountSessionsResult.Error -> {
                CoreLogger.e(LogTag.SIGNUP, "Failed to get account sessions: ${result.v1}")
                null
            }

            is MailSessionGetAccountSessionsResult.Ok -> result.v1
        }
    }

    private suspend fun getAccount(userId: String): StoredAccount? =
        when (val result = sessionInterface.getAccount(userId)) {
            is MailSessionGetAccountResult.Error -> {
                CoreLogger.e(LogTag.SIGNUP, "Failed to get account: ${result.v1}")
                null
            }

            is MailSessionGetAccountResult.Ok -> result.v1
        }

    private suspend fun clearUp() {
        try {
            getSignUpFlow().destroy()
        } catch (e: Exception) {
            CoreLogger.e(LogTag.SIGNUP, "Error destroying signup flow: ${e.message}")
        }
    }
}

interface ErrorHandler {

    fun handleError(throwable: Throwable): SignUpState
}

fun SignupException.getErrorMessage(): String? {
    return when (this) {
        is SignupException.Api -> v1.takeIf { it.isNotEmpty() } ?: message
        is SignupException.Crypto -> v1.takeIf { it.isNotEmpty() } ?: message
        else -> message
    }
}
