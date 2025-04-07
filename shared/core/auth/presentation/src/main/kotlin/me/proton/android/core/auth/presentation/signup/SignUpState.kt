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

package me.proton.android.core.auth.presentation.signup

import me.proton.android.core.auth.presentation.signup.SignUpState.DataInput
import me.proton.android.core.auth.presentation.signup.ui.Country
import me.proton.android.core.auth.presentation.signup.ui.Domain
import me.proton.core.account.domain.entity.AccountType

sealed interface SignUpState {
    sealed interface DataInput : SignUpState
    data object SigningUp : SignUpState
    data class SignUpError(val message: String?) : SignUpState
    data object SignUpSuccess : SignUpState
    data class LoginSuccess(val userId: String) : SignUpState
}

// region username
sealed interface CreateUsernameState : DataInput {

    val accountType: AccountType
    val isLoading: Boolean

    data class Idle(
        override val accountType: AccountType,
        override val isLoading: Boolean = false
    ) : CreateUsernameState

    data class Load(
        override val accountType: AccountType,
        override val isLoading: Boolean = true
    ) : CreateUsernameState

    data class LoadingComplete(
        override val accountType: AccountType,
        val domains: List<Domain>? = null,
        override val isLoading: Boolean = false
    ) : CreateUsernameState

    data class Creating(
        override val accountType: AccountType,
        override val isLoading: Boolean = false
    ) : CreateUsernameState

    sealed class ValidationError(
        override val accountType: AccountType,
        override val isLoading: Boolean = false
    ) : CreateUsernameState {

        data object UsernameEmpty : ValidationError(AccountType.Username, false)

        data object InternalUsernameEmpty : ValidationError(AccountType.Internal, false)

        data object EmailEmpty : ValidationError(AccountType.External, false)

        data class Other(
            override val accountType: AccountType,
            val field: ValidationField,
            val message: String?
        ) : ValidationError(accountType, false)
    }

    data class Error(
        val unused: Long = System.currentTimeMillis(),
        override val accountType: AccountType,
        override val isLoading: Boolean = false,
        val message: String?
    ) : CreateUsernameState

    data class Success(
        override val accountType: AccountType,
        override val isLoading: Boolean = false,
        val username: String,
        val domain: String? = null,
        val route: String
    ) : CreateUsernameState

    data class Closed(
        override val accountType: AccountType,
        override val isLoading: Boolean = false
    ) : CreateUsernameState
}

enum class ValidationField {
    USERNAME,
    EMAIL
}
// endregion

// region password
sealed interface CreatePasswordState : DataInput {

    data object Idle : CreatePasswordState
    data object Creating : CreatePasswordState
    sealed interface ValidationError : CreatePasswordState {
        data object PasswordEmpty : ValidationError
        data object ConfirmPasswordMissMatch : ValidationError
        data class Other(
            val message: String?
        ) : ValidationError
    }

    data class Error(
        val unused: Long = System.currentTimeMillis(),
        val message: String?
    ) : CreatePasswordState

    data class Success(val route: String) : CreatePasswordState
    data object Closed : CreatePasswordState
}
// endregion

// region recovery
sealed interface CreateRecoveryState : DataInput {

    data class Idle(
        val recoveryMethod: RecoveryMethod = RecoveryMethod.Email,
        val countries: List<Country>? = null,
        val defaultCountry: Country?
    ) : CreateRecoveryState

    data class Creating(
        val recoveryMethod: RecoveryMethod
    ) : CreateRecoveryState

    sealed class ValidationError(
        open val message: String?
    ) : CreateRecoveryState {

        data class Email(
            override val message: String?
        ) : ValidationError(message)

        data class Phone(
            override val message: String?
        ) : ValidationError(message)
    }

    data class Error(
        val unused: Long = System.currentTimeMillis(),
        val recoveryMethod: RecoveryMethod,
        val message: String?
    ) : CreateRecoveryState

    data class Success(
        val recoveryMethod: RecoveryMethod,
        val value: String,
        val route: String
    ) : CreateRecoveryState

    data class WantCountryPicker(
        val recoveryMethod: RecoveryMethod,
        val countries: List<Country>
    ) : CreateRecoveryState

    data class OnCountryPicked(
        val recoveryMethod: RecoveryMethod,
        val country: Country
    ) : CreateRecoveryState

    data class CountryPickerFailed(
        val recoveryMethod: RecoveryMethod,
        val country: Country?
    ) : CreateRecoveryState

    data class WantSkip(
        val recoveryMethod: RecoveryMethod
    ) : CreateRecoveryState

    data class SkipSuccess(
        val recoveryMethod: RecoveryMethod,
        val route: String
    ) : CreateRecoveryState

    data class SkipFailed(
        val recoveryMethod: RecoveryMethod
    ) : CreateRecoveryState

    data object Closed : CreateRecoveryState
}

enum class RecoveryMethod(val value: Int) {
    Email(0), Phone(1);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int) = map[value] ?: Email
    }
}
// endregion
