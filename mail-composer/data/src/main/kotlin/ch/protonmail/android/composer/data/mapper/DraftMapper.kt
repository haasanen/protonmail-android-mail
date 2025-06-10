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

package ch.protonmail.android.composer.data.mapper

import ch.protonmail.android.composer.data.local.LocalDraft
import ch.protonmail.android.composer.data.local.LocalDraftWithSyncStatus
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.composer.data.wrapper.DraftWrapperWithSyncStatus
import ch.protonmail.android.mailcommon.data.mapper.LocalComposerRecipient
import ch.protonmail.android.mailcommon.data.mapper.LocalDraftSendResult
import ch.protonmail.android.mailcommon.data.mapper.LocalEmbeddedImageInfo
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.DraftFieldsWithSyncStatus
import ch.protonmail.android.mailcomposer.domain.model.MessageSendingStatus
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SaveDraftError
import ch.protonmail.android.mailcomposer.domain.model.ScheduleSendOptions
import ch.protonmail.android.mailcomposer.domain.model.SendErrorReason
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMessageId
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.Recipient
import timber.log.Timber
import uniffi.proton_mail_uniffi.ComposerRecipient
import uniffi.proton_mail_uniffi.DraftAttachmentUploadErrorReason
import uniffi.proton_mail_uniffi.DraftCreateMode
import uniffi.proton_mail_uniffi.DraftSaveError
import uniffi.proton_mail_uniffi.DraftSaveErrorReason
import uniffi.proton_mail_uniffi.DraftScheduleSendOptions
import uniffi.proton_mail_uniffi.DraftSendErrorReason
import uniffi.proton_mail_uniffi.DraftSendFailure
import uniffi.proton_mail_uniffi.DraftSendStatus
import uniffi.proton_mail_uniffi.DraftSyncStatus
import uniffi.proton_mail_uniffi.SingleRecipientEntry
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

fun DraftScheduleSendOptions.toScheduleSendOptions() = ScheduleSendOptions(
    tomorrowTime = Instant.fromEpochSeconds(this.tomorrowTime.toLong()),
    mondayTime = Instant.fromEpochSeconds(this.mondayTime.toLong()),
    isCustomTimeOptionAvailable = this.isCustomOptionAvailable
)

fun LocalDraftWithSyncStatus.toDraftFieldsWithSyncStatus() = when (this) {
    is LocalDraftWithSyncStatus.Local -> DraftFieldsWithSyncStatus.Local(this.localDraft.toDraftFields())
    is LocalDraftWithSyncStatus.Remote -> DraftFieldsWithSyncStatus.Remote(this.localDraft.toDraftFields())
}

fun LocalDraft.toDraftFields() = DraftFields(
    sender = SenderEmail(this.sender),
    subject = Subject(this.subject),
    body = DraftBody(this.body),
    recipientsTo = RecipientsTo(this.recipientsTo.toRecipients()),
    recipientsCc = RecipientsCc(this.recipientsCc.toRecipients()),
    recipientsBcc = RecipientsBcc(this.recipientsBcc.toRecipients())
)

fun DraftWrapperWithSyncStatus.toLocalDraftWithSyncStatus() = when (this.syncStatus) {
    DraftSyncStatus.CACHED -> LocalDraftWithSyncStatus.Local(this.draftWrapper.toLocalDraft())
    DraftSyncStatus.SYNCED -> LocalDraftWithSyncStatus.Remote(this.draftWrapper.toLocalDraft())
}

fun DraftWrapper.toLocalDraft() = LocalDraft(
    subject = this.subject(),
    sender = this.sender(),
    body = this.body(),
    recipientsTo = this.recipientsTo().recipients().toComposerRecipients(),
    recipientsCc = this.recipientsCc().recipients().toComposerRecipients(),
    recipientsBcc = this.recipientsBcc().recipients().toComposerRecipients()
)

fun DraftAction.toDraftCreateMode(): DraftCreateMode? = when (this) {
    is DraftAction.Forward -> DraftCreateMode.Forward(this.parentId.toLocalMessageId())
    is DraftAction.Reply -> DraftCreateMode.Reply(this.parentId.toLocalMessageId())
    is DraftAction.ReplyAll -> DraftCreateMode.ReplyAll(this.parentId.toLocalMessageId())
    DraftAction.Compose -> DraftCreateMode.Empty
    is DraftAction.ComposeToAddresses,
    is DraftAction.PrefillForShare -> {
        Timber.e("rust-draft: mapping draft action $this failed! Unsupported by rust DraftCreateMode type")
        null
    }
}

