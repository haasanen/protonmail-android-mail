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

package ch.protonmail.android.mailcomposer.presentation.reducer

import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposerAction
import ch.protonmail.android.mailcomposer.presentation.model.ComposerDraftState
import ch.protonmail.android.mailcomposer.presentation.model.ComposerEvent
import ch.protonmail.android.mailcomposer.presentation.model.ComposerOperation
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel
import ch.protonmail.android.mailcomposer.presentation.model.FocusedFieldType
import ch.protonmail.android.mailcomposer.presentation.model.SenderUiModel
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.NO_ATTACHMENT_LIMIT
import javax.inject.Inject
import kotlin.time.Duration

@Suppress("TooManyFunctions")
class ComposerReducer @Inject constructor(
    private val attachmentUiModelMapper: AttachmentMetadataUiModelMapper
) {

    fun newStateFrom(currentState: ComposerDraftState, operation: ComposerOperation): ComposerDraftState =
        when (operation) {
            is ComposerAction -> operation.newStateForAction(currentState)
            is ComposerEvent -> operation.newStateForEvent(currentState)
        }

    @Suppress("ComplexMethod")
    private fun ComposerAction.newStateForAction(currentState: ComposerDraftState) = when (this) {
        is ComposerAction.DraftBodyChanged,
        is ComposerAction.AttachmentsAdded,
        is ComposerAction.RemoveAttachment -> currentState

        is ComposerAction.SenderChanged -> updateSenderTo(currentState, this.sender)
        is ComposerAction.OnAddAttachments -> updateForOnAddAttachments(currentState)
        is ComposerAction.OnCloseComposer -> updateCloseComposerState(currentState)
        is ComposerAction.ChangeSenderRequested -> currentState
        is ComposerAction.OnSendMessage -> updateStateForSendMessage(currentState)
        is ComposerAction.ConfirmSendingWithoutSubject -> updateForConfirmSendWithoutSubject(currentState)
        is ComposerAction.RejectSendingWithoutSubject -> updateForRejectSendWithoutSubject(currentState)
        is ComposerAction.OnSetExpirationTimeRequested -> updateStateForSetExpirationTimeRequested(currentState)
        is ComposerAction.ExpirationTimeSet -> updateStateForExpirationTimeSet(currentState)
        is ComposerAction.SendExpiringMessageToExternalRecipientsConfirmed -> currentState
        is ComposerAction.DiscardDraft -> updateStateForDiscardDraft(currentState)
        is ComposerAction.DiscardDraftConfirmed -> updateStateForDiscardDraftConfirmed(currentState)
    }

    @Suppress("ComplexMethod", "LongMethod")
    private fun ComposerEvent.newStateForEvent(currentState: ComposerDraftState) = when (this) {

        is ComposerEvent.DefaultSenderReceived -> updateSenderTo(currentState, this.sender)
        is ComposerEvent.ErrorLoadingDefaultSenderAddress -> updateStateToSenderError(currentState)
        is ComposerEvent.ErrorVerifyingPermissionsToChangeSender -> currentState.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_change_sender_failed_getting_subscription))
        )

        is ComposerEvent.ErrorFreeUserCannotChangeSender -> updateStateToPaidFeatureMessage(currentState)

        is ComposerEvent.ErrorStoringDraftBody -> currentState.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_store_draft_body))
        )

        is ComposerEvent.ErrorStoringDraftSubject -> currentState.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_store_draft_subject))
        )

        is ComposerEvent.ErrorStoringDraftRecipients -> currentState.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_store_draft_recipients))
        )

        is ComposerEvent.SenderAddressesReceived -> currentState.copy(
            senderAddresses = this.senders,
            changeBottomSheetVisibility = Effect.of(true)
        )

        is ComposerEvent.OnCloseWithDraftSaved -> updateCloseComposerStateWithDraftSaved(currentState, this.messageId)
        is ComposerEvent.OpenDraft -> currentState.copy(isLoading = true)
        is ComposerEvent.PrefillDraftDataReceived -> updateComposerFieldsState(
            currentState,
            this.draftUiModel,
            this.isDataRefreshed,
            this.isBlockedSendingFromPmAddress,
            this.isBlockedSendingFromDisabledAddress,
            this.bodyShouldTakeFocus
        )

        is ComposerEvent.PrefillDataReceivedViaShare -> updateComposerFieldsState(
            currentState,
            this.draftUiModel,
            isDataRefreshed = true,
            blockedSendingFromPmAddress = false,
            blockedSendingFromDisabledAddress = false
        )

        is ComposerEvent.ReplaceDraftBody -> {
            updateReplaceDraftBodyEffect(currentState, this.draftBody)
        }

        is ComposerEvent.ErrorLoadingDraftData -> currentState.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_loading_draft)),
            isLoading = false
        )

        is ComposerEvent.OnSendMessageOffline -> updateStateForSendMessageOffline(currentState)
        is ComposerEvent.OnAttachmentsUpdated -> updateAttachmentsState(currentState, this.attachments)
        is ComposerEvent.ErrorLoadingParentMessageData -> currentState.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_loading_parent_message)),
            isLoading = false
        )

        is ComposerEvent.OnSendingError -> updateSendingErrorState(currentState, sendingError)
        is ComposerEvent.OnMessagePasswordUpdated -> updateStateForMessagePassword(currentState, this.messagePassword)
        is ComposerEvent.ConfirmEmptySubject -> currentState.copy(
            confirmSendingWithoutSubject = Effect.of(Unit)
        )

        is ComposerEvent.ErrorSettingExpirationTime -> currentState.copy(
            error = Effect.of(TextUiModel(R.string.composer_error_setting_expiration_time))
        )

        is ComposerEvent.OnMessageExpirationTimeUpdated -> updateStateForMessageExpirationTime(
            currentState,
            this.messageExpirationTime
        )

        is ComposerEvent.ConfirmSendExpiringMessageToExternalRecipients -> currentState.copy(
            confirmSendExpiringMessage = Effect.of(this.externalRecipients)

        )

        is ComposerEvent.RecipientsUpdated -> updateRecipients(currentState, hasValidRecipients)
        is ComposerEvent.ErrorDiscardingDraft -> updateForErrorDiscardingDraft(currentState)
        is ComposerEvent.OnMessageSending -> currentState.copy(showSendingLoading = true)
        is ComposerEvent.AddAttachmentError -> updateStateForAddAttachmentError(currentState, this.error)
        is ComposerEvent.DeleteAttachmentError -> updateStateForDeleteAttachmentError(currentState)
        is ComposerEvent.OnDraftBodyUpdated -> updateDraftBodyTo(currentState, this.draftBody, this.displayBodyUiModel)
        is ComposerEvent.InlineAttachmentAdded -> currentState.copy(
            injectInlineAttachment = Effect.of(this.contentId)
        )
    }

    private fun updateStateForDeleteAttachmentError(currentState: ComposerDraftState): ComposerDraftState =
        currentState.copy(error = Effect.of(TextUiModel(R.string.composer_delete_attachment_error)))

    private fun updateStateForAddAttachmentError(
        currentState: ComposerDraftState,
        error: AttachmentAddError
    ): ComposerDraftState = when (error) {
        AttachmentAddError.AttachmentTooLarge -> currentState.copy(attachmentsFileSizeExceeded = Effect.of(Unit))
        AttachmentAddError.EncryptionError -> currentState.copy(attachmentsEncryptionFailed = Effect.of(Unit))
        AttachmentAddError.TooManyAttachments ->
            currentState.copy(error = Effect.of(TextUiModel(R.string.composer_too_many_attachments_error)))

        AttachmentAddError.Unknown,
        AttachmentAddError.InvalidDraftMessage ->
            currentState.copy(error = Effect.of(TextUiModel(R.string.composer_unexpected_attachments_error)))
    }

    private fun updateComposerFieldsState(
        currentState: ComposerDraftState,
        draftUiModel: DraftUiModel,
        isDataRefreshed: Boolean,
        blockedSendingFromPmAddress: Boolean,
        blockedSendingFromDisabledAddress: Boolean,
        bodyShouldTakeFocus: Boolean = false
    ) = currentState.copy(
        fields = currentState.fields.copy(
            sender = SenderUiModel(draftUiModel.draftFields.sender.value),
            body = draftUiModel.draftFields.body.value,
            displayBody = draftUiModel.draftDisplayBodyUiModel
        ),
        isLoading = false,
        warning = if (!isDataRefreshed) {
            Effect.of(TextUiModel(R.string.composer_warning_local_data_shown))
        } else {
            Effect.empty()
        },
        senderChangedNotice = when {
            blockedSendingFromPmAddress ->
                Effect.of(TextUiModel(R.string.composer_sender_changed_pm_address_is_a_paid_feature))

            blockedSendingFromDisabledAddress ->
                Effect.of(TextUiModel(R.string.composer_sender_changed_original_address_disabled))

            else -> Effect.empty()
        },
        focusTextBody = if (bodyShouldTakeFocus) Effect.of(Unit) else Effect.empty()
    )

    private fun updateAttachmentsState(
        currentState: ComposerDraftState,
        attachments: List<AttachmentMetadataWithState>
    ) = currentState.copy(
        attachments = AttachmentGroupUiModel(
            limit = NO_ATTACHMENT_LIMIT,
            attachments = attachments.map {
                attachmentUiModelMapper.toUiModel(
                    attachmentMetadata = it.attachmentMetadata,
                    isDeletable = true,
                    status = it.attachmentState
                )
            }
        )
    )

    private fun updateSendingErrorState(currentState: ComposerDraftState, sendingError: TextUiModel) =
        currentState.copy(sendingErrorEffect = Effect.of(sendingError), showSendingLoading = false)

    private fun updateCloseComposerState(currentState: ComposerDraftState) = currentState.copy(
        closeComposer = Effect.of(Unit)
    )

    private fun updateCloseComposerStateWithDraftSaved(currentState: ComposerDraftState, messageId: MessageId) =
        currentState.copy(
            closeComposerWithDraftSaved = Effect.of(messageId)
        )

    private fun updateDraftBodyTo(
        currentState: ComposerDraftState,
        draftBody: DraftBody,
        draftDisplayBody: DraftDisplayBodyUiModel
    ): ComposerDraftState =
        currentState.copy(fields = currentState.fields.copy(body = draftBody.value, displayBody = draftDisplayBody))

    private fun updateStateForSendMessage(currentState: ComposerDraftState) =
        currentState.copy(closeComposerWithMessageSending = Effect.of(Unit))

    private fun updateForConfirmSendWithoutSubject(currentState: ComposerDraftState) = currentState.copy(
        closeComposerWithMessageSending = Effect.of(Unit),
        confirmSendingWithoutSubject = Effect.empty()
    )

    private fun updateForRejectSendWithoutSubject(currentState: ComposerDraftState) = currentState.copy(
        changeFocusToField = Effect.of(FocusedFieldType.SUBJECT),
        confirmSendingWithoutSubject = Effect.empty(),
        showSendingLoading = false
    )

    private fun updateStateForSendMessageOffline(currentState: ComposerDraftState) =
        currentState.copy(closeComposerWithMessageSendingOffline = Effect.of(Unit))

    private fun updateStateToPaidFeatureMessage(currentState: ComposerDraftState) =
        currentState.copy(premiumFeatureMessage = Effect.of(TextUiModel(R.string.composer_change_sender_paid_feature)))

    private fun updateStateToSenderError(currentState: ComposerDraftState) = currentState.copy(
        fields = currentState.fields.copy(sender = SenderUiModel("")),
        error = Effect.of(TextUiModel(R.string.composer_error_invalid_sender))
    )

    private fun updateSenderTo(currentState: ComposerDraftState, sender: SenderUiModel) = currentState.copy(
        fields = currentState.fields.copy(sender = sender),
        changeBottomSheetVisibility = Effect.of(false)
    )

    private fun updateReplaceDraftBodyEffect(currentState: ComposerDraftState, draftBody: DraftBody) =
        currentState.copy(
            replaceDraftBody = Effect.of(TextUiModel(draftBody.value))
        )

    private fun updateStateForMessagePassword(currentState: ComposerDraftState, messagePassword: MessagePassword?) =
        currentState.copy(isMessagePasswordSet = messagePassword != null)

    private fun updateStateForSetExpirationTimeRequested(currentState: ComposerDraftState) =
        currentState.copy(changeBottomSheetVisibility = Effect.of(true))

    private fun updateStateForExpirationTimeSet(currentState: ComposerDraftState) =
        currentState.copy(changeBottomSheetVisibility = Effect.of(false))

    private fun updateStateForMessageExpirationTime(
        currentState: ComposerDraftState,
        messageExpirationTime: MessageExpirationTime?
    ) = currentState.copy(messageExpiresIn = messageExpirationTime?.expiresIn ?: Duration.ZERO)

    private fun updateForOnAddAttachments(currentState: ComposerDraftState) = currentState.copy(
        openImagePicker = Effect.of(Unit)
    )

    private fun updateStateForDiscardDraft(currentState: ComposerDraftState) = currentState.copy(
        confirmDiscardDraft = Effect.of(Unit)
    )

    private fun updateStateForDiscardDraftConfirmed(currentState: ComposerDraftState) = currentState.copy(
        closeComposer = Effect.of(Unit)
    )

    private fun updateRecipients(currentState: ComposerDraftState, hasValidRecipients: Boolean) =
        currentState.copy(isSubmittable = hasValidRecipients)

    private fun updateForErrorDiscardingDraft(currentState: ComposerDraftState) = currentState.copy(
        error = Effect.of(TextUiModel.TextRes(R.string.discard_draft_error))
    )
}
