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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.LogTag
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
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.util.kotlin.CoreLogger
import uniffi.proton_mail_uniffi.LoginError
import uniffi.proton_mail_uniffi.LoginFlow
import uniffi.proton_mail_uniffi.LoginFlowToUserContextResult
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetAccountResult
import uniffi.proton_mail_uniffi.MailSessionGetAccountSessionsResult
import uniffi.proton_mail_uniffi.MailSessionResumeLoginFlowResult
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import uniffi.proton_mail_uniffi.VoidLoginResult
import javax.inject.Inject

@HiltViewModel
class OneTimePasswordInputViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val sessionInterface: MailSession
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<OneTimePasswordInputAction>(Load())

    val state: StateFlow<OneTimePasswordInputState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is Load -> onLoad()
            is Close -> onClose()
            is Authenticate -> onValidateAndAuthenticate(action)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Idle)

    fun submit(action: OneTimePasswordInputAction) = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onLoad(): Flow<OneTimePasswordInputState> = flow {
        emit(Idle)
    }

    private fun onClose(): Flow<OneTimePasswordInputState> = flow {
        sessionInterface.deleteAccount(userId)
        emit(Closed)
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
                    is VoidLoginResult.Error -> emitAll(onError(submit.v1))
                    is VoidLoginResult.Ok -> emitAll(onSuccess(loginFlow.v1))
                }
            }
        }
    }

    private fun onError(error: LoginError): Flow<OneTimePasswordInputState> = flow {
        emit(Error.LoginFlow(error.getErrorMessage(context)))

        when ((error as? LoginError.Other)?.v1) {
            ProtonError.SessionExpired,
            is ProtonError.Unexpected -> emitAll(onClose())

            else -> Unit
        }
    }

    private fun onSuccess(loginFlow: LoginFlow): Flow<OneTimePasswordInputState> = flow {
        when (loginFlow.isAwaitingMailboxPassword()) {
            true -> emit(Awaiting2Pass)
            false -> when (val result = loginFlow.toUserContext()) {
                is LoginFlowToUserContextResult.Error -> emitAll(onError(result.v1))
                is LoginFlowToUserContextResult.Ok -> emit(LoggedIn)
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
