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

package me.proton.android.core.auth.presentation.secondfactor.fido

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.android.core.auth.presentation.login.getErrorMessage
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorArg.getUserId
import me.proton.android.core.auth.presentation.secondfactor.SecondFactorFlowManager
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputAction.Authenticate
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputAction.Load
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputAction.SecurityKeyResult
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputState.Closed
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputState.Error
import me.proton.android.core.auth.presentation.secondfactor.fido.Fido2InputState.Idle
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationOptions
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.Result
import me.proton.core.compose.viewmodel.BaseViewModel
import uniffi.proton_account_uniffi.LoginFlow
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionToUserSessionResult
import uniffi.proton_mail_uniffi.ProtonError
import javax.inject.Inject

@Suppress("TooGenericExceptionCaught")
@HiltViewModel
class Fido2InputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val getFidoOptions: GetFidoOptions,
    private val submitFido: SubmitFido,
    private val sessionInterface: MailSession,
    private val secondFactorFlowManager: SecondFactorFlowManager
) : BaseViewModel<Fido2InputAction, Fido2InputState>(
    initialAction = Load(),
    initialState = Idle,
    sharingStarted = SharingStarted.Lazily
) {

    private val userId by lazy { savedStateHandle.getUserId() }

    override fun onAction(action: Fido2InputAction): Flow<Fido2InputState> {
        return when (action) {
            is Load -> flowOf(Idle)
            is Authenticate -> onInitiatedReadingSecurityKey()
            is SecurityKeyResult -> handleSecurityKeyResult(action.result, action.proof)
            is Fido2InputAction.ReadSecurityKey -> onReadSecurityKey(action.options)
        }
    }

    override suspend fun FlowCollector<Fido2InputState>.onError(throwable: Throwable) {
        emit(Error.General(throwable.message))
    }

    private fun onError(error: ProtonError) = flow {
        emit(Error.SubmitFido(error.getErrorMessage(context)))
        when (error) {
            is ProtonError.Unexpected -> emitAll(onClose())
            else -> Unit
        }
    }

    private fun onClose(): Flow<Fido2InputState> = flow {
        sessionInterface.deleteAccount(userId)
        emit(Closed)
    }

    private fun onInitiatedReadingSecurityKey() = flow {
        emit(Fido2InputState.InitiatedReadingSecurityKey)

        val fido2Options = getFidoOptions.invoke(userId)
        val authenticationOptions = fido2Options?.authenticationOptions?.toNative()
        if (authenticationOptions != null) {
            perform(Fido2InputAction.ReadSecurityKey(authenticationOptions))
        } else {
            emit(Error.StoredKeysConfig)
        }
    }

    private fun onReadSecurityKey(options: Fido2AuthenticationOptions) = flow {
        emit(Fido2InputState.ReadingSecurityKey(options))
    }

    private fun handleSecurityKeyResult(result: Result, secondFactorProof: SecondFactorProof.Fido2?) = flow {
        when (result) {
            is Result.Success -> handleSuccessfulSecurityKeyRead(secondFactorProof)
            is Result.Error -> emit(Error.ReadingSecurityKey.Message(result.error.message))
            is Result.Cancelled -> emit(Error.ReadingSecurityKey.Cancelled)
            is Result.EmptyResult -> emit(Error.ReadingSecurityKey.Empty)
            is Result.NoCredentialsResponse -> emit(Error.ReadingSecurityKey.NoCredentials)
            is Result.UnknownResult -> emit(Error.ReadingSecurityKey.Unknown)
        }
    }

    private suspend fun FlowCollector<Fido2InputState>.handleSuccessfulSecurityKeyRead(
        secondFactorProof: SecondFactorProof.Fido2?
    ) {
        if (secondFactorProof == null) {
            emit(Error.ReadingSecurityKey.Empty)
        } else {
            emit(Fido2InputState.Authenticating)
            emitAll(submitFido2(secondFactorProof))
        }
    }

    private fun submitFido2(proof: SecondFactorProof.Fido2) = flow {
        when (val result = submitFido.execute(userId, proof)) {
            is SubmitFidoResult.Success ->
                if (result.loginFlow != null)
                    emitAll(handleLoginSuccess(result.loginFlow))
                else emit(Fido2InputState.LoggedIn)

            is SubmitFidoResult.ProtonError -> emitAll(onError(result.error))
            is SubmitFidoResult.SessionClosed -> emitAll(onClose())
            is SubmitFidoResult.OtherError -> emit(Error.General(result.message))
        }
    }

    private fun handleLoginSuccess(loginFlow: LoginFlow): Flow<Fido2InputState> = flow {
        when (loginFlow.isAwaitingMailboxPassword()) {
            true -> emit(Fido2InputState.Awaiting2Pass)
            false -> handleUserContextConversion(loginFlow)
        }
    }

    private suspend fun FlowCollector<Fido2InputState>.handleUserContextConversion(loginFlow: LoginFlow) {
        when (val result = submitFido.convertToUserContext(loginFlow)) {
            is MailSessionToUserSessionResult.Error -> emit(Error.General(result.v1.getErrorMessage(context)))
            is MailSessionToUserSessionResult.Ok -> emit(Fido2InputState.LoggedIn)
        }
    }
}
