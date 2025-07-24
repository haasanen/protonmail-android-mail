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
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateMailboxPassword
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateMailboxPassword.SaveMailboxPassword
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateMailboxPassword.TwoFaComplete
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementState.Awaiting2faForMailbox
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementState.Error
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementState.MailboxPasswordSaved
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementState.UserInput
import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken
import uniffi.proton_account_uniffi.PasswordFlow
import uniffi.proton_account_uniffi.PasswordFlowChangeMboxPassResult
import uniffi.proton_account_uniffi.PasswordFlowSubmitPassResult
import uniffi.proton_account_uniffi.SimplePasswordState.COMPLETE
import uniffi.proton_account_uniffi.SimplePasswordState.WANT_CHANGE
import uniffi.proton_account_uniffi.SimplePasswordState.WANT_TFA

class MailboxPasswordHandler private constructor(
    private val getUserId: () -> CoreUserId?,
    private val getFlow: suspend () -> PasswordFlow
) : ErrorHandler {

    fun handleAction(action: UpdateMailboxPassword, currentState: UserInput): Flow<PasswordManagementState> =
        when (action) {
            is TwoFaComplete -> handleTwoFaResult(action.result, currentState, action.token)
            is SaveMailboxPassword -> {
                val updatedState = currentState.updateMailboxPassword(
                    current = action.currentLoginPassword,
                    new = action.newPassword,
                    confirmNew = action.confirmPassword
                )
                saveMailboxPassword(updatedState, action.token)
            }
        }

    private fun handleTwoFaResult(
        result: Boolean,
        currentState: UserInput,
        token: PasswordValidatorToken?
    ): Flow<PasswordManagementState> = flow {
        if (result) {
            emitAll(submitMailboxPassChange(currentState, token))
        } else {
            emit(currentState.copyWithMailboxPassword { it.copy(loading = false) })
            getFlow().stepBack()
        }
    }

    private fun saveMailboxPassword(
        currentState: UserInput,
        token: PasswordValidatorToken?
    ): Flow<PasswordManagementState> = flow {
        emit(currentState.copyWithMailboxPassword { it.copy(loading = true) })
        if (currentState.mailboxPassword.current.isBlank()) {
            emit(currentState.setMailboxPasswordValidationError(ValidationError.CurrentPasswordEmpty))
            return@flow
        }
        if (token == null) {
            emit(currentState.setMailboxPasswordValidationError(ValidationError.PasswordInvalid))
            return@flow
        }

        val submitResult = submitCurrentPassword(currentState.mailboxPassword.current, currentState) ?: return@flow
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
                    WANT_CHANGE -> emitAll(submitMailboxPassChange(currentState, token))
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
        emit(Awaiting2faForMailbox(userId, currentState, token))
    }

    private fun submitMailboxPassChange(
        currentState: UserInput,
        token: PasswordValidatorToken?
    ): Flow<PasswordManagementState> = flow {
        emit(currentState.copyWithMailboxPassword { it.copy(loading = true) })

        val mailboxPasswordState = currentState.mailboxPassword
        val changeResult = attemptMailboxPasswordChange(mailboxPasswordState, token, currentState) ?: return@flow
        handleMailboxPasswordChangeResult(changeResult, currentState)
    }

    private suspend fun FlowCollector<PasswordManagementState>.attemptMailboxPasswordChange(
        mailboxPasswordState: MailboxPasswordState,
        token: PasswordValidatorToken?,
        currentState: UserInput
    ): PasswordFlowChangeMboxPassResult? {
        return runCatching {
            getFlow().changeMboxPass(
                newMboxPass = mailboxPasswordState.new,
                confirmPassword = mailboxPasswordState.confirmNew,
                token = (token as? PasswordValidatorTokenWrapper)?.toRust()
            )
        }.getOrElse { exception ->
            emit(Error.General(exception.message ?: "Mailbox password change failed", currentState))
            null
        }
    }

    private suspend fun FlowCollector<PasswordManagementState>.handleMailboxPasswordChangeResult(
        changeResult: PasswordFlowChangeMboxPassResult,
        currentState: UserInput
    ) {
        when (changeResult) {
            is PasswordFlowChangeMboxPassResult.Error -> {
                val validationError = changeResult.v1.mapToValidationError()
                if (validationError != null) {
                    emit(currentState.setMailboxPasswordValidationError(validationError))
                } else {
                    emit(Error.General(changeResult.v1.toString(), currentState))
                }
            }

            is PasswordFlowChangeMboxPassResult.Ok -> {
                when (changeResult.v1) {
                    COMPLETE -> emit(MailboxPasswordSaved)
                    else -> emit(Error.InvalidState(currentState))
                }
            }
        }
    }

    override fun handleError(throwable: Throwable, currentState: UserInput): PasswordManagementState =
        Error.General(error = throwable.message ?: "Unknown error occurred", currentState)

    companion object {

        fun create(getFlow: suspend () -> PasswordFlow, getUserId: () -> CoreUserId?): MailboxPasswordHandler =
            MailboxPasswordHandler(getUserId, getFlow)
    }
}

private fun UserInput.copyWithMailboxPassword(transform: (MailboxPasswordState) -> MailboxPasswordState): UserInput =
    copy(mailboxPassword = transform(mailboxPassword))

private fun UserInput.updateMailboxPassword(
    current: String,
    new: String,
    confirmNew: String
): UserInput = copy(
    mailboxPassword = mailboxPassword.copy(
        current = current,
        new = new,
        confirmNew = confirmNew
    )
)

private fun UserInput.setMailboxPasswordValidationError(error: ValidationError): UserInput =
    copyWithMailboxPassword { it.copy(validationError = error, loading = false) }
