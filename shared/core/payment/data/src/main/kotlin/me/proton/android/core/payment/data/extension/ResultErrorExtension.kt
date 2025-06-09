/*
 * Copyright (C) 2025 Proton AG
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

package me.proton.android.core.payment.data.extension

import android.content.Context
import me.proton.android.core.payment.presentation.R
import uniffi.proton_mail_uniffi.ContextReason
import uniffi.proton_mail_uniffi.OtherErrorReason
import uniffi.proton_mail_uniffi.OtherErrorReason.InvalidParameter
import uniffi.proton_mail_uniffi.OtherErrorReason.Other
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.ProtonError.Network
import uniffi.proton_mail_uniffi.ProtonError.OtherReason
import uniffi.proton_mail_uniffi.ProtonError.ServerError
import uniffi.proton_mail_uniffi.ProtonError.Unexpected
import uniffi.proton_mail_uniffi.UnexpectedError
import uniffi.proton_mail_uniffi.UserContextError
import uniffi.uniffi_common.UserApiServiceError
import uniffi.uniffi_common.UserApiServiceError.BadGateway
import uniffi.uniffi_common.UserApiServiceError.Internal
import uniffi.uniffi_common.UserApiServiceError.InternalServerError
import uniffi.uniffi_common.UserApiServiceError.NotFound
import uniffi.uniffi_common.UserApiServiceError.NotImplemented
import uniffi.uniffi_common.UserApiServiceError.OtherHttpError
import uniffi.uniffi_common.UserApiServiceError.OtherNetwork
import uniffi.uniffi_common.UserApiServiceError.ServiceUnavailable
import uniffi.uniffi_common.UserApiServiceError.TooManyRequests
import uniffi.uniffi_common.UserApiServiceError.Unauthorized
import uniffi.uniffi_common.UserApiServiceError.UnprocessableEntity

fun UserContextError.getErrorMessage(context: Context) = when (this) {
    is UserContextError.Other -> this.v1.getErrorMessage(context)
    is UserContextError.Reason -> this.v1.getErrorMessage()
}

fun ContextReason.getErrorMessage() = when (this) {
    ContextReason.UNKNOWN_LABEL -> "UNKNOWN_LABEL"
    ContextReason.DUPLICATE_CONTEXT -> "DUPLICATE_CONTEXT"
    ContextReason.USER_CONTEXT_NOT_INITIALIZED -> "USER_CONTEXT_NOT_INITIALIZED"
}

fun ProtonError.getErrorMessage(context: Context) = when (this) {
    is OtherReason -> v1.getErrorMessage()
    is ServerError -> v1.getErrorMessage()
    is Unexpected -> v1.getErrorMessage()
    is Network -> context.getString(R.string.presentation_general_connection_error)
}

fun UserApiServiceError.getErrorMessage() = when (this) {
    is UserApiServiceError.BadRequest -> v1
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
    throw IllegalStateException("UnexpectedError: $it")
}

fun OtherErrorReason.getErrorMessage() = when (this) {
    is InvalidParameter -> "InvalidParameter"
    is Other -> this.v1
}
