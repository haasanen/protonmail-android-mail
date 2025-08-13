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

import me.proton.android.core.auth.presentation.signup.ui.Country
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken

sealed interface SignUpOperation

sealed interface SignUpAction : SignUpOperation {
    data object CreatePlan : SignUpAction // add in the future
    data object CreateUser : SignUpAction
    data object FinalizeSignup : SignUpAction
}

sealed interface CreateUsernameAction : SignUpAction {
    data class LoadData(
        val accountType: AccountType
    ) : CreateUsernameAction

    data object CreateExternalAccount : CreateUsernameAction
    data object CreateInternalAccount : CreateUsernameAction
    data class Perform(
        val unused: Long = System.currentTimeMillis(),
        val value: String,
        val domain: String?,
        val accountType: AccountType,
        val usernameFrameDetails: ChallengeFrameDetails
    ) : CreateUsernameAction

    data class CreateUsernameClosed(
        val back: Boolean = false
    ) : CreateUsernameAction
}

sealed interface CreatePasswordAction : SignUpAction {
    data object LoadData : CreatePasswordAction
    data class Perform(
        val unused: Long = System.currentTimeMillis(),
        val password: String,
        val confirmPassword: String,
        val token: PasswordValidatorToken?
    ) : CreatePasswordAction

    data class CreatePasswordClosed(
        val back: Boolean = false
    ) : CreatePasswordAction
}

sealed interface CreateRecoveryAction : SignUpAction {

    data class SelectRecoveryMethod(
        val unused: Long = System.currentTimeMillis(),
        val recoveryMethod: RecoveryMethod,
        val locale: String
    ) : CreateRecoveryAction

    data class SubmitRecoveryEmail(
        val unused: Long = System.currentTimeMillis(),
        val recoveryMethod: RecoveryMethod = RecoveryMethod.Email,
        val email: String,
        val recoveryFrameDetails: ChallengeFrameDetails
    ) : CreateRecoveryAction

    data class SubmitRecoveryPhone(
        val unused: Long = System.currentTimeMillis(),
        val recoveryMethod: RecoveryMethod = RecoveryMethod.Phone,
        val callingCode: String,
        val phoneNumber: String,
        val recoveryFrameDetails: ChallengeFrameDetails
    ) : CreateRecoveryAction

    sealed interface DialogAction : CreateRecoveryAction {
        data class WantSkipRecovery(
            val recoveryFrameDetails: ChallengeFrameDetails
        ) : DialogAction

        data object RecoverySkipped : DialogAction

        data object WantSkipDialogClosed : DialogAction

        data class PickCountry(
            val recoveryMethod: RecoveryMethod = RecoveryMethod.Phone
        ) : DialogAction

        data class CountryPicked(
            val recoveryMethod: RecoveryMethod = RecoveryMethod.Phone,
            val country: Country
        ) : DialogAction

        data object CountryPickerClosed : DialogAction
    }

    data class CreateRecoveryClosed(
        val back: Boolean = false
    ) : CreateRecoveryAction
}
