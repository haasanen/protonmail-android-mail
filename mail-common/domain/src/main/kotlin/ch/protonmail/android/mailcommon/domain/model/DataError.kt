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

package ch.protonmail.android.mailcommon.domain.model

/**
 * Errors related to Data
 */
sealed interface DataError {

    /**
     * Errors related to Local persistence
     */
    sealed interface Local : DataError {

        data object TypeConversionError : Local

        data object DecryptionError : Local

        data object EncryptionError : Local

        data object NoDataCached : Local

        data object NoUserSession : Local

        data object OutOfMemory : Local

        data object FailedToReadFile : Local

        data object FailedToStoreFile : Local

        data object FailedToDeleteFile : Local

        data object DeletingFailed : Local

        data object DbWriteFailed : Local

        data object UnsupportedOperation : Local

        sealed interface SendDraftError : Local {
            data object AlreadySent : SendDraftError
            data object InvalidSenderAddress : SendDraftError
            data object InvalidRecipient : SendDraftError
            data object AttachmentsError : SendDraftError
        }

        sealed interface AttachmentError : Local {
            data object AttachmentTooLarge : AttachmentError
            data object TooManyAttachments : AttachmentError
            data object InvalidDraftMessage : AttachmentError
            data object EncryptionError : AttachmentError
        }

        data object DiscardDraftError : Local

        data object UndoSendError : Local

        data object NoDraftId : Local

        data object NoAutoDeleteBanner : Local

        /**
         * This object is not meant to be actively used.
         * Its purpose is to notify the logging tool that a case that should be handled
         * is not and to allow dedicated handling to be put in place.
         */
        object Unknown : Local
    }

    /**
     * Error fetching data from Remote source
     */
    sealed interface Remote : DataError {

        /**
         * The API returned a failure response
         */
        data class Http(
            val networkError: NetworkError,
            val apiErrorInfo: String? = null,
            val isRetryable: Boolean = false
        ) : Remote

        /**
         * The API returned a success, but proton code is not OK
         */
        data class Proton(val protonError: ProtonError) : Remote

        /**
         * This object is not meant to be actively used.
         * Its purpose is to notify the logging tool that a case that should be handled
         * is not and to allow dedicated handling to be put in place.
         */
        object Unknown : Remote
    }

    object AddressNotFound : DataError
}

fun DataError.isOfflineError() = this is DataError.Remote.Http && this.networkError is NetworkError.NoNetwork
