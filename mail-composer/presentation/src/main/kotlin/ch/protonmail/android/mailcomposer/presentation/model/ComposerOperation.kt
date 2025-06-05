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

package ch.protonmail.android.mailcomposer.presentation.model

import android.net.Uri
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import kotlin.time.Duration

sealed interface ComposerOperation

internal sealed interface ComposerAction : ComposerOperation {
    data class FileAttachmentsAdded(val uriList: List<Uri>) : ComposerAction
    data class AttachmentsAdded(val uriList: List<Uri>) : ComposerAction
    data class SenderChanged(val sender: SenderUiModel) : ComposerAction
    data class ExpirationTimeSet(val duration: Duration) : ComposerAction

    data class DraftBodyChanged(val draftBody: DraftBody) : ComposerAction
    data class RemoveAttachment(val attachmentId: AttachmentId) : ComposerAction
    data class RemoveInlineImage(val contentId: String) : ComposerAction

    data object ChangeSenderRequested : ComposerAction
    data object OnAddAttachments : ComposerAction
    data object OnAttachFromFiles : ComposerAction
    data object OnAttachFromPhotos : ComposerAction
    data object OnAttachFromCamera : ComposerAction
    data object OnCloseComposer : ComposerAction
    data object OnSendMessage : ComposerAction
    data object OnScheduleSendRequested : ComposerAction
    data object OnSetExpirationTimeRequested : ComposerAction
    data object ConfirmSendingWithoutSubject : ComposerAction
    data object RejectSendingWithoutSubject : ComposerAction
    data object SendExpiringMessageToExternalRecipientsConfirmed : ComposerAction
    data object DiscardDraft : ComposerAction
    data object DiscardDraftConfirmed : ComposerAction
    data object OnInlineImageActionsRequested : ComposerAction
}

sealed interface ComposerEvent : ComposerOperation {
    data class DefaultSenderReceived(val sender: SenderUiModel) : ComposerEvent
    data class SenderAddressesReceived(val senders: List<SenderUiModel>) : ComposerEvent
    data object OpenDraft : ComposerEvent
    data class PrefillDraftDataReceived(
        val draftUiModel: DraftUiModel,
        val isDataRefreshed: Boolean,
        val isBlockedSendingFromPmAddress: Boolean,
        val isBlockedSendingFromDisabledAddress: Boolean,
        val bodyShouldTakeFocus: Boolean
    ) : ComposerEvent
    data class PrefillDataReceivedViaShare(val draftUiModel: DraftUiModel) : ComposerEvent
    data class ReplaceDraftBody(val draftBody: DraftBody) : ComposerEvent
    data class OnAttachmentsUpdated(val attachments: List<AttachmentMetadataWithState>) : ComposerEvent
    data class OnSendingError(val sendingError: TextUiModel) : ComposerEvent
    data class OnMessagePasswordUpdated(val messagePassword: MessagePassword?) : ComposerEvent
    data class OnMessageExpirationTimeUpdated(val messageExpirationTime: MessageExpirationTime?) : ComposerEvent
    data class ConfirmSendExpiringMessageToExternalRecipients(val externalRecipients: List<Recipient>) : ComposerEvent
    data class RecipientsUpdated(val hasValidRecipients: Boolean) : ComposerEvent
    data class OnCloseWithDraftSaved(val messageId: MessageId) : ComposerEvent
    data class OnDraftBodyUpdated(
        val draftBody: DraftBody,
        val displayBodyUiModel: DraftDisplayBodyUiModel
    ) : ComposerEvent
    data class ScheduleSendOptionsData(val options: ScheduleSendOptionsUiModel) : ComposerEvent

    data object ErrorLoadingDefaultSenderAddress : ComposerEvent
    data object ErrorStoringDraftBody : ComposerEvent
    data object ErrorStoringDraftRecipients : ComposerEvent
    data object ErrorStoringDraftSubject : ComposerEvent
    data object OnSendMessageOffline : ComposerEvent
    data object ErrorLoadingDraftData : ComposerEvent
    data object ErrorLoadingParentMessageData : ComposerEvent
    data object ConfirmEmptySubject : ComposerEvent
    data object ErrorDiscardingDraft : ComposerEvent
    data object OnMessageSending : ComposerEvent
    data object DeleteAttachmentError : ComposerEvent
    data class AddAttachmentError(val error: AttachmentAddError) : ComposerEvent
    data class InlineAttachmentAdded(val contentId: String) : ComposerEvent
    data class InlineAttachmentRemoved(val contentId: String) : ComposerEvent
    data object ErrorGetScheduleSendOptions : ComposerEvent
}
