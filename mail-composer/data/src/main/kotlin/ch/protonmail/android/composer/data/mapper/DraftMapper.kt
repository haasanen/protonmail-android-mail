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
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.mailcommon.data.mapper.LocalComposerRecipient
import ch.protonmail.android.mailcommon.data.mapper.LocalDraftSendResult
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.MessageSendingStatus
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SendErrorReason
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMessageId
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.Recipient
import timber.log.Timber
import uniffi.proton_mail_uniffi.ComposerRecipient
import uniffi.proton_mail_uniffi.DraftCreateMode
import uniffi.proton_mail_uniffi.DraftSaveSendError
import uniffi.proton_mail_uniffi.DraftSaveSendErrorReason
import uniffi.proton_mail_uniffi.DraftSendStatus
import uniffi.proton_mail_uniffi.SingleRecipientEntry
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun LocalDraft.toDraftFields() = DraftFields(
    sender = SenderEmail(this.sender),
    subject = Subject(this.subject),
    body = DraftBody(this.body),
    recipientsTo = RecipientsTo(this.recipientsTo.toRecipients()),
    recipientsCc = RecipientsCc(this.recipientsCc.toRecipients()),
    recipientsBcc = RecipientsBcc(this.recipientsBcc.toRecipients())
)

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
    is DraftSendStatus.Success -> this.toMessageSendingStatusForSuccess(status.v1.toLong())
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

private fun LocalDraftSendResult.toMessageSendingStatusForFailure(error: DraftSaveSendError): MessageSendingStatus {
    return when (error) {
        is DraftSaveSendError.Reason -> {
            // Check if the error reason indicates that the message was already sent.
            if (error.v1.isAlreadySent()) {
                // Treat it as a successful send, with no undo time.
                MessageSendingStatus.MessageSentFinal(this.messageId.toMessageId())
            } else {
                MessageSendingStatus.SendMessageError(
                    messageId = this.messageId.toMessageId(),
                    reason = error.v1.toSendErrorReason()
                )
            }
        }
        is DraftSaveSendError.Other -> MessageSendingStatus.SendMessageError(
            messageId = this.messageId.toMessageId(),
            reason = SendErrorReason.OtherDataError(error.v1.toDataError())
        )
    }
}

fun DraftSaveSendErrorReason.isAlreadySent(): Boolean = when (this) {
    DraftSaveSendErrorReason.AlreadySent,
    DraftSaveSendErrorReason.MessageAlreadySent -> true
    else -> false
}

fun DraftSaveSendErrorReason.toSendErrorReason(): SendErrorReason = when (this) {
    DraftSaveSendErrorReason.NoRecipients -> SendErrorReason.ErrorNoMessage.NoRecipients
    DraftSaveSendErrorReason.AlreadySent -> SendErrorReason.ErrorNoMessage.AlreadySent
    DraftSaveSendErrorReason.MessageDoesNotExist -> SendErrorReason.ErrorNoMessage.MessageDoesNotExist
    DraftSaveSendErrorReason.MessageIsNotADraft -> SendErrorReason.ErrorNoMessage.MessageIsNotADraft
    DraftSaveSendErrorReason.MessageAlreadySent -> SendErrorReason.ErrorNoMessage.MessageAlreadySent
    DraftSaveSendErrorReason.MissingAttachmentUploads -> SendErrorReason.ErrorNoMessage.MissingAttachmentUploads
    DraftSaveSendErrorReason.AttachmentUpload -> SendErrorReason.ErrorNoMessage.AttachmentUpload

    is DraftSaveSendErrorReason.AddressDoesNotHavePrimaryKey ->
        SendErrorReason.ErrorWithMessage.AddressDoesNotHavePrimaryKey(v1)

    is DraftSaveSendErrorReason.RecipientEmailInvalid ->
        SendErrorReason.ErrorWithMessage.RecipientEmailInvalid(v1)

    is DraftSaveSendErrorReason.ProtonRecipientDoesNotExist ->
        SendErrorReason.ErrorWithMessage.ProtonRecipientDoesNotExist(v1)

    is DraftSaveSendErrorReason.UnknownRecipientValidationError ->
        SendErrorReason.ErrorWithMessage.UnknownRecipientValidationError(v1)

    is DraftSaveSendErrorReason.AddressDisabled ->
        SendErrorReason.ErrorWithMessage.AddressDisabled(v1)

    is DraftSaveSendErrorReason.PackageError ->
        SendErrorReason.ErrorWithMessage.PackageError(v1)
}

@MissingRustApi
// Hardcoded values in the mapping
private fun List<String>.toRecipients(): List<Recipient> = this.map {
    Recipient(
        address = it,
        name = "",
        isProton = false
    )
}