fun List<LocalComposerRecipient>.toSingleRecipients(): List<Recipient> = this
    .filterIsInstance<ComposerRecipient.Single>()
    .map {
        val localRecipient = it.v1
        Recipient(
            address = localRecipient.address,
            name = localRecipient.displayName ?: localRecipient.address,
            isProton = false
        )
    }

private fun List<LocalComposerRecipient>.toComposerRecipients(): List<String> = this.map { localRecipient ->
    when (localRecipient) {
        is ComposerRecipient.Group -> localRecipient.v1.displayName
        is ComposerRecipient.Single -> localRecipient.v1.address
    }
}

fun Recipient.toSingleRecipientEntry() = SingleRecipientEntry(
    this.name,
    this.address
)

fun LocalDraftSendResult.toMessageSendingStatus(): MessageSendingStatus = when (val status = this.error) {
    is DraftSendStatus.Success -> this.toMessageSendingStatusForSuccess(status.secondsUntilCancel.toLong())
    is DraftSendStatus.Failure -> this.toMessageSendingStatusForFailure(status.v1)
}

private fun LocalDraftSendResult.toMessageSendingStatusForSuccess(timeRemainingForUndo: Long): MessageSendingStatus {
    return if (timeRemainingForUndo > 0) {
        MessageSendingStatus.MessageSentUndoable(
            messageId = this.messageId.toMessageId(),
            timeRemainingForUndo = timeRemainingForUndo.toDuration(DurationUnit.SECONDS)
        )
    } else {
        MessageSendingStatus.MessageSentFinal(this.messageId.toMessageId())
    }
}

private fun LocalDraftSendResult.toMessageSendingStatusForFailure(error: DraftSendFailure): MessageSendingStatus {
    return when (error) {
        is DraftSendFailure.AttachmentUpload -> MessageSendingStatus.SendMessageError(
            messageId = this.messageId.toMessageId(),
            reason = error.v1.toSendErrorReason()
        )
        is DraftSendFailure.Other -> MessageSendingStatus.SendMessageError(
            messageId = this.messageId.toMessageId(),
            reason = SendErrorReason.OtherDataError(error.v1.toDataError())
        )
        is DraftSendFailure.Save -> MessageSendingStatus.SendMessageError(
            messageId = this.messageId.toMessageId(),
            reason = error.v1.toSendErrorReason()
        )
        is DraftSendFailure.Send -> MessageSendingStatus.SendMessageError(
            messageId = this.messageId.toMessageId(),
            reason = error.v1.toSendErrorReason()
        )
    }
}

fun DraftSaveErrorReason.toSendErrorReason(): SendErrorReason = when (this) {
    is DraftSaveErrorReason.MessageAlreadySent,
    is DraftSaveErrorReason.MessageIsNotADraft -> SendErrorReason.ErrorNoMessage.AlreadySent
    is DraftSaveErrorReason.AddressDisabled ->
        SendErrorReason.ErrorWithMessage.AddressDisabled(v1)

    is DraftSaveErrorReason.AddressDoesNotHavePrimaryKey ->
        SendErrorReason.ErrorWithMessage.AddressDoesNotHavePrimaryKey(v1)

    is DraftSaveErrorReason.RecipientEmailInvalid -> SendErrorReason.ErrorWithMessage.RecipientEmailInvalid(v1)

    is DraftSaveErrorReason.ProtonRecipientDoesNotExist ->
        SendErrorReason.ErrorWithMessage.ProtonRecipientDoesNotExist(v1)

    is DraftSaveErrorReason.MessageDoesNotExist -> SendErrorReason.ErrorNoMessage.MessageDoesNotExist
}

