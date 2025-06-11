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

package ch.protonmail.android.maildetail.presentation.model

import android.net.Uri
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentState
import ch.protonmail.android.mailattachments.domain.model.OpenAttachmentIntentValues
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetOperation
import ch.protonmail.android.maildetail.domain.model.OpenProtonCalendarIntentValues
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingConversation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingDeleteDialog
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingErrorBar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingMarkAsLegitimateDialog
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingMessageBar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingMessages
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingReportPhishingDialog
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingTrashedMessagesBanner
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToBottomSheetEntryPoint
import ch.protonmail.android.maillabel.presentation.model.MailLabelText
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageTheme
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentListExpandCollapseMode
import kotlinx.collections.immutable.ImmutableList

sealed interface ConversationDetailOperation {

    sealed interface AffectingConversation : ConversationDetailOperation
    sealed interface AffectingMessages : ConversationDetailOperation
    sealed interface AffectingErrorBar
    sealed interface AffectingMessageBar
    sealed interface AffectingBottomSheet
    sealed interface AffectingDeleteDialog
    sealed interface AffectingReportPhishingDialog
    sealed interface AffectingTrashedMessagesBanner
    sealed interface AffectingMarkAsLegitimateDialog
}

sealed interface ConversationDetailEvent : ConversationDetailOperation {

    data class ConversationBottomBarEvent(val bottomBarEvent: BottomBarEvent) : ConversationDetailEvent

    data class ConversationData(
        val conversationUiModel: ConversationDetailMetadataUiModel
    ) : ConversationDetailEvent, AffectingConversation

    object ErrorLoadingConversation : ConversationDetailEvent, AffectingConversation, AffectingMessages
    object ErrorLoadingMessages : ConversationDetailEvent, AffectingMessages
    object NoNetworkError : ConversationDetailEvent, AffectingMessages

    data class MessagesData(
        val messagesUiModels: ImmutableList<ConversationDetailMessageUiModel>,
        val requestScrollToMessageId: MessageIdUiModel?,
        val filterByLocation: LabelId?,
        val shouldHideMessagesBasedOnTrashFilter: Boolean
    ) : ConversationDetailEvent, AffectingMessages, AffectingTrashedMessagesBanner

    data class ConversationBottomSheetEvent(
        val bottomSheetOperation: BottomSheetOperation
    ) : ConversationDetailEvent, AffectingBottomSheet

    data class MessageBottomSheetEvent(
        val bottomSheetOperation: BottomSheetOperation
    ) : ConversationDetailEvent, AffectingBottomSheet

    object ErrorAddStar : ConversationDetailEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorRemoveStar : ConversationDetailEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorMarkingAsRead : ConversationDetailEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorMarkingAsUnread : ConversationDetailEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorMovingToTrash : ConversationDetailEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorMovingConversation : ConversationDetailEvent, AffectingBottomSheet, AffectingErrorBar
    object ErrorMovingMessage : ConversationDetailEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorLabelingConversation : ConversationDetailEvent, AffectingBottomSheet, AffectingErrorBar
    object ErrorGettingAttachment : ConversationDetailEvent, AffectingErrorBar
    object ErrorGettingAttachmentNotEnoughSpace : ConversationDetailEvent, AffectingErrorBar
    object ErrorAttachmentDownloadInProgress : ConversationDetailEvent, AffectingErrorBar
    object ErrorDeletingConversation :
        ConversationDetailEvent,
        AffectingBottomSheet,
        AffectingErrorBar,
        AffectingDeleteDialog

    object ErrorDeletingMessage :
        ConversationDetailEvent, AffectingBottomSheet, AffectingErrorBar, AffectingDeleteDialog

    data class ExpandDecryptedMessage(
        val messageId: MessageIdUiModel,
        val conversationDetailMessageUiModel: ConversationDetailMessageUiModel.Expanded
    ) : ConversationDetailEvent, AffectingMessages

    data class CollapseDecryptedMessage(
        val messageId: MessageIdUiModel,
        val conversationDetailMessageUiModel: ConversationDetailMessageUiModel.Collapsed
    ) : ConversationDetailEvent, AffectingMessages

    data class ShowAllAttachmentsForMessage(
        val messageId: MessageIdUiModel,
        val conversationDetailMessageUiModel: ConversationDetailMessageUiModel.Expanded
    ) : ConversationDetailEvent, AffectingMessages

