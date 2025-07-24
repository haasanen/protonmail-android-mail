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

import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken

sealed interface PasswordManagementOperation

sealed interface PasswordManagementAction : PasswordManagementOperation {
    data object Load : PasswordManagementAction
    data object Reset : PasswordManagementAction

    sealed interface UserInputAction : PasswordManagementAction {
        data class SelectTab(val tab: PasswordManagementState.Tab) : UserInputAction

        sealed interface UpdateLoginPassword : UserInputAction {
            data class TwoFaComplete(val result: Boolean, val token: PasswordValidatorToken?) : UpdateLoginPassword
            data class SaveLoginPassword(
                val currentPassword: String,
                val newPassword: String,
                val confirmPassword: String,
                val token: PasswordValidatorToken?
            ) : UpdateLoginPassword
        }

        sealed interface UpdateMailboxPassword : UserInputAction {
            data class TwoFaComplete(val result: Boolean, val token: PasswordValidatorToken?) : UpdateMailboxPassword
            data class SaveMailboxPassword(
                val currentLoginPassword: String,
                val newPassword: String,
                val confirmPassword: String,
                val token: PasswordValidatorToken?
            ) : UpdateMailboxPassword
        }
    }

    data object ErrorShown : PasswordManagementAction
}