fun DraftAttachmentUploadErrorReason.toSendErrorReason(): SendErrorReason = when (this) {
    DraftAttachmentUploadErrorReason.MESSAGE_DOES_NOT_EXIST,
    DraftAttachmentUploadErrorReason.MESSAGE_DOES_NOT_EXIST_ON_SERVER,
    DraftAttachmentUploadErrorReason.MESSAGE_ALREADY_SENT -> SendErrorReason.ErrorNoMessage.AlreadySent
    DraftAttachmentUploadErrorReason.CRYPTO -> SendErrorReason.ErrorNoMessage.AttachmentCryptoFailure
    DraftAttachmentUploadErrorReason.ATTACHMENT_TOO_LARGE -> SendErrorReason.ErrorNoMessage.AttachmentTooLarge
    DraftAttachmentUploadErrorReason.TOO_MANY_ATTACHMENTS -> SendErrorReason.ErrorNoMessage.TooManyAttachments
    DraftAttachmentUploadErrorReason.RETRY_INVALID_STATE ->
        SendErrorReason.ErrorNoMessage.AttachmentUploadFailureRetriable
}

fun DraftSendErrorReason.toSendErrorReason(): SendErrorReason = when (this) {
    DraftSendErrorReason.NoRecipients -> SendErrorReason.ErrorNoMessage.NoRecipients
    DraftSendErrorReason.AlreadySent -> SendErrorReason.ErrorNoMessage.AlreadySent
    DraftSendErrorReason.MessageDoesNotExist -> SendErrorReason.ErrorNoMessage.MessageDoesNotExist
    DraftSendErrorReason.MessageIsNotADraft -> SendErrorReason.ErrorNoMessage.MessageIsNotADraft
    DraftSendErrorReason.MessageAlreadySent -> SendErrorReason.ErrorNoMessage.MessageAlreadySent
    DraftSendErrorReason.MissingAttachmentUploads -> SendErrorReason.ErrorNoMessage.MissingAttachmentUploads
    DraftSendErrorReason.ScheduleSendMessageLimitExceeded -> SendErrorReason.ErrorNoMessage.ScheduledSendMessagesLimit
    DraftSendErrorReason.ScheduleSendExpired -> SendErrorReason.ErrorNoMessage.ScheduledSendExpired

    is DraftSendErrorReason.AddressDoesNotHavePrimaryKey ->
        SendErrorReason.ErrorWithMessage.AddressDoesNotHavePrimaryKey(v1)

    is DraftSendErrorReason.RecipientEmailInvalid ->
        SendErrorReason.ErrorWithMessage.RecipientEmailInvalid(v1)

    is DraftSendErrorReason.ProtonRecipientDoesNotExist ->
        SendErrorReason.ErrorWithMessage.ProtonRecipientDoesNotExist(v1)

    is DraftSendErrorReason.AddressDisabled ->
        SendErrorReason.ErrorWithMessage.AddressDisabled(v1)

    is DraftSendErrorReason.PackageError ->
        SendErrorReason.ErrorWithMessage.PackageError(v1)
}

fun DraftSaveError.toSaveDraftError(): SaveDraftError = when (this) {
    is DraftSaveError.Other -> SaveDraftError.Other(this.v1.toDataError())
    is DraftSaveError.Reason -> when (val reason = this.v1) {
        is DraftSaveErrorReason.MessageAlreadySent,
        is DraftSaveErrorReason.MessageDoesNotExist,
        is DraftSaveErrorReason.MessageIsNotADraft -> SaveDraftError.MessageIsNotADraft
        is DraftSaveErrorReason.AddressDisabled -> SaveDraftError.AddressDisabled(reason.v1)
        is DraftSaveErrorReason.AddressDoesNotHavePrimaryKey -> SaveDraftError.AddressDoesNotHavePrimaryKey(reason.v1)
        is DraftSaveErrorReason.RecipientEmailInvalid -> SaveDraftError.InvalidRecipient(reason.v1)
        is DraftSaveErrorReason.ProtonRecipientDoesNotExist -> SaveDraftError.InvalidRecipient(reason.v1)
    }
}

fun LocalEmbeddedImageInfo.toEmbeddedImage() = EmbeddedImage(this.data, this.mime)

@MissingRustApi
// Hardcoded values in the mapping
private fun List<String>.toRecipients(): List<Recipient> = this.map {
    Recipient(
        address = it,
        name = "",
        isProton = false
    )
}
