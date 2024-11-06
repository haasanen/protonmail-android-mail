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

sealed class CreateUsernameState(
    open val accountType: AccountType
) {

    data class Idle(
        override val accountType: AccountType,
        val domains: List<Domain>? = null
    ) : CreateUsernameState(accountType)

    data class Loading(
        override val accountType: AccountType
    ) : CreateUsernameState(accountType)

    data class Validating(
        override val accountType: AccountType
    ) : CreateUsernameState(accountType)

    data class Error(
        override val accountType: AccountType,
        val message: String?
    ) : CreateUsernameState(accountType)

    data class FormError(
        override val accountType: AccountType,
        val message: String?
    ) : CreateUsernameState(accountType)

    data class Success(
        override val accountType: AccountType,
        val username: String
    ) : CreateUsernameState(accountType)

    val isLoading: Boolean
        get() = when (this) {
            is Validating,
            is Loading -> true
            else -> false
        }
}

enum class AccountType {
    Internal, External
}