    data class ErrorExpandingDecryptMessageError(val messageId: MessageIdUiModel) :
        ConversationDetailEvent, AffectingMessages, AffectingErrorBar

    data class ErrorExpandingRetrieveMessageError(val messageId: MessageIdUiModel) :
        ConversationDetailEvent, AffectingMessages, AffectingErrorBar

    data class ErrorExpandingRetrievingMessageOffline(val messageId: MessageIdUiModel) :
        ConversationDetailEvent, AffectingMessages, AffectingErrorBar

    data class ExpandingMessage(
        val messageId: MessageIdUiModel,
        val conversationDetailMessageUiModel: ConversationDetailMessageUiModel.Collapsed
    ) : ConversationDetailEvent, AffectingMessages

    data class AttachmentStatusChanged(
        val messageId: MessageIdUiModel,
        val attachmentId: AttachmentId,
        val status: AttachmentState
    ) : ConversationDetailEvent, AffectingMessages

    data class AttachmentListExpandCollapseModeChanged(
        val messageId: MessageIdUiModel,
        val expandCollapseMode: AttachmentListExpandCollapseMode
    ) : ConversationDetailEvent, AffectingMessages

    data class OpenAttachmentEvent(val values: OpenAttachmentIntentValues) : ConversationDetailEvent

    data class HandleOpenProtonCalendarRequest(val intent: OpenProtonCalendarIntentValues) : ConversationDetailEvent

    data class MessageMoved(
        val mailLabelText: MailLabelText
    ) : ConversationDetailEvent, AffectingBottomSheet, AffectingMessageBar

    data class LastMessageMoved(val mailLabelText: MailLabelText) : ConversationDetailEvent, AffectingBottomSheet

    data object ExitScreen : ConversationDetailEvent, AffectingBottomSheet
    data class ExitScreenWithMessage(val operation: ConversationDetailOperation) :
        ConversationDetailEvent,
        AffectingBottomSheet

    data class ScheduleSendCancelled(val messageId: MessageIdUiModel) : ConversationDetailEvent
}

sealed interface ConversationDetailViewAction : ConversationDetailOperation {

    object Star : ConversationDetailViewAction, AffectingConversation, AffectingBottomSheet
    object UnStar : ConversationDetailViewAction, AffectingConversation, AffectingBottomSheet
    object MarkRead : ConversationDetailViewAction, AffectingBottomSheet
    object MarkUnread : ConversationDetailViewAction, AffectingBottomSheet
    object MoveToArchive : ConversationDetailViewAction, AffectingBottomSheet
    object MoveToSpam : ConversationDetailViewAction, AffectingBottomSheet
    object MoveToTrash : ConversationDetailViewAction, AffectingBottomSheet
    object MoveToInbox : ConversationDetailViewAction, AffectingBottomSheet
    object DeleteRequested : ConversationDetailViewAction, AffectingDeleteDialog
    object DeleteDialogDismissed : ConversationDetailViewAction, AffectingDeleteDialog
    object DeleteConfirmed : ConversationDetailViewAction, AffectingDeleteDialog, AffectingBottomSheet
    object RequestConversationMoveToBottomSheet : ConversationDetailViewAction, AffectingBottomSheet
    object DismissBottomSheet : ConversationDetailViewAction, AffectingBottomSheet

    object RequestConversationLabelAsBottomSheet : ConversationDetailViewAction, AffectingBottomSheet
    data class RequestMessageMoreActionsBottomSheet(val messageId: MessageId, val themeOptions: MessageThemeOptions) :
        ConversationDetailViewAction, AffectingBottomSheet

    data object RequestConversationMoreActionsBottomSheet : ConversationDetailViewAction, AffectingBottomSheet

    data class ExpandMessage(val messageId: MessageIdUiModel) : ConversationDetailViewAction
    data class CollapseMessage(val messageId: MessageIdUiModel) : ConversationDetailViewAction
    data class MessageBodyLinkClicked(val messageId: MessageIdUiModel, val uri: Uri) : ConversationDetailViewAction
    object DoNotAskLinkConfirmationAgain : ConversationDetailViewAction
    data class RequestScrollTo(val messageId: MessageIdUiModel) : ConversationDetailViewAction
    object ScrollRequestCompleted : ConversationDetailViewAction
    data class ShowAllAttachmentsForMessage(val messageId: MessageIdUiModel) : ConversationDetailViewAction
    data class OnAttachmentClicked(val messageId: MessageIdUiModel, val attachmentId: AttachmentId) :
        ConversationDetailViewAction

