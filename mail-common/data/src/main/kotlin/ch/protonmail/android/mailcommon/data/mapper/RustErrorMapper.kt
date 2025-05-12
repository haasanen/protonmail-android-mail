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
import uniffi.proton_mail_uniffi.DraftAttachmentUploadError
import uniffi.proton_mail_uniffi.DraftAttachmentUploadErrorReason
import uniffi.proton_mail_uniffi.DraftDiscardError
import uniffi.proton_mail_uniffi.DraftDiscardErrorReason
import uniffi.proton_mail_uniffi.DraftOpenError
import uniffi.proton_mail_uniffi.DraftOpenErrorReason
import uniffi.proton_mail_uniffi.DraftSendError
import uniffi.proton_mail_uniffi.DraftSendErrorReason
import uniffi.proton_mail_uniffi.DraftUndoSendError
import uniffi.proton_mail_uniffi.DraftUndoSendErrorReason
import uniffi.proton_mail_uniffi.EventError
import uniffi.proton_mail_uniffi.EventErrorReason
import uniffi.proton_mail_uniffi.ProtonError
import uniffi.proton_mail_uniffi.SessionErrorReason
import uniffi.proton_mail_uniffi.UserSessionError

fun UserSessionError.toDataError(): DataError = when (this) {
    is UserSessionError.Other -> this.v1.toDataError()
    is UserSessionError.Reason -> when (this.v1) {
        SessionErrorReason.UNKNOWN_LABEL,
        SessionErrorReason.DUPLICATE_CONTEXT,
        SessionErrorReason.USER_CONTEXT_NOT_INITIALIZED -> DataError.Local.Unknown
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

fun DraftSendError.toDataError(): DataError = when (this) {
    is DraftSendError.Other -> this.v1.toDataError()
    is DraftSendError.Reason -> when (this.v1) {
        is DraftSendErrorReason.AlreadySent,
        is DraftSendErrorReason.MessageAlreadySent,
        is DraftSendErrorReason.MessageIsNotADraft -> DataError.Local.SendDraftError.AlreadySent
        is DraftSendErrorReason.AddressDisabled,
        is DraftSendErrorReason.AddressDoesNotHavePrimaryKey -> DataError.Local.SendDraftError.InvalidSenderAddress
        is DraftSendErrorReason.UnknownRecipientValidationError,
        is DraftSendErrorReason.RecipientEmailInvalid,
        is DraftSendErrorReason.ProtonRecipientDoesNotExist,
        is DraftSendErrorReason.NoRecipients -> DataError.Local.SendDraftError.InvalidRecipient
        is DraftSendErrorReason.MissingAttachmentUploads -> DataError.Local.SendDraftError.AttachmentsError
        is DraftSendErrorReason.MessageDoesNotExist,
        is DraftSendErrorReason.ScheduleSendExpired,
        is DraftSendErrorReason.PackageError -> DataError.Local.Unknown
    }
}

fun DraftOpenError.toDataError(): DataError = when (this) {
    is DraftOpenError.Other -> this.v1.toDataError()
    is DraftOpenError.Reason -> when (this.v1) {
        DraftOpenErrorReason.MESSAGE_DOES_NOT_EXIST,
        DraftOpenErrorReason.MESSAGE_IS_NOT_A_DRAFT,
        DraftOpenErrorReason.REPLY_OR_FORWARD_DRAFT,
        DraftOpenErrorReason.ADDRESS_NOT_FOUND,
        DraftOpenErrorReason.MESSAGE_BODY_MISSING -> DataError.Local.OpenDraftError
    }
}

fun DraftDiscardError.toDataError(): DataError = when (this) {
    is DraftDiscardError.Other -> this.v1.toDataError()
    is DraftDiscardError.Reason -> when (this.v1) {
        DraftDiscardErrorReason.MESSAGE_DOES_NOT_EXIST,
        DraftDiscardErrorReason.DELETE_FAILED -> DataError.Local.DiscardDraftError
    }
}

fun DraftUndoSendError.toDataError(): DataError = when (this) {
    is DraftUndoSendError.Other -> this.v1.toDataError()
    is DraftUndoSendError.Reason -> when (this.v1) {
        DraftUndoSendErrorReason.MESSAGE_DOES_NOT_EXIST,
        DraftUndoSendErrorReason.MESSAGE_IS_NOT_A_DRAFT,
        DraftUndoSendErrorReason.MESSAGE_CAN_NOT_BE_UNDO_SENT,
        DraftUndoSendErrorReason.SEND_CAN_NO_LONGER_BE_UNDONE -> DataError.Local.UndoSendError
    }
}

fun DraftAttachmentUploadError.toDataError(): DataError = when (this) {
    is DraftAttachmentUploadError.Other -> this.v1.toDataError()
    is DraftAttachmentUploadError.Reason -> when (this.v1) {
        DraftAttachmentUploadErrorReason.MESSAGE_DOES_NOT_EXIST,
        DraftAttachmentUploadErrorReason.MESSAGE_DOES_NOT_EXIST_ON_SERVER,
        DraftAttachmentUploadErrorReason.MESSAGE_ALREADY_SENT -> DataError.Local.AttachmentError.InvalidDraftMessage
        DraftAttachmentUploadErrorReason.CRYPTO -> DataError.Local.AttachmentError.EncryptionError
        DraftAttachmentUploadErrorReason.ATTACHMENT_TOO_LARGE -> DataError.Local.AttachmentError.AttachmentTooLarge
        DraftAttachmentUploadErrorReason.TOO_MANY_ATTACHMENTS -> DataError.Local.AttachmentError.TooManyAttachments
        DraftAttachmentUploadErrorReason.RETRY_INVALID_STATE -> DataError.Local.Unknown
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
    is ProtonError.SessionExpired -> DataError.Remote.Http(NetworkError.Unauthorized)
    is ProtonError.Unexpected -> DataError.Local.Unknown
}
