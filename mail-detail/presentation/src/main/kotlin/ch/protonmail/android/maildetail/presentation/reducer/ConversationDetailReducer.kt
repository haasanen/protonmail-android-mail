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

package ch.protonmail.android.maildetail.presentation.reducer

import ch.protonmail.android.mailattachments.domain.model.OpenAttachmentIntentValues
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.ActionResult
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetOperation
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.reducer.BottomBarReducer
import ch.protonmail.android.maildetail.domain.model.OpenProtonCalendarIntentValues
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.mapper.ActionResultMapper
import ch.protonmail.android.maildetail.presentation.model.ConversationDeleteState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.HandleOpenProtonCalendarRequest
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.OfflineErrorCancellingScheduleSend
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ExitScreenWithMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ExitScreen
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.LastMessageMoved
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.MessageMoved
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorDeletingMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.MessageBottomSheetEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ScheduleSendCancelled
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorUnsnoozing
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ConversationBottomBarEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ConversationBottomSheetEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorAddStar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorMarkingAsRead
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorAttachmentDownloadInProgress
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorDeletingConversation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorExpandingDecryptMessageError
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorAnsweringRsvpEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorExpandingRetrieveMessageError
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorExpandingRetrievingMessageOffline
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorGettingAttachment
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorGettingAttachmentNotEnoughSpace
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorLabelingConversation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorCancellingScheduleSend
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorMarkingAsUnread
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorMovingConversation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorMovingMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorMovingToTrash
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ErrorRemoveStar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.MessagesData
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.OpenAttachmentEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingDeleteDialog
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailsMessagesState
import ch.protonmail.android.maildetail.presentation.model.EditScheduledMessageDialogState
import ch.protonmail.android.maildetail.presentation.model.MarkAsLegitimateDialogState
import ch.protonmail.android.maildetail.presentation.model.MessageBodyLink
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.model.ReportPhishingDialogState
import ch.protonmail.android.maildetail.presentation.model.TrashedMessagesBannerState
import ch.protonmail.android.mailmessage.presentation.reducer.BottomSheetReducer
import javax.inject.Inject

