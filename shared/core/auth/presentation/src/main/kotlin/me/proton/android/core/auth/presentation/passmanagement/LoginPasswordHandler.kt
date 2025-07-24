/*
 * Copyright (c) 2025 Proton Technologies AG
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

package me.proton.android.core.auth.presentation.passmanagement

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.auth.data.entity.PasswordValidatorTokenWrapper
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateLoginPassword
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateLoginPassword.SaveLoginPassword
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateLoginPassword.TwoFaComplete
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementState.Error
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementState.LoginPasswordSaved
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementState.UserInput
import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken
import uniffi.proton_account_uniffi.PasswordFlow
import uniffi.proton_account_uniffi.PasswordFlowChangePassResult
import uniffi.proton_account_uniffi.PasswordFlowSubmitPassResult
import uniffi.proton_account_uniffi.SimplePasswordState.COMPLETE
import uniffi.proton_account_uniffi.SimplePasswordState.WANT_CHANGE
import uniffi.proton_account_uniffi.SimplePasswordState.WANT_TFA

class LoginPasswordHandler private constructor(
    private val getUserId: () -> CoreUserId?,
    private val getFlow: suspend () -> PasswordFlow
) : ErrorHandler {

    fun handleAction(action: UpdateLoginPassword, currentState: UserInput): Flow<PasswordManagementState> =
        when (action) {
            is SaveLoginPassword -> {
                val updatedState = currentState.updateLoginPassword(
                    current = action.currentPassword,
                    new = action.newPassword,
                    confirmNew = action.confirmPassword
                )
                saveLoginPassword(updatedState, action.token)
            }

            is TwoFaComplete -> handleTwoFaResult(action.result, currentState, action.token)
        }

    private fun handleTwoFaResult(
        result: Boolean,
        currentState: UserInput,
        token: PasswordValidatorToken?
    ): Flow<PasswordManagementState> = flow {
        if (result) {
            emitAll(submitPassChange(currentState, token))
        } else {
            emit(currentState.copyWithLoginPassword { it.copy(loading = false) })
            getFlow().stepBack()
        }
    }

    private fun saveLoginPassword(
        currentState: UserInput,
        token: PasswordValidatorToken?
    ): Flow<PasswordManagementState> = flow {
        emit(currentState.copyWithLoginPassword { it.copy(loading = true) })
        if (currentState.loginPassword.current.isBlank()) {
            emit(currentState.setPasswordValidationError(ValidationError.CurrentPasswordEmpty))
            return@flow
        }
        if (token == null) {
            emit(currentState.setPasswordValidationError(ValidationError.PasswordEmpty))
            return@flow
        }

        val submitResult = submitCurrentPassword(currentState.loginPassword.current, currentState) ?: return@flow

        handlePasswordSubmitResult(submitResult, currentState, token)
    }

    private suspend fun FlowCollector<PasswordManagementState>.submitCurrentPassword(
        currentPassword: String,
        currentState: UserInput
    ): PasswordFlowSubmitPassResult? {
        return runCatching {
            getFlow().submitPass(currentPassword)
        }.getOrElse { exception ->
            emit(Error.General(exception.message ?: "Unknown error", currentState))
            null
        }
    }

    private suspend fun FlowCollector<PasswordManagementState>.handlePasswordSubmitResult(
        submitResult: PasswordFlowSubmitPassResult,
        currentState: UserInput,
        token: PasswordValidatorToken?
    ) {
        when (submitResult) {
            is PasswordFlowSubmitPassResult.Error -> {
                emit(Error.General(submitResult.v1.toString(), currentState))
            }

            is PasswordFlowSubmitPassResult.Ok -> {
                when (submitResult.v1) {
                    WANT_TFA -> handleTwoFactorAuthentication(currentState, token)
                    WANT_CHANGE -> emitAll(submitPassChange(currentState, token))
                    else -> emit(Error.InvalidState(currentState))
                }
            }
        }
    }

    private suspend fun FlowCollector<PasswordManagementState>.handleTwoFactorAuthentication(
        currentState: UserInput,
        token: PasswordValidatorToken?
    ) {
        val userId = getUserId()
        if (userId == null) {
            emit(Error.General("User ID not available", currentState))
            return
        }
        emit(PasswordManagementState.Awaiting2faForLogin(userId, currentState, token))
    }

    private fun submitPassChange(
        currentState: UserInput,
        token: PasswordValidatorToken?
    ): Flow<PasswordManagementState> = flow {
        emit(currentState.copyWithLoginPassword { it.copy(loading = true) })

        val loginPasswordState = currentState.loginPassword
        val changeResult = attemptPasswordChange(loginPasswordState, token, currentState) ?: return@flow

        handlePasswordChangeResult(changeResult, currentState)
    }

    private suspend fun FlowCollector<PasswordManagementState>.attemptPasswordChange(
        loginPasswordState: LoginPasswordState,
        token: PasswordValidatorToken?,
        currentState: UserInput
    ): PasswordFlowChangePassResult? {
        return runCatching {
            getFlow().changePass(
                newPass = loginPasswordState.new,
                confirmPassword = loginPasswordState.confirmNew,
                token = (token as? PasswordValidatorTokenWrapper)?.toRust()
            )
        }.getOrElse { exception ->
            emit(Error.General(exception.message ?: "Password change failed", currentState))
            null
        }
    }

    private suspend fun FlowCollector<PasswordManagementState>.handlePasswordChangeResult(
        changeResult: PasswordFlowChangePassResult,
        currentState: UserInput
    ) {
        when (changeResult) {
            is PasswordFlowChangePassResult.Error -> {
                val validationError = changeResult.v1.mapToValidationError()
                if (validationError != null) {
                    emit(currentState.setPasswordValidationError(validationError))
                } else {
                    emit(Error.General(changeResult.v1.toString(), currentState))
                }
            }

            is PasswordFlowChangePassResult.Ok -> {
                when (changeResult.v1) {
                    COMPLETE -> emit(LoginPasswordSaved)
                    else -> emit(Error.InvalidState(currentState))
                }
            }
        }
    }

    override fun handleError(throwable: Throwable, currentState: UserInput): PasswordManagementState =
        Error.General(error = throwable.message ?: "Unknown error occurred", currentState)

    companion object {

        fun create(getFlow: suspend () -> PasswordFlow, getUserId: () -> CoreUserId?): LoginPasswordHandler =
            LoginPasswordHandler(getUserId, getFlow)
    }
}

private fun UserInput.copyWithLoginPassword(transform: (LoginPasswordState) -> LoginPasswordState): UserInput =
    copy(loginPassword = transform(loginPassword))

private fun UserInput.updateLoginPassword(
    current: String,
    new: String,
    confirmNew: String
): UserInput = copy(
    loginPassword = loginPassword.copy(
        current = current,
        new = new,
        confirmNew = confirmNew
    )
)

private fun UserInput.setPasswordValidationError(error: ValidationError): UserInput =
    copyWithLoginPassword { it.copy(validationError = error, loading = false) }
