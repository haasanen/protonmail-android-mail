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

package ch.protonmail.android.mailcomposer.presentation.model.operations

import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentDeleteError
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.ComposerStateModifications
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.BottomSheetEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.CompletionEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.ConfirmationsEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.ContentEffectsStateModifications
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.LoadingError
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.RecoverableError
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.UnrecoverableError
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient

internal sealed interface EffectsEvent : ComposerStateEvent {

    override fun toStateModifications(): ComposerStateModifications

    sealed interface DraftEvent : EffectsEvent {

        override fun toStateModifications(): ComposerStateModifications = ComposerStateModifications(
            effectsModification = when (this) {
                is OnDraftLoadingFailed -> LoadingError.DraftContent
                is OnDiscardDraftRequested -> ConfirmationsEffectsStateModification.DiscardDraftConfirmationRequested
            }
        )

        data object OnDraftLoadingFailed : DraftEvent
        data object OnDiscardDraftRequested : DraftEvent
    }

    sealed interface LoadingEvent : EffectsEvent {

        override fun toStateModifications(): ComposerStateModifications = ComposerStateModifications(
            effectsModification = when (this) {
                OnParentLoadingFailed -> UnrecoverableError.ParentMessageMetadata
                OnSenderAddressLoadingFailed -> UnrecoverableError.InvalidSenderAddress
            }
        )

        data object OnParentLoadingFailed : LoadingEvent
        data object OnSenderAddressLoadingFailed : LoadingEvent
    }

    sealed interface AttachmentEvent : EffectsEvent {

        override fun toStateModifications(): ComposerStateModifications = ComposerStateModifications(
            effectsModification = when (this) {
                is AddAttachmentError -> RecoverableError.AttachmentsStore(error)
                OnAddFileRequest -> ContentEffectsStateModifications.OnAddAttachmentFileRequested
                OnAddFromCameraRequest -> ContentEffectsStateModifications.OnAddAttachmentCameraRequested
                OnAddMediaRequest -> ContentEffectsStateModifications.OnAddAttachmentPhotosRequested
                is InlineAttachmentAdded -> ContentEffectsStateModifications.OnInlineAttachmentAdded(contentId)
                is InlineAttachmentRemoved -> ContentEffectsStateModifications.OnInlineAttachmentRemoved(contentId)
                is OnAttachFromOptionsRequest -> BottomSheetEffectsStateModification.ShowBottomSheet
                is OnInlineImageActionsRequested -> BottomSheetEffectsStateModification.ShowBottomSheet
                is RemoveAttachmentError -> RecoverableError.AttachmentRemove(error)
                is OnLoadAttachmentsFailed -> RecoverableError.LoadingAttachmentsFailed
            }
        )

        data class RemoveAttachmentError(val error: AttachmentDeleteError) : AttachmentEvent
        data class AddAttachmentError(val error: AttachmentAddError) : AttachmentEvent
        data class InlineAttachmentAdded(val contentId: String) : AttachmentEvent
        data class InlineAttachmentRemoved(val contentId: String) : AttachmentEvent

        data object OnInlineImageActionsRequested : AttachmentEvent
        data object OnAttachFromOptionsRequest : AttachmentEvent
        data object OnAddFileRequest : AttachmentEvent
        data object OnAddMediaRequest : AttachmentEvent
        data object OnAddFromCameraRequest : AttachmentEvent
        data object OnLoadAttachmentsFailed : AttachmentEvent
    }

    sealed interface ComposerControlEvent : EffectsEvent {

        override fun toStateModifications(): ComposerStateModifications = ComposerStateModifications(
            effectsModification = when (this) {
                is OnCloseRequest -> CompletionEffectsStateModification.CloseComposer.CloseComposerNoDraft
                is OnComposerRestored -> CompletionEffectsStateModification.CloseComposer.CloseComposerNoDraft
                is OnCloseRequestWithDraft ->
                    CompletionEffectsStateModification.CloseComposer.CloseComposerDraftSaved(this.draftId)
            }
        )

