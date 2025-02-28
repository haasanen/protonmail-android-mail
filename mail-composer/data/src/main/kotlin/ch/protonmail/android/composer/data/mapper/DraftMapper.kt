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
import ch.protonmail.android.mailcommon.datarust.mapper.LocalComposerRecipient
import ch.protonmail.android.mailcommon.datarust.mapper.LocalDraftSendResult
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.SaveSendErrorReason
import ch.protonmail.android.mailcomposer.domain.model.MessageSendingStatus
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
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

fun LocalDraft.toDraftFields() = DraftFields(
    sender = SenderEmail(this.sender),
    subject = Subject(this.subject),
    body = DraftBody(this.body),
    recipientsTo = RecipientsTo(this.recipientsTo.toRecipients()),
    recipientsCc = RecipientsCc(this.recipientsCc.toRecipients()),
    recipientsBcc = RecipientsBcc(this.recipientsBcc.toRecipients()),
    originalHtmlQuote = null
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
    is DraftSendStatus.Success -> this.toMessageSendingStatusForSuccess(status.v1)
    is DraftSendStatus.Failure -> this.toMessageSendingStatusForFailure(status.v1)
}

private fun LocalDraftSendResult.toMessageSendingStatusForSuccess(timeRemainingForUndo: ULong): MessageSendingStatus {
    return if (timeRemainingForUndo > 0u) {
        MessageSendingStatus.MessageSentUndoable(
            messageId = this.messageId.toMessageId(),
            timeRemainingForUndo = timeRemainingForUndo.toLong()
        )
    } else {
        MessageSendingStatus.MessageSentFinal(this.messageId.toMessageId())
    }
}
private fun LocalDraftSendResult.toMessageSendingStatusForFailure(error: DraftSaveSendError): MessageSendingStatus {
    return when (error) {
        is DraftSaveSendError.Reason -> MessageSendingStatus.SendMessageError(
            messageId = this.messageId.toMessageId(),
            reason = error.v1.toSaveSendErrorReason()
        )
        is DraftSaveSendError.Other -> MessageSendingStatus.SendMessageError(
            messageId = this.messageId.toMessageId(),
            reason = SaveSendErrorReason.OtherDataError(error.v1.toDataError())
        )
    }
}

fun DraftSaveSendErrorReason.toSaveSendErrorReason(): SaveSendErrorReason = when (this) {
    DraftSaveSendErrorReason.NoRecipients -> SaveSendErrorReason.ErrorNoMessage.NoRecipients
    DraftSaveSendErrorReason.AlreadySent -> SaveSendErrorReason.ErrorNoMessage.AlreadySent
    DraftSaveSendErrorReason.MessageDoesNotExist -> SaveSendErrorReason.ErrorNoMessage.MessageDoesNotExist
    DraftSaveSendErrorReason.MessageIsNotADraft -> SaveSendErrorReason.ErrorNoMessage.MessageIsNotADraft
    DraftSaveSendErrorReason.MessageAlreadySent -> SaveSendErrorReason.ErrorNoMessage.MessageAlreadySent

    is DraftSaveSendErrorReason.AddressDoesNotHavePrimaryKey ->
        SaveSendErrorReason.ErrorWithMessage.AddressDoesNotHavePrimaryKey(v1)

    is DraftSaveSendErrorReason.RecipientEmailInvalid ->
        SaveSendErrorReason.ErrorWithMessage.RecipientEmailInvalid(v1)

    is DraftSaveSendErrorReason.ProtonRecipientDoesNotExist ->
        SaveSendErrorReason.ErrorWithMessage.ProtonRecipientDoesNotExist(v1)

    is DraftSaveSendErrorReason.UnknownRecipientValidationError ->
        SaveSendErrorReason.ErrorWithMessage.UnknownRecipientValidationError(v1)

    is DraftSaveSendErrorReason.AddressDisabled ->
        SaveSendErrorReason.ErrorWithMessage.AddressDisabled(v1)

    is DraftSaveSendErrorReason.PackageError ->
        SaveSendErrorReason.ErrorWithMessage.PackageError(v1)
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
