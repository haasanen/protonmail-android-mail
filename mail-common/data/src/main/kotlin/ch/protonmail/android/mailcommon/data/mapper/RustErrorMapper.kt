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

package ch.protonmail.android.mailcommon.data.mapper

import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.NetworkError
import uniffi.proton_mail_uniffi.ActionError
import uniffi.proton_mail_uniffi.ActionErrorReason
import uniffi.proton_mail_uniffi.DraftDiscardError
import uniffi.proton_mail_uniffi.DraftDiscardErrorReason
import uniffi.proton_mail_uniffi.EventError
import uniffi.proton_mail_uniffi.EventErrorReason
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.SessionReason
import uniffi.proton_mail_uniffi.UserSessionError

fun UserSessionError.toDataError(): DataError = when (this) {
    is UserSessionError.Other -> this.v1.toDataError()
    is UserSessionError.Reason -> when (this.v1) {
        is SessionReason.DuplicateSession,
        is SessionReason.MethodCalledInWrongOrigin,
        is SessionReason.UnknownLabel,
        is SessionReason.UserSessionNotInitialized -> DataError.Local.NoUserSession
    }
}

fun ActionError.toDataError(): DataError = when (this) {
    is ActionError.Other -> this.v1.toDataError()
    is ActionError.Reason -> when (v1) {
        ActionErrorReason.UNKNOWN_LABEL,
        ActionErrorReason.UNKNOWN_MESSAGE,
        ActionErrorReason.UNKNOWN_CONTENT_ID -> DataError.Local.NoDataCached
    }
}

fun DraftDiscardError.toDataError(): DataError = when (this) {
    is DraftDiscardError.Other -> this.v1.toDataError()
    is DraftDiscardError.Reason -> when (this.v1) {
        DraftDiscardErrorReason.MESSAGE_DOES_NOT_EXIST,
        DraftDiscardErrorReason.DELETE_FAILED -> DataError.Local.DiscardDraftError
    }
}

fun EventError.toDataError(): DataError = when (this) {
    is EventError.Other -> this.v1.toDataError()
    is EventError.Reason -> when (this.v1) {
        EventErrorReason.REFRESH,
        EventErrorReason.SUBSCRIBER -> DataError.Local.Unknown
    }
}

fun ProtonError.toDataError(): DataError = when (this) {
    is ProtonError.Network -> DataError.Remote.Http(NetworkError.NoNetwork)
    is ProtonError.OtherReason -> DataError.Local.Unknown
    is ProtonError.ServerError -> DataError.Remote.Http(NetworkError.ServerError)
    is ProtonError.Unexpected -> DataError.Local.Unknown
    is ProtonError.NonProcessableActions -> DataError.Local.UnsupportedOperation
}
