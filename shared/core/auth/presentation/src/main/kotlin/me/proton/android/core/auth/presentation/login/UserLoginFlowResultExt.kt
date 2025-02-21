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
import uniffi.proton_mail_uniffi.OtherErrorReason.InvalidParameter
import uniffi.proton_mail_uniffi.OtherErrorReason.Other
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.ProtonError.Network
import uniffi.proton_mail_uniffi.ProtonError.OtherReason
import uniffi.proton_mail_uniffi.ProtonError.ServerError
import uniffi.proton_mail_uniffi.ProtonError.SessionExpired
import uniffi.proton_mail_uniffi.ProtonError.Unexpected
import uniffi.proton_mail_uniffi.UnexpectedError
import uniffi.proton_mail_uniffi.UserApiServiceError
import uniffi.proton_mail_uniffi.UserApiServiceError.BadGateway
import uniffi.proton_mail_uniffi.UserApiServiceError.BadRequest
import uniffi.proton_mail_uniffi.UserApiServiceError.InternalServerError
import uniffi.proton_mail_uniffi.UserApiServiceError.NotFound
import uniffi.proton_mail_uniffi.UserApiServiceError.NotImplemented
import uniffi.proton_mail_uniffi.UserApiServiceError.OtherHttpError
import uniffi.proton_mail_uniffi.UserApiServiceError.ServiceUnavailable
import uniffi.proton_mail_uniffi.UserApiServiceError.TooManyRequest
import uniffi.proton_mail_uniffi.UserApiServiceError.Unauthorized
import uniffi.proton_mail_uniffi.UserApiServiceError.UnprocessableEntity

fun LoginError.getErrorMessage(context: Context) = when (this) {
    is LoginError.Other -> this.v1.getErrorMessage(context)
    is LoginError.Reason -> this.v1.getErrorMessage(context)
}

@Suppress("MaxLineLength")
fun LoginErrorReason.getErrorMessage(context: Context) = when (this) {
    LoginErrorReason.INVALID_CREDENTIALS -> context.getString(R.string.auth_login_error_invalid_action_invalid_credentials)
    LoginErrorReason.UNSUPPORTED_TFA -> context.getString(R.string.auth_login_error_invalid_action_unsupported_tfa)
    LoginErrorReason.CANT_UNLOCK_USER_KEY -> context.getString(R.string.auth_login_error_invalid_action_cannot_unlock_keys)
}

fun UserApiServiceError.getErrorMessage() = when (this) {
    is BadRequest -> v1
    is OtherHttpError -> v2
    is BadGateway -> v1
    is InternalServerError -> v1
    is NotFound -> v1
    is NotImplemented -> v1
    is ServiceUnavailable -> v1
    is TooManyRequest -> v1
    is Unauthorized -> v1
    is UnprocessableEntity -> v1
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
    is InvalidParameter -> context.getString(R.string.auth_login_error_invalid_action_invalid_credentials)
    is Other -> this.v1
}

private fun ProtonError.getErrorMessage(context: Context) = when (this) {
    is OtherReason -> v1.getErrorMessage(context)
    is ServerError -> v1.getErrorMessage()
    is Unexpected -> v1.getErrorMessage()
    is Network -> context.getString(R.string.presentation_general_connection_error)
    is SessionExpired -> context.getString(R.string.presentation_error_general)
}