class ConversationDetailReducer @Inject constructor(
    private val bottomBarReducer: BottomBarReducer,
    private val metadataReducer: ConversationDetailMetadataReducer,
    private val messagesReducer: ConversationDetailMessagesReducer,
    private val bottomSheetReducer: BottomSheetReducer,
    private val deleteDialogReducer: ConversationDeleteDialogReducer,
    private val reportPhishingDialogReducer: ConversationReportPhishingDialogReducer,
    private val trashedMessagesBannerReducer: TrashedMessagesBannerReducer,
    private val markAsLegitimateDialogReducer: MarkAsLegitimateDialogReducer,
    private val editScheduledMessageDialogReducer: EditScheduledMessageDialogReducer,
    private val actionResultMapper: ActionResultMapper
) {

    suspend fun newStateFrom(
        currentState: ConversationDetailState,
        operation: ConversationDetailOperation
    ): ConversationDetailState {
        return currentState.copy(
            conversationState = currentState.toNewConversationState(operation),
            messagesState = currentState.toNewMessageState(operation),
            bottomBarState = currentState.toNewBottomBarState(operation),
            bottomSheetState = currentState.toNewBottomSheetStateFrom(operation),
            error = currentState.toErrorState(operation),
            actionResult = currentState.toActionResult(operation),
            exitScreenEffect = currentState.toExitState(operation),
            exitScreenActionResult = currentState.toExitWithMessageState(operation),
            openMessageBodyLinkEffect = currentState.toOpenMessageBodyLinkState(operation),
            openAttachmentEffect = currentState.toNewOpenAttachmentStateFrom(operation),
            openProtonCalendarIntent = currentState.toNewOpenProtonCalendarIntentFrom(operation),
            onExitWithNavigateToComposer = currentState.toOpenComposerEffectState(operation),
            scrollToMessage = currentState.toScrollToMessageState(operation),
            conversationDeleteState = currentState.toNewDeleteDialogState(operation),
            reportPhishingDialogState = currentState.toNewReportPhishingDialogState(operation),
            trashedMessagesBannerState = currentState.toNewTrashedMessagesBannerState(operation),
            markAsLegitimateDialogState = currentState.toNewMarkAsLegitimateDialogState(operation),
            editScheduledMessageDialogState = currentState.toNewEditScheduleMessageDialogState(operation)
        )
    }

    private fun ConversationDetailState.toNewEditScheduleMessageDialogState(
        operation: ConversationDetailOperation
    ): EditScheduledMessageDialogState {
        return if (operation is ConversationDetailOperation.AffectingEditScheduleMessageDialog) {
            editScheduledMessageDialogReducer.newStateFrom(operation)
        } else {
            editScheduledMessageDialogState
        }
    }

    private fun ConversationDetailState.toOpenComposerEffectState(
        operation: ConversationDetailOperation
    ): Effect<MessageIdUiModel> = when (operation) {
        is ScheduleSendCancelled -> Effect.of(operation.messageId)

        else -> onExitWithNavigateToComposer
    }

    private fun ConversationDetailState.toNewConversationState(operation: ConversationDetailOperation) =
        if (operation is ConversationDetailOperation.AffectingConversation) {
            metadataReducer.newStateFrom(conversationState, operation)
        } else {
            conversationState
        }

    private suspend fun ConversationDetailState.toNewMessageState(operation: ConversationDetailOperation) =
        if (operation is ConversationDetailOperation.AffectingMessages) {
            messagesReducer.newStateFrom(messagesState, operation)
        } else {
            messagesState
        }

    private fun ConversationDetailState.toNewBottomBarState(operation: ConversationDetailOperation) =
        if (operation is ConversationBottomBarEvent) {
            bottomBarReducer.newStateFrom(bottomBarState, operation.bottomBarEvent)
        } else {
            bottomBarState
        }

    private fun ConversationDetailState.toNewBottomSheetStateFrom(operation: ConversationDetailOperation) =
        if (operation is ConversationDetailOperation.AffectingBottomSheet) {
            val bottomSheetOperation = when (operation) {
                is ConversationBottomSheetEvent -> operation.bottomSheetOperation
                is MessageBottomSheetEvent -> operation.bottomSheetOperation
                is ConversationDetailViewAction.RequestContactActionsBottomSheet,
                is ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet,
                is ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet,
                is ConversationDetailViewAction.RequestConversationLabelAsBottomSheet,
                is ConversationDetailViewAction.RequestMessageLabelAsBottomSheet,
                is ConversationDetailViewAction.RequestConversationMoveToBottomSheet,
                is ConversationDetailViewAction.RequestSnoozeBottomSheet,
                is ConversationDetailViewAction.RequestMessageMoveToBottomSheet -> BottomSheetOperation.Requested

                is ErrorMovingConversation,
                is ErrorLabelingConversation,
                is ErrorAddStar,
                is ErrorDeletingConversation,
                is ErrorDeletingMessage,
                is ErrorMarkingAsRead,
                is ErrorMarkingAsUnread,
                is ErrorMovingMessage,
                is ErrorMovingToTrash,
                is ErrorRemoveStar,
                is ConversationDetailViewAction.MarkRead,
                is ConversationDetailViewAction.MarkUnread,
                is ConversationDetailViewAction.Star,
                is ConversationDetailViewAction.UnStar,
                is ConversationDetailViewAction.ReportPhishing,
                is ConversationDetailViewAction.DismissBottomSheet,
                is ConversationDetailViewAction.SwitchViewMode,
                is ConversationDetailViewAction.MarkMessageUnread,
                is ConversationDetailViewAction.DeleteConfirmed,
                is ConversationDetailViewAction.DeleteMessageConfirmed,
                is ConversationDetailViewAction.MoveToInbox,
                is ConversationDetailViewAction.MoveToSpam,
                is ConversationDetailViewAction.MoveToTrash,
                is ConversationDetailViewAction.MoveToArchive,
                is ConversationDetailViewAction.StarMessage,
                is ConversationDetailViewAction.UnStarMessage,
                is ConversationDetailViewAction.MoveMessage,
                is ConversationDetailViewAction.LabelAsCompleted,
                is ConversationDetailViewAction.MoveToCompleted,
                is ConversationDetailViewAction.PrintMessage,
                is MessageMoved,
                is LastMessageMoved,
                is ExitScreen,
                is ConversationDetailViewAction.SnoozeCompleted,
                ConversationDetailViewAction.SnoozeDismissed,
                ConversationDetailViewAction.OnUnsnoozeConversationRequested,
                is ExitScreenWithMessage -> BottomSheetOperation.Dismiss
            }
            bottomSheetReducer.newStateFrom(bottomSheetState, bottomSheetOperation)
        } else {
            bottomSheetState
        }

    @Suppress("ComplexMethod")
    private fun ConversationDetailState.toErrorState(operation: ConversationDetailOperation): Effect<TextUiModel> {
        return if (operation is ConversationDetailOperation.AffectingErrorBar) {
            val textResource = when (operation) {
                is ErrorAddStar -> R.string.error_star_operation_failed
                is ErrorRemoveStar -> R.string.error_unstar_operation_failed
                is ErrorMarkingAsRead -> R.string.error_mark_as_read_failed
                is ErrorMarkingAsUnread -> R.string.error_mark_as_unread_failed
                is ErrorMovingToTrash -> R.string.error_move_to_trash_failed
                is ErrorMovingConversation -> R.string.error_move_conversation_failed
                is ErrorMovingMessage -> R.string.error_move_message_failed
                is ErrorLabelingConversation -> R.string.error_relabel_message_failed
                is ErrorExpandingDecryptMessageError -> R.string.decryption_error
                is ErrorExpandingRetrieveMessageError -> R.string.detail_error_retrieving_message_body
                is ErrorExpandingRetrievingMessageOffline -> R.string.error_offline_loading_message
                is ErrorGettingAttachment -> R.string.error_get_attachment_failed
                is ErrorGettingAttachmentNotEnoughSpace -> R.string.error_get_attachment_not_enough_memory
                is ErrorAttachmentDownloadInProgress -> R.string.error_attachment_download_in_progress
                is ErrorDeletingConversation -> R.string.error_delete_conversation_failed
                is ErrorUnsnoozing -> R.string.snooze_sheet_error_unable_to_unsnooze
                is ErrorDeletingMessage -> R.string.error_delete_message_failed
                is ErrorCancellingScheduleSend -> R.string.error_cancel_schedule_send_failed
                is OfflineErrorCancellingScheduleSend ->
                    R.string.offline_error_cancel_schedule_send_failed
                is ErrorAnsweringRsvpEvent -> R.string.rsvp_widget_error_answering
            }
            Effect.of(TextUiModel(textResource))
        } else {
            error
        }
    }

    private fun ConversationDetailState.toActionResult(operation: ConversationDetailOperation): Effect<ActionResult> {
        return when (operation) {
            is ConversationDetailOperation.AffectingMessageBar -> {
                val targetActionResult = actionResultMapper.toActionResult(operation)
                if (targetActionResult != null) {
                    Effect.of(targetActionResult)
                } else {
                    actionResult
                }
            }

            else -> actionResult
        }
    }

    private fun ConversationDetailState.toExitState(operation: ConversationDetailOperation): Effect<Unit> =
        when (operation) {
            is ExitScreen -> Effect.of(Unit)
            is ConversationDetailViewAction.ReportPhishingConfirmed -> when (messagesState) {
                is ConversationDetailsMessagesState.Data -> if (messagesState.messages.size > 1) {
                    exitScreenEffect
                } else {
                    Effect.of(Unit)
                }

                else -> exitScreenEffect
            }

            else -> exitScreenEffect
        }

    private fun ConversationDetailState.toExitWithMessageState(
        operation: ConversationDetailOperation
    ): Effect<ActionResult> = when (operation) {
        is ExitScreenWithMessage -> {
            val actionResult = actionResultMapper.toActionResult(operation.operation)
            if (actionResult != null) {
                Effect.of(actionResult)
            } else {
                exitScreenActionResult
            }
        }

        is LastMessageMoved -> {
            val actionResult = actionResultMapper.toActionResult(operation)
            if (actionResult != null) {
                Effect.of(actionResult)
            } else {
                exitScreenActionResult
            }
        }

        else -> exitScreenActionResult
    }

    private fun ConversationDetailState.toOpenMessageBodyLinkState(
        operation: ConversationDetailOperation
    ): Effect<MessageBodyLink> = when (operation) {
        is ConversationDetailViewAction.MessageBodyLinkClicked -> Effect.of(
            MessageBodyLink(operation.messageId, operation.uri)
        )

        else -> openMessageBodyLinkEffect
    }

    private fun ConversationDetailState.toScrollToMessageState(
        operation: ConversationDetailOperation
    ): MessageIdUiModel? = when (operation) {
        // Scroll to message requested
        is ConversationDetailViewAction.RequestScrollTo -> operation.messageId

        // Scroll to message completed, so we need to clear the state
        is ConversationDetailViewAction.ScrollRequestCompleted -> null

        // ConversationDetailEvent.MessagesData update should not clear the scroll state. It will be cleared when
        // the scroll is completed.
        is MessagesData -> {
            operation.requestScrollToMessageId ?: scrollToMessage
        }

        else -> scrollToMessage
    }

    private fun ConversationDetailState.toNewOpenAttachmentStateFrom(
        operation: ConversationDetailOperation
    ): Effect<OpenAttachmentIntentValues> = when (operation) {
        is OpenAttachmentEvent -> Effect.of(operation.values)
        else -> openAttachmentEffect
    }

    private fun ConversationDetailState.toNewOpenProtonCalendarIntentFrom(
        operation: ConversationDetailOperation
    ): Effect<OpenProtonCalendarIntentValues> = when (operation) {
        is HandleOpenProtonCalendarRequest -> Effect.of(operation.intent)
        else -> openProtonCalendarIntent
    }


    private fun ConversationDetailState.toNewDeleteDialogState(
        operation: ConversationDetailOperation
    ): ConversationDeleteState {
        return if (operation is AffectingDeleteDialog) {
            deleteDialogReducer.newStateFrom(operation)
        } else {
            conversationDeleteState
        }
    }

    private fun ConversationDetailState.toNewReportPhishingDialogState(
        operation: ConversationDetailOperation
    ): ReportPhishingDialogState {
        return if (operation is ConversationDetailOperation.AffectingReportPhishingDialog) {
            reportPhishingDialogReducer.newStateFrom(operation)
        } else {
            reportPhishingDialogState
        }
    }

    private fun ConversationDetailState.toNewTrashedMessagesBannerState(
        operation: ConversationDetailOperation
    ): TrashedMessagesBannerState {
        return if (operation is ConversationDetailOperation.AffectingTrashedMessagesBanner) {
            trashedMessagesBannerReducer.newStateFrom(operation)
        } else {
            trashedMessagesBannerState
        }
    }

    private fun ConversationDetailState.toNewMarkAsLegitimateDialogState(
        operation: ConversationDetailOperation
    ): MarkAsLegitimateDialogState {
        return if (operation is ConversationDetailOperation.AffectingMarkAsLegitimateDialog) {
            markAsLegitimateDialogReducer.newStateFrom(operation)
        } else {
            markAsLegitimateDialogState
        }
    }
}