    data class ExpandOrCollapseAttachmentList(val messageId: MessageIdUiModel) :
        ConversationDetailViewAction

    data class ExpandOrCollapseMessageBody(val messageId: MessageIdUiModel) : ConversationDetailViewAction
    data class LoadRemoteContent(val messageId: MessageIdUiModel) : ConversationDetailViewAction
    data class ShowEmbeddedImages(val messageId: MessageIdUiModel) : ConversationDetailViewAction
    data class LoadRemoteAndEmbeddedContent(val messageId: MessageIdUiModel) : ConversationDetailViewAction

    data class ReportPhishing(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingBottomSheet, AffectingReportPhishingDialog

    object ReportPhishingDismissed : ConversationDetailViewAction, AffectingReportPhishingDialog
    data class ReportPhishingConfirmed(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingReportPhishingDialog

    data class OpenInProtonCalendar(val messageId: MessageId) : ConversationDetailViewAction
    data class SwitchViewMode(
        val messageId: MessageId,
        val currentTheme: MessageTheme,
        val overrideTheme: MessageTheme
    ) : ConversationDetailViewAction, AffectingBottomSheet

    data class MarkMessageUnread(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingBottomSheet

    data class RequestContactActionsBottomSheet(
        val participant: ParticipantUiModel,
        val avatarUiModel: AvatarUiModel?
    ) : ConversationDetailViewAction,
        AffectingBottomSheet

    data class RequestMessageLabelAsBottomSheet(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingBottomSheet

    data class LabelAsCompleted(
        val wasArchived: Boolean,
        val entryPoint: LabelAsBottomSheetEntryPoint
    ) : ConversationDetailViewAction,
        AffectingBottomSheet,
        AffectingMessageBar

    data class RequestMessageMoveToBottomSheet(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingBottomSheet

    data class MoveToCompleted(
        val mailLabelText: MailLabelText,
        val entryPoint: MoveToBottomSheetEntryPoint
    ) : ConversationDetailViewAction,
        AffectingBottomSheet,
        AffectingMessageBar

    data class DeleteMessageRequested(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingDeleteDialog

    data class DeleteMessageConfirmed(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingDeleteDialog, AffectingBottomSheet

    data class StarMessage(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingBottomSheet

    data class UnStarMessage(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingBottomSheet

    data object ChangeVisibilityOfMessages : ConversationDetailViewAction

    data class OnAvatarImageLoadRequested(
        val avatar: AvatarUiModel
    ) : ConversationDetailViewAction

    sealed class MoveMessage(
        open val messageId: MessageId,
        open val mailLabelText: MailLabelText
    ) : ConversationDetailViewAction, AffectingBottomSheet {

        sealed class System(
            override val messageId: MessageId,
            val labelId: SystemLabelId,
            override val mailLabelText: MailLabelText
        ) : MoveMessage(messageId, mailLabelText) {

            class Spam(messageId: MessageId) :
                System(messageId, SystemLabelId.Spam, MailLabelText(R.string.label_title_spam))

            class Trash(messageId: MessageId) :
                System(messageId, SystemLabelId.Trash, MailLabelText(R.string.label_title_trash))

            class Archive(messageId: MessageId) :
                System(messageId, SystemLabelId.Archive, MailLabelText(R.string.label_title_archive))

            class Inbox(messageId: MessageId) :
                System(messageId, SystemLabelId.Inbox, MailLabelText(R.string.label_title_inbox))
        }

        class CustomFolder(
            override val messageId: MessageId,
            val labelId: LabelId,
            override val mailLabelText: MailLabelText
        ) : MoveMessage(messageId, mailLabelText)
    }

    data class MarkMessageAsLegitimate(
        val messageId: MessageId,
        val isPhishing: Boolean
    ) : ConversationDetailViewAction, AffectingMarkAsLegitimateDialog

    data class MarkMessageAsLegitimateConfirmed(
        val messageId: MessageId
    ) : ConversationDetailViewAction, AffectingMarkAsLegitimateDialog

    data object MarkMessageAsLegitimateDismissed : ConversationDetailViewAction, AffectingMarkAsLegitimateDialog

    data class UnblockSender(val messageId: MessageIdUiModel, val email: String) : ConversationDetailViewAction

    data class EditScheduleSendMessage(val messageId: MessageIdUiModel) : ConversationDetailViewAction
}
