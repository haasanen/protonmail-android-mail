/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.auth.presentation.login

sealed interface LoginViewState {
    data object Idle : LoginViewState
    data object LoggingIn : LoginViewState
    data class Awaiting2fa(val userId: String) : LoginViewState
    data class Awaiting2Pass(val userId: String) : LoginViewState
    data class LoggedIn(val userId: String) : LoginViewState
    sealed interface Error : LoginViewState {
        data object Validation : Error
        data class AlreadyLoggedIn(val userId: String) : Error
    }

    val isLoading: Boolean
        get() = when (this) {
            is LoggingIn -> true
            is Awaiting2fa -> true
            is Awaiting2Pass -> true
            is LoggedIn -> true
            else -> false
        }
}
