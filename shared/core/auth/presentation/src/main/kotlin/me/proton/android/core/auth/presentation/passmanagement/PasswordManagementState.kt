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

package me.proton.android.core.auth.presentation.passmanagement

import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken

sealed interface PasswordManagementState {
    data object Loading : PasswordManagementState
    data object Closed : PasswordManagementState

    data class UserInput(
        val selectedTab: Tab = Tab.LOGIN,
        val loginPassword: LoginPasswordState = LoginPasswordState(),
        val mailboxPassword: MailboxPasswordState = MailboxPasswordState()
    ) : PasswordManagementState

    data class Awaiting2faForLogin(
        val userId: CoreUserId,
        val userInput: UserInput,
        val token: PasswordValidatorToken?
    ) : PasswordManagementState

    data object LoginPasswordSaved : PasswordManagementState

    data class Awaiting2faForMailbox(
        val userId: CoreUserId,
        val userInput: UserInput,
        val token: PasswordValidatorToken?
    ) : PasswordManagementState

    data object MailboxPasswordSaved : PasswordManagementState

    sealed class Error(
        open val error: String?,
        open val userInput: UserInput
    ) : PasswordManagementState {

        data class InvalidUserId(
            override val userInput: UserInput
        ) : Error(null, userInput)

        data class General(
            override val error: String?,
            override val userInput: UserInput
        ) : Error(error, userInput)

        data class InvalidState(
            override val userInput: UserInput
        ) : Error(null, userInput)
    }

    fun userInputOrNull(): UserInput? = when (this) {
        is UserInput -> this
        is Awaiting2faForLogin -> this.userInput
        is Awaiting2faForMailbox -> this.userInput
        is Error -> this.userInput
        else -> null
    }

    enum class Tab { LOGIN, MAILBOX }

}

sealed interface ValidationError {
    data object CurrentPasswordEmpty : ValidationError
    data object PasswordEmpty : ValidationError
    data object PasswordInvalid : ValidationError
    data object ConfirmPasswordMissMatch : ValidationError
    data class Other(val message: String?) : ValidationError
}

data class LoginPasswordState(
    val current: String = "",
    val new: String = "",
    val confirmNew: String = "",
    val loading: Boolean = false,
    val isAvailable: Boolean = true,
    val currentPasswordNeeded: Boolean = true,
    val validationToken: PasswordValidatorToken? = null,
    val validationError: ValidationError? = null
)

data class MailboxPasswordState(
    val current: String = "",
    val new: String = "",
    val confirmNew: String = "",
    val loading: Boolean = false,
    val isAvailable: Boolean = false,
    val validationToken: PasswordValidatorToken? = null,
    val validationError: ValidationError? = null
)
