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

package me.proton.android.core.auth.presentation.secondfactor.otp

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.android.core.auth.presentation.LogTag
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.login.getErrorMessage
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorArg.getUserId
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputAction.Authenticate
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputAction.Close
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputAction.Load
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputState.Awaiting2Pass
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputState.Closed
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputState.Error
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputState.Idle
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputState.Loading
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputState.LoggedIn
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.util.kotlin.CoreLogger
import uniffi.proton_account_uniffi.LoginError
import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_account_uniffi.LoginFlowSubmitTotpResult
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetAccountResult
import uniffi.proton_mail_uniffi.MailSessionGetAccountSessionsResult
import uniffi.proton_mail_uniffi.MailSessionResumeLoginFlowResult
import uniffi.proton_mail_uniffi.MailSessionToUserSessionResult
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import javax.inject.Inject

@HiltViewModel
class OneTimePasswordInputViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val sessionInterface: MailSession
) : BaseViewModel<OneTimePasswordInputAction, OneTimePasswordInputState>(
    initialState = Idle,
    initialAction = Load()
) {

    private val userId by lazy { savedStateHandle.getUserId() }

    override suspend fun FlowCollector<OneTimePasswordInputState>.onError(throwable: Throwable) {
        emit(Error.LoginFlow(throwable.message))
    }

    override fun onAction(action: OneTimePasswordInputAction): Flow<OneTimePasswordInputState> {
        return when (action) {
            is Load -> onLoad()
            is Close -> onClose()
            is Authenticate -> onValidateAndAuthenticate(action)
        }
    }

    private fun onLoad(): Flow<OneTimePasswordInputState> = flow {
        emit(Idle)
    }

    private fun onClose(message: String? = null): Flow<OneTimePasswordInputState> = flow {
        sessionInterface.deleteAccount(userId)
        emit(Closed(message = message))
    }

    private fun onValidateAndAuthenticate(action: Authenticate) = flow {
        emit(Loading)
        when (action.code.isBlank()) {
            true -> emit(Error.Validation)
            false -> emitAll(onAuthenticate(action))
        }
    }

    private fun onAuthenticate(action: Authenticate): Flow<OneTimePasswordInputState> = flow {
        emit(Loading)
        val session = getSession(getAccount(userId))?.firstOrNull()
        val loginFlow =
            session?.let { sessionInterface.resumeLoginFlow(userId, session.sessionId()) }
        when (loginFlow) {
            null -> emitAll(onClose())
            is MailSessionResumeLoginFlowResult.Error -> emitAll(onError(loginFlow.v1))
            is MailSessionResumeLoginFlowResult.Ok -> {
                when (val submit = loginFlow.v1.submitTotp(action.code)) {
                    is LoginFlowSubmitTotpResult.Error -> emitAll(onSubmitTotpError(submit, loginFlow.v1))
                    is LoginFlowSubmitTotpResult.Ok -> emitAll(onSuccess(loginFlow.v1))
                }
            }
        }
    }

    private fun onSubmitTotpError(err: LoginFlowSubmitTotpResult.Error, loginFlow: LoginFlow) = flow {
        if (loginFlow.isAwaiting2fa()) {
            emitAll(onError(err.v1))
        } else {
            emitAll(onClose(message = context.getString(R.string.auth_second_factor_incorrect_code)))
        }
    }

    private fun onError(error: ProtonError): Flow<OneTimePasswordInputState> = flow {
        emit(Error.LoginFlow(error.getErrorMessage(context)))

        when (error) {
            is ProtonError.Unexpected -> emitAll(onClose())
            else -> Unit
        }
    }

    private fun onError(error: LoginError): Flow<OneTimePasswordInputState> = flow {
        emit(Error.LoginFlow(error.getErrorMessage(context)))
    }

    private fun onSuccess(loginFlow: LoginFlow): Flow<OneTimePasswordInputState> = flow {
        when (loginFlow.isAwaitingMailboxPassword()) {
            true -> emit(Awaiting2Pass)
            false -> when (val result = sessionInterface.toUserSession(loginFlow)) {
                is MailSessionToUserSessionResult.Error -> emit(Error.LoginFlow(result.v1.getErrorMessage(context)))
                is MailSessionToUserSessionResult.Ok -> emit(LoggedIn)
            }
        }
    }

    private suspend fun getSession(account: StoredAccount?): List<StoredSession>? {
        if (account == null) {
            return null
        }

        return when (val result = sessionInterface.getAccountSessions(account)) {
            is MailSessionGetAccountSessionsResult.Error -> {
                CoreLogger.e(LogTag.LOGIN, result.v1.toString())
                null
            }

            is MailSessionGetAccountSessionsResult.Ok -> result.v1
        }
    }

    private suspend fun getAccount(userId: String) = when (val result = sessionInterface.getAccount(userId)) {
        is MailSessionGetAccountResult.Error -> {
            CoreLogger.e(LogTag.LOGIN, result.v1.toString())
            null
        }

        is MailSessionGetAccountResult.Ok -> result.v1
    }
}
