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

sealed class CreateRecoveryState(
    open val recoveryMethod: RecoveryMethod,
    open val countries: List<Country>? = null
) {

    data class Idle(
        override val recoveryMethod: RecoveryMethod,
        override val countries: List<Country>? = null
    ) : CreateRecoveryState(recoveryMethod, countries)

    data class Loading(
        override val recoveryMethod: RecoveryMethod,
        override val countries: List<Country>? = null,
    ) : CreateRecoveryState(recoveryMethod, countries)

    data class Validating(
        override val recoveryMethod: RecoveryMethod,
        override val countries: List<Country>
    ) : CreateRecoveryState(recoveryMethod)

    sealed class FormError(
        override val recoveryMethod: RecoveryMethod,
        open val message: String?
    ) : CreateRecoveryState(recoveryMethod) {

        data class Email(
            override val recoveryMethod: RecoveryMethod,
            override val message: String?
        ) : FormError(recoveryMethod, message)

        data class Phone(
            override val recoveryMethod: RecoveryMethod,
            override val message: String?
        ) : FormError(recoveryMethod, message)
    }

    data class Success(
        override val recoveryMethod: RecoveryMethod,
        val value: String
    ) : CreateRecoveryState(recoveryMethod)

    val isLoading: Boolean
        get() = when (this) {
            is Validating -> true
            else -> false
        }
}

enum class RecoveryMethod(val value: Int) {
    Email(0), Phone(1);

    companion object {
        val map = entries.associateBy { it.value }
        fun enumOf(value: Int) = map[value] ?: Email
    }
}
