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

import ch.protonmail.android.mailcommon.data.mapper.LocalMailLoginError
import ch.protonmail.android.mailcommon.data.mapper.LocalMailLoginErrorOther
import ch.protonmail.android.mailcommon.data.mapper.LocalMailLoginErrorReason
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import uniffi.proton_mail_uniffi.LoginErrorReason

sealed interface MailLoginError {
    data object InvalidCredentials : MailLoginError
    data object UnsupportedTwoFactorAuthentication : MailLoginError
    data object CannotUnlockUserKey : MailLoginError
    data class Other(val error: DataError) : MailLoginError
}


fun LocalMailLoginError.toLoginError(): MailLoginError {
    return when (this) {
        is LocalMailLoginErrorReason -> when (this.v1) {
            LoginErrorReason.INVALID_CREDENTIALS -> MailLoginError.InvalidCredentials
            LoginErrorReason.UNSUPPORTED_TFA -> MailLoginError.UnsupportedTwoFactorAuthentication
            LoginErrorReason.CANT_UNLOCK_USER_KEY -> MailLoginError.CannotUnlockUserKey
        }
        is LocalMailLoginErrorOther -> MailLoginError.Other(this.v1.toDataError())
    }
}
