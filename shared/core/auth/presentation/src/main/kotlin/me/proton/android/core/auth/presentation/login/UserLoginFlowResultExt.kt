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
import uniffi.proton_mail_uniffi.LoginError
import uniffi.proton_mail_uniffi.LoginErrorReason
import uniffi.proton_mail_uniffi.OtherErrorReason
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.UnexpectedError
import uniffi.proton_mail_uniffi.UserApiServiceError

fun LoginError.getErrorMessage(context: Context) = when (this) {
    is LoginError.Other -> this.v1.getErrorMessage(context)
    is LoginError.Reason -> this.v1.getErrorMessage(context)
}

@Suppress("MaxLineLength")
fun LoginErrorReason.getErrorMessage(context: Context) = when (this) {
    is LoginErrorReason.CantUnlockUserKey -> context.getString(R.string.auth_login_error_invalid_action_cannot_unlock_keys)
    is LoginErrorReason.HumanVerificationChallenge -> context.getString(R.string.auth_login_error_invalid_action_human_verification_challenge)
    is LoginErrorReason.InvalidCredentials -> context.getString(R.string.auth_login_error_invalid_action_invalid_credentials)
    is LoginErrorReason.UnsupportedTfa -> context.getString(R.string.auth_login_error_invalid_action_unsupported_tfa)
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
    UnexpectedError.API -> "API"
    UnexpectedError.DRAFT -> "DRAFT"
    UnexpectedError.ERROR_MAPPING -> "ERROR_MAPPING"
    UnexpectedError.CONFIG -> "CONFIG"
}.also {
    val error = IllegalStateException("UnexpectedError: $it")
    CoreLogger.e(LogTag.LOGIN, error)
}

private fun OtherErrorReason.getErrorMessage(context: Context) = when (this) {
    OtherErrorReason.InvalidParameter -> context.getString(R.string.auth_login_error_invalid_action_invalid_credentials)
    is OtherErrorReason.Other -> this.v1
}

private fun ProtonError.getErrorMessage(context: Context) = when (this) {
    is ProtonError.OtherReason -> v1.getErrorMessage(context)
    is ProtonError.ServerError -> v1.getErrorMessage(context)
    is ProtonError.Unexpected -> v1.getErrorMessage()
    ProtonError.Network -> context.getString(R.string.presentation_general_connection_error)
    ProtonError.SessionExpired -> context.getString(R.string.presentation_error_general)
}
