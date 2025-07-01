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

package ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects

import androidx.annotation.StringRes
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddErrorWithList
import ch.protonmail.android.mailcomposer.domain.model.AttachmentDeleteError
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState

internal sealed class LoadingError(@StringRes val resId: Int) : EffectsStateModification {

    override fun apply(state: ComposerState.Effects): ComposerState.Effects =
        state.copy(error = Effect.of(TextUiModel(resId)))

    data object DraftContent : LoadingError(R.string.composer_error_loading_draft)
}

internal sealed class UnrecoverableError(@StringRes val resId: Int) : EffectsStateModification {

    override fun apply(state: ComposerState.Effects): ComposerState.Effects =
        state.copy(exitError = Effect.of(TextUiModel(resId)))

    data object InvalidSenderAddress : UnrecoverableError(R.string.composer_error_invalid_sender)
    data object ParentMessageMetadata : UnrecoverableError(R.string.composer_error_loading_parent_message)
}

internal sealed interface RecoverableError : EffectsStateModification {

    sealed class SenderChange(@StringRes val resId: Int) : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = when (this) {
            FreeUser -> state.copy(premiumFeatureMessage = Effect.of(TextUiModel(resId)))
            AddressCanNotSend,
            ChangeSenderFailure,
            RefreshBodyFailure,
            GetAddressesError -> state.copy(
                error = Effect.of(TextUiModel(resId)),
                changeBottomSheetVisibility = Effect.of(false)
            )
        }

        data object FreeUser : SenderChange(R.string.composer_change_sender_paid_feature)
        data object GetAddressesError :
            SenderChange(R.string.composer_error_change_sender_failed_getting_addresses)

        data object AddressCanNotSend : SenderChange(R.string.composer_change_sender_invalid_address)
        data object ChangeSenderFailure : SenderChange(R.string.composer_change_sender_unexpected_failure)
        data object RefreshBodyFailure : SenderChange(R.string.composer_change_sender_error_refreshing_body)
    }

    data class AttachmentsListChangedWithError(
        val attachmentAddErrorWithList: AttachmentAddErrorWithList
    ) : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            when (attachmentAddErrorWithList.error) {
                AttachmentAddError.AttachmentTooLarge,
                AttachmentAddError.TooManyAttachments -> state.copy(
                    attachmentsFileSizeExceeded = Effect.of(
                        attachmentAddErrorWithList.failedAttachments.map {
                            it.attachmentMetadata.attachmentId
                        }
                    )
                )

                AttachmentAddError.EncryptionError,
                AttachmentAddError.Unknown,
                AttachmentAddError.InvalidDraftMessage ->
                    state.copy(error = Effect.of(TextUiModel(R.string.composer_unexpected_attachments_error)))


            }
    }

    data class AttachmentsStore(val error: AttachmentAddError) : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = when (error) {
            AttachmentAddError.AttachmentTooLarge -> state.copy(attachmentsFileSizeExceeded = Effect.of(emptyList()))
            AttachmentAddError.EncryptionError -> state.copy(attachmentsEncryptionFailed = Effect.of(Unit))
            AttachmentAddError.TooManyAttachments ->
                state.copy(error = Effect.of(TextUiModel(R.string.composer_too_many_attachments_error)))

            AttachmentAddError.Unknown,
            AttachmentAddError.InvalidDraftMessage ->
                state.copy(error = Effect.of(TextUiModel(R.string.composer_unexpected_attachments_error)))
        }
    }

    data class AttachmentRemove(val error: AttachmentDeleteError) : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = when (error) {
            AttachmentDeleteError.FailedToDeleteFile,
            AttachmentDeleteError.InvalidDraftMessage,
            AttachmentDeleteError.Unknown ->
                state.copy(error = Effect.of(TextUiModel(R.string.composer_delete_attachment_error)))
        }
    }

    data object Expiration : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_setting_expiration_time)),
            changeBottomSheetVisibility = Effect.of(false)
        )
    }

    data class SendingFailed(val reason: String) : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(sendingErrorEffect = Effect.of(TextUiModel.Text(reason)))
    }

    data object DiscardDraftFailed : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(error = Effect.of(TextUiModel.TextRes(R.string.discard_draft_error)))
    }

    data object SaveBodyFailed : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(error = Effect.of(TextUiModel(R.string.composer_error_store_draft_body)))
    }

    data object SaveRecipientFailed : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(error = Effect.of(TextUiModel(R.string.composer_error_store_draft_recipients)))
    }

    data object SaveSubjectFailed : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(error = Effect.of(TextUiModel(R.string.composer_error_store_draft_subject)))
    }

    data object SendMessageFailed : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(error = Effect.of(TextUiModel(R.string.composer_error_send_message)))
    }

    data object GetScheduleSendOptionsFailed : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(error = Effect.of(TextUiModel(R.string.composer_error_retrieving_schedule_send_options)))
    }

    data object LoadingAttachmentsFailed : RecoverableError {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(error = Effect.of(TextUiModel(R.string.composer_loading_attachments_error)))
    }

}
