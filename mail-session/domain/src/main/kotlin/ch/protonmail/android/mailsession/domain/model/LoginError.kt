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

package ch.protonmail.android.mailsession.domain.model

import uniffi.proton_account_uniffi.LoginError as LocalLoginError

sealed interface LoginError {
    data object ApiFailure : LoginError
    data object InternalError : LoginError
    data object AuthenticationFailure : LoginError
    data object Unknown : LoginError
}

fun LocalLoginError.toLoginError(): LoginError {
    return when (this) {
        // 1. API-related failures
        is LocalLoginError.FlowLogin,
        is LocalLoginError.FlowTotp,
        is LocalLoginError.FlowFido,
        is LocalLoginError.UserFetch,
        is LocalLoginError.KeySecretSaltFetch ->
            LoginError.ApiFailure

        // 2. Internal data/state issues
        is LocalLoginError.KeySecretDerivation,
        is LocalLoginError.ServerProof,
        is LocalLoginError.SrpProof,
        is LocalLoginError.AuthStore,
        is LocalLoginError.Other ->
            LoginError.InternalError

        // 3. Login-specific validation failures
        LocalLoginError.InvalidState,
        LocalLoginError.MissingPrimaryKey,
        LocalLoginError.KeySecretDecryption,
        LocalLoginError.WrongMailboxPassword ->
            LoginError.AuthenticationFailure

        // 4. Fallback
        else -> LoginError.Unknown
    }
}
