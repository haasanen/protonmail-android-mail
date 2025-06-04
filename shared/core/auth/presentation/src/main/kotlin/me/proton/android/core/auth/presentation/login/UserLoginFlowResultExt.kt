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
import uniffi.proton_account_uniffi.LoginError
import uniffi.proton_mail_uniffi.LoginErrorReason
import uniffi.proton_mail_uniffi.MailLoginError
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
import uniffi.uniffi_common.UserApiServiceError.BadGateway
import uniffi.uniffi_common.UserApiServiceError.BadRequest
import uniffi.uniffi_common.UserApiServiceError.InternalServerError
import uniffi.uniffi_common.UserApiServiceError.NotFound
import uniffi.uniffi_common.UserApiServiceError.NotImplemented
import uniffi.uniffi_common.UserApiServiceError.OtherHttpError
import uniffi.uniffi_common.UserApiServiceError.ServiceUnavailable
import uniffi.uniffi_common.UserApiServiceError.Unauthorized
import uniffi.uniffi_common.UserApiServiceError.UnprocessableEntity
import uniffi.uniffi_common.UserApiServiceError.Internal
import uniffi.uniffi_common.UserApiServiceError.OtherNetwork
import uniffi.uniffi_common.UserApiServiceError.TooManyRequests
import uniffi.uniffi_common.UserApiServiceError

fun MailLoginError.getErrorMessage(context: Context) = when (this) {
    is MailLoginError.Other -> this.v1.getErrorMessage(context)
    is MailLoginError.Reason -> this.v1.getErrorMessage(context)
}

fun LoginError.getErrorMessage(): String = when (this) {
    is LoginError.FlowLogin -> v1.getErrorMessage()
    is LoginError.FlowTotp -> v1.getErrorMessage()
    is LoginError.FlowFido -> v1.getErrorMessage()
    is LoginError.UserFetch -> v1.getErrorMessage()
    is LoginError.KeySecretSaltFetch -> v1.getErrorMessage()

    is LoginError.KeySecretAuthUpdate -> v1
    is LoginError.KeySecretDerivation -> v1
    is LoginError.ServerProof -> v1
    is LoginError.SrpProof -> v1
    is LoginError.AuthStore -> v1
    is LoginError.Other -> v1

    LoginError.InvalidState -> "LoginError.InvalidState"
    LoginError.MissingPrimaryKey -> "LoginError.MissingPrimaryKey"
    LoginError.KeySecretDecryption -> "LoginError.KeySecretDecryption"
    LoginError.UnsupportedTfa -> "LoginError.UnsupportedTfa"
    LoginError.WrongMailboxPassword -> "LoginError.WrongMailboxPassword"
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
    is Unauthorized -> v1
    is UnprocessableEntity -> v1
    is Internal -> v1
    is OtherNetwork -> v1
    is TooManyRequests -> v1
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
