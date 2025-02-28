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

package ch.protonmail.android.mailcomposer.domain.model

import ch.protonmail.android.mailcommon.domain.model.DataError

sealed interface SaveSendErrorReason {

    sealed interface ErrorNoMessage : SaveSendErrorReason {
        data object NoRecipients : SaveSendErrorReason
        data object AlreadySent : SaveSendErrorReason
        data object MessageDoesNotExist : SaveSendErrorReason
        data object MessageIsNotADraft : SaveSendErrorReason
        data object MessageAlreadySent : SaveSendErrorReason
    }

    sealed interface ErrorWithMessage : SaveSendErrorReason {
        val details: String

        data class AddressDoesNotHavePrimaryKey(override val details: String) : ErrorWithMessage
        data class RecipientEmailInvalid(override val details: String) : ErrorWithMessage
        data class ProtonRecipientDoesNotExist(override val details: String) : ErrorWithMessage
        data class UnknownRecipientValidationError(override val details: String) : ErrorWithMessage
        data class AddressDisabled(override val details: String) : ErrorWithMessage
        data class PackageError(override val details: String) : ErrorWithMessage
    }

    data class OtherDataError(val dataError: DataError) : SaveSendErrorReason

}
