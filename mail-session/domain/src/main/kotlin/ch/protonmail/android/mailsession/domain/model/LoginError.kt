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

import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import uniffi.proton_mail_uniffi.LoginErrorReason
import uniffi.proton_mail_uniffi.LoginError as LocalLoginError

sealed interface LoginError {
    data object InvalidCredentials : LoginError
    data object UnsupportedTwoFactorAuthentication : LoginError
    data object CannotUnlockUserKey : LoginError
    data class Other(val error: DataError) : LoginError
}


fun LocalLoginError.toLoginError(): LoginError {
    return when (this) {
        is LocalLoginError.Reason -> when (this.v1) {
            LoginErrorReason.INVALID_CREDENTIALS -> LoginError.InvalidCredentials
            LoginErrorReason.UNSUPPORTED_TFA -> LoginError.UnsupportedTwoFactorAuthentication
            LoginErrorReason.CANT_UNLOCK_USER_KEY -> LoginError.CannotUnlockUserKey
        }
        is LocalLoginError.Other -> LoginError.Other(this.v1.toDataError())
    }
}
