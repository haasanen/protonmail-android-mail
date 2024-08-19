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

import android.content.Context
import me.proton.android.core.auth.presentation.LogTag
import me.proton.android.core.auth.presentation.R
import me.proton.core.util.kotlin.CoreLogger
import uniffi.proton_mail_uniffi.LoginReason
import uniffi.proton_mail_uniffi.UnexpectedError
import uniffi.proton_mail_uniffi.UserApiServiceError
import uniffi.proton_mail_uniffi.UserLoginFlowError

fun UserLoginFlowError.getErrorMessage(context: Context) = when (this) {
    is UserLoginFlowError.InvalidAction -> v1.getErrorMessage(context)
    is UserLoginFlowError.Network -> context.getString(R.string.presentation_general_connection_error)
    is UserLoginFlowError.ServerError -> v1.getErrorMessage(context)
    is UserLoginFlowError.Unexpected -> v1.getErrorMessage()
}

fun LoginReason.getErrorMessage(context: Context) = when (this) {
    is LoginReason.CantUnlockUserKey -> context.getString(R.string.auth_login_error_invalid_action_cannot_unlock_keys)
    is LoginReason.HumanVerificationChallenge -> context.getString(R.string.auth_login_error_invalid_action_human_verification_challenge)
    is LoginReason.InvalidCredentials -> context.getString(R.string.auth_login_error_invalid_action_invalid_credentials)
    is LoginReason.UnsupportedTfa -> context.getString(R.string.auth_login_error_invalid_action_unsupported_tfa)
}

fun UserApiServiceError.getErrorMessage(context: Context) = when (this) {
    is UserApiServiceError.BadRequest -> v1
    is UserApiServiceError.OtherHttpError -> v2
    is UserApiServiceError.BadGateway,
    is UserApiServiceError.InternalServerError,
    is UserApiServiceError.NotFound,
    is UserApiServiceError.NotImplemented,
    is UserApiServiceError.ServiceUnavailable,
    is UserApiServiceError.TooManyRequest,
    is UserApiServiceError.Unauthorized,
    is UserApiServiceError.UnprocessableEntity -> context.getString(R.string.presentation_general_connection_error)
}

fun UnexpectedError.getErrorMessage() = when (this) {
    UnexpectedError.CRYPTO -> "CRYPTO"
    UnexpectedError.DATABASE -> "DATABASE"
    UnexpectedError.FILE_SYSTEM -> "FILE_SYSTEM"
    UnexpectedError.INTERNAL -> "INTERNAL"
    UnexpectedError.INVALID_ARGUMENT -> "INVALID_ARGUMENT"
    UnexpectedError.MEMORY -> "MEMORY"
    UnexpectedError.NETWORK -> "NETWORK"
    UnexpectedError.OS -> "OS"
    UnexpectedError.QUEUE -> "QUEUE"
    UnexpectedError.UNKNOWN -> "UNKNOWN"
}.also {
    val error = IllegalStateException("UnexpectedError: $it")
    CoreLogger.e(LogTag.LOGIN, error)
}