        data object OnCloseRequest : ComposerControlEvent
        data class OnCloseRequestWithDraft(val draftId: MessageId) : ComposerControlEvent
        data object OnComposerRestored : ComposerControlEvent
    }

    sealed interface ErrorEvent : EffectsEvent {

        override fun toStateModifications(): ComposerStateModifications = ComposerStateModifications(
            effectsModification = when (this) {
                OnSenderChangeFreeUserError -> RecoverableError.SenderChange.FreeUser
                OnGetAddressesError -> RecoverableError.SenderChange.GetAddressesError
                OnSetExpirationError -> RecoverableError.Expiration
                OnDiscardDraftError -> RecoverableError.DiscardDraftFailed
                OnStoreBodyError -> RecoverableError.SaveBodyFailed
                OnStoreRecipientError -> RecoverableError.SaveRecipientFailed
                OnStoreSubjectError -> RecoverableError.SaveSubjectFailed
                OnSendMessageError -> RecoverableError.SendMessageFailed
                OnGetScheduleSendOptionsError -> RecoverableError.GetScheduleSendOptionsFailed
                OnAddressNotValidForSending -> RecoverableError.SenderChange.AddressCanNotSend
                OnChangeSenderFailure -> RecoverableError.SenderChange.ChangeSenderFailure
                OnRefreshBodyFailed -> RecoverableError.SenderChange.RefreshBodyFailure
            }
        )

        data object OnSenderChangeFreeUserError : ErrorEvent
        data object OnGetAddressesError : ErrorEvent
        data object OnSetExpirationError : ErrorEvent
        data object OnDiscardDraftError : ErrorEvent
        data object OnStoreBodyError : ErrorEvent
        data object OnStoreSubjectError : ErrorEvent
        data object OnStoreRecipientError : ErrorEvent
        data object OnSendMessageError : ErrorEvent
        data object OnGetScheduleSendOptionsError : ErrorEvent
        data object OnAddressNotValidForSending : ErrorEvent
        data object OnRefreshBodyFailed : ErrorEvent
        data object OnChangeSenderFailure : ErrorEvent
    }

    sealed interface SendEvent : EffectsEvent {

        override fun toStateModifications(): ComposerStateModifications = ComposerStateModifications(
            effectsModification = when (this) {
                is OnCancelSendNoSubject -> ConfirmationsEffectsStateModification.CancelSendNoSubject
                is OnSendExpiringToExternalRecipients ->
                    ConfirmationsEffectsStateModification.ShowExternalExpiringRecipients(externalRecipients)

                is OnSendMessage -> CompletionEffectsStateModification.SendMessage.SendAndExit
                is OnOfflineSendMessage -> CompletionEffectsStateModification.SendMessage.SendAndExitOffline
                is OnSendingError -> RecoverableError.SendingFailed(message)
                is OnScheduleSendMessage -> CompletionEffectsStateModification.ScheduleMessage.ScheduleAndExit
                is OnOfflineScheduleSendMessage ->
                    CompletionEffectsStateModification.ScheduleMessage.ScheduleAndExitOffline
            }
        )

        data object OnSendMessage : SendEvent
        data object OnOfflineSendMessage : SendEvent

        data object OnScheduleSendMessage : SendEvent
        data object OnOfflineScheduleSendMessage : SendEvent

        data object OnCancelSendNoSubject : SendEvent
        data class OnSendExpiringToExternalRecipients(val externalRecipients: List<Recipient>) : SendEvent
        data class OnSendingError(val message: String) : SendEvent
    }

    data object SetExpirationReady : EffectsEvent {

        override fun toStateModifications(): ComposerStateModifications = ComposerStateModifications(
            effectsModification = BottomSheetEffectsStateModification.ShowBottomSheet
        )
    }
}
