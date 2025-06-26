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

package ch.protonmail.android.mailmailbox.presentation.mailbox.model

import ch.protonmail.android.mailattachments.domain.model.OpenAttachmentIntentValues
import ch.protonmail.android.mailattachments.presentation.model.AttachmentIdUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetOperation
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsItemId
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToItemId
import ch.protonmail.android.mailmailbox.domain.model.SpamOrTrash
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingActionMessage
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingBottomAppBar
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingBottomSheet
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingDeleteDialog
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingClearAllDialog
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingErrorBar
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingMailboxList
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingTopAppBar
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation.AffectingUnreadFilter
import ch.protonmail.android.mailmessage.domain.model.AvatarImageStates
import me.proton.android.core.accountmanager.domain.model.CoreAccountAvatarItem
import ch.protonmail.android.maillabel.domain.model.ViewMode
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent

internal sealed interface MailboxOperation {
    sealed interface AffectingTopAppBar
    sealed interface AffectingUnreadFilter
    sealed interface AffectingMailboxList
    sealed interface AffectingBottomAppBar
    sealed interface AffectingActionMessage
    sealed interface AffectingDeleteDialog
    sealed interface AffectingClearAllDialog
    sealed interface AffectingBottomSheet
    sealed interface AffectingErrorBar
}

internal sealed interface MailboxViewAction : MailboxOperation {

    data class OnItemLongClicked(
        val item: MailboxItemUiModel
    ) : MailboxViewAction

    data class OnItemAvatarClicked(
        val item: MailboxItemUiModel
    ) : MailboxViewAction

    data class OnAvatarImageLoadRequested(
        val item: MailboxItemUiModel
    ) : MailboxViewAction

    data class OnAvatarImageLoadFailed(
        val item: MailboxItemUiModel
    ) : MailboxViewAction

    data class MailboxItemsChanged(
        val itemIds: List<String>
    ) : MailboxViewAction

    object EnterSearchMode : MailboxViewAction, AffectingTopAppBar, AffectingMailboxList
    data class SearchQuery(val query: String) : MailboxViewAction, AffectingMailboxList
    object SearchResult : MailboxViewAction, AffectingMailboxList
    object ExitSearchMode : MailboxViewAction, AffectingTopAppBar, AffectingMailboxList, AffectingBottomAppBar

    object ExitSelectionMode : MailboxViewAction, AffectingTopAppBar, AffectingMailboxList, AffectingBottomAppBar

    data class ItemClicked(val item: MailboxItemUiModel) : MailboxViewAction

    object Refresh : MailboxViewAction, AffectingMailboxList
    object RefreshCompleted : MailboxViewAction, AffectingMailboxList
    object EnableUnreadFilter : MailboxViewAction, AffectingUnreadFilter
    object DisableUnreadFilter : MailboxViewAction, AffectingUnreadFilter

    object MarkAsRead : MailboxViewAction, AffectingMailboxList, AffectingBottomSheet
    object MarkAsUnread : MailboxViewAction, AffectingMailboxList, AffectingBottomSheet
    object Trash : MailboxViewAction
    object Delete : MailboxViewAction
    object DeleteConfirmed : MailboxViewAction
    object DeleteDialogDismissed : MailboxViewAction, AffectingDeleteDialog
    object RequestLabelAsBottomSheet : MailboxViewAction, AffectingBottomSheet
    data class SwipeReadAction(val itemId: String, val isRead: Boolean) : MailboxViewAction
    data class SwipeArchiveAction(val itemId: String) : MailboxViewAction, AffectingActionMessage
    data class SwipeSpamAction(val itemId: String) : MailboxViewAction, AffectingActionMessage
    data class SwipeTrashAction(val itemId: String) : MailboxViewAction, AffectingActionMessage
    data class StarAction(val itemId: String, val isStarred: Boolean) : MailboxViewAction
    data class SwipeLabelAsAction(val itemId: LabelAsItemId) : MailboxViewAction, AffectingBottomSheet
    data class SwipeMoveToAction(val itemId: MoveToItemId) : MailboxViewAction, AffectingBottomSheet

    object RequestMoveToBottomSheet : MailboxViewAction, AffectingBottomSheet

    object RequestMoreActionsBottomSheet : MailboxViewAction, AffectingBottomSheet
    object RequestManageAccountsBottomSheet : MailboxViewAction, AffectingBottomSheet
    object Star : MailboxViewAction, AffectingMailboxList, AffectingBottomSheet
    object UnStar : MailboxViewAction, AffectingMailboxList, AffectingBottomSheet
    object MoveToArchive :
        MailboxViewAction,
        AffectingTopAppBar,
        AffectingBottomAppBar,
        AffectingMailboxList,
        AffectingBottomSheet

    object MoveToSpam :
        MailboxViewAction,
        AffectingTopAppBar,
        AffectingBottomAppBar,
        AffectingMailboxList,
        AffectingBottomSheet

    object MoveToInbox :
        MailboxViewAction,
        AffectingTopAppBar,
        AffectingBottomAppBar,
        AffectingMailboxList,
        AffectingBottomSheet

    object DismissBottomSheet : MailboxViewAction, AffectingBottomSheet

    /*
     *`OnOfflineWithData` and `OnErrorWithData` are not actual Actions which are actively performed by the user
     * but rather "Events" which happen when loading mailbox items. They are represented as actions due to
     * limitations of the paging library, which delivers such events on the Composable. See commit 7c3f88 for more.
     */
    object OnOfflineWithData : MailboxViewAction, AffectingMailboxList
    object OnErrorWithData : MailboxViewAction, AffectingMailboxList
    object NavigateToInboxLabel : MailboxViewAction
    object RequestUpsellingBottomSheet : MailboxViewAction, AffectingBottomSheet
    data class SelectAll(val allItems: List<MailboxItemUiModel>) : MailboxViewAction
    data object DeselectAll : MailboxViewAction
    object CustomizeToolbar : MailboxViewAction

    data class RequestAttachment(val attachmentId: AttachmentIdUiModel) : MailboxViewAction

    data object ClearAll : MailboxViewAction
    data object ClearAllConfirmed : MailboxViewAction, AffectingClearAllDialog
    data object ClearAllDismissed : MailboxViewAction, AffectingClearAllDialog
}

internal sealed interface MailboxEvent : MailboxOperation {

    object MoveToConfirmed :
        MailboxEvent,
        AffectingTopAppBar,
        AffectingBottomAppBar,
        AffectingMailboxList,
        AffectingBottomSheet

    data class AvatarImageStatesUpdated(
        val avatarImageStates: AvatarImageStates
    ) : MailboxEvent, AffectingMailboxList

    data class NewLabelSelected(
        val selectedLabel: MailLabel,
        val selectedLabelCount: Int?
    ) : MailboxEvent, AffectingTopAppBar, AffectingUnreadFilter, AffectingMailboxList

    data class SelectedLabelChanged(
        val selectedLabel: MailLabel
    ) : MailboxEvent, AffectingTopAppBar, AffectingMailboxList

    data class SelectedLabelCountChanged(
        val selectedLabelCount: Int
    ) : MailboxEvent, AffectingUnreadFilter

    data class EnterSelectionMode(
        val item: MailboxItemUiModel
    ) : MailboxEvent, AffectingTopAppBar, AffectingMailboxList, AffectingBottomAppBar

    data class SwipeActionsChanged(
        val swipeActionsPreference: SwipeActionsUiModel
    ) : MailboxEvent, AffectingMailboxList

    data class Trash(val numAffectedMessages: Int) :
        MailboxEvent,
        AffectingMailboxList,
        AffectingTopAppBar,
        AffectingBottomAppBar,
        AffectingActionMessage,
        AffectingBottomSheet

    data class Delete(val viewMode: ViewMode, val numAffectedMessages: Int) : MailboxEvent, AffectingDeleteDialog
    data class DeleteConfirmed(val viewMode: ViewMode, val numAffectedMessages: Int) :
        MailboxEvent,
        AffectingMailboxList,
        AffectingTopAppBar,
        AffectingBottomAppBar,
        AffectingActionMessage,
        AffectingDeleteDialog,
        AffectingBottomSheet

    data class PrimaryAccountAvatarChanged(val item: CoreAccountAvatarItem?) : MailboxEvent, AffectingTopAppBar

    data class PaginatorInvalidated(val event: PageInvalidationEvent) : MailboxEvent, AffectingMailboxList

    sealed interface ItemClicked : MailboxEvent {

        val item: MailboxItemUiModel

        data class ItemDetailsOpened(
            override val item: MailboxItemUiModel,
            val contextLabel: LabelId
        ) : ItemClicked, AffectingMailboxList

        data class OpenComposer(
            override val item: MailboxItemUiModel
        ) : ItemClicked, AffectingMailboxList

        data class ItemAddedToSelection(
            override val item: MailboxItemUiModel
        ) : ItemClicked, AffectingMailboxList, AffectingTopAppBar

        data class ItemRemovedFromSelection(
            override val item: MailboxItemUiModel
        ) : ItemClicked, AffectingMailboxList, AffectingTopAppBar
    }

    data class ItemsRemovedFromSelection(
        val itemIds: List<String>
    ) : MailboxEvent, AffectingMailboxList, AffectingTopAppBar

    data class AllItemsSelected(
        val allItems: List<MailboxItemUiModel>
    ) : MailboxEvent, AffectingMailboxList, AffectingTopAppBar

    data object AllItemsDeselected :
        MailboxEvent, AffectingMailboxList, AffectingTopAppBar

    data class MessageBottomBarEvent(
        val bottomBarEvent: BottomBarEvent
    ) : MailboxEvent, AffectingBottomAppBar

    data class MailboxBottomSheetEvent(
        val bottomSheetOperation: BottomSheetOperation
    ) : MailboxEvent, AffectingBottomSheet

    object ErrorDeleting : MailboxEvent, AffectingErrorBar, AffectingDeleteDialog, AffectingBottomSheet
    object ErrorLabeling : MailboxEvent, AffectingErrorBar
    object ErrorRetrievingCustomMailLabels : MailboxEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorRetrievingFolderColorSettings : MailboxEvent, AffectingErrorBar, AffectingBottomSheet
    object ErrorMoving : MailboxEvent, AffectingErrorBar
    object ErrorRetrievingDestinationMailFolders : MailboxEvent, AffectingErrorBar, AffectingBottomSheet

    data object AttachmentDownloadOngoingEvent : MailboxEvent, AffectingMailboxList
    data object AttachmentErrorEvent : MailboxEvent, AffectingMailboxList
    data class AttachmentReadyEvent(
        val openAttachmentIntentValues: OpenAttachmentIntentValues
    ) : MailboxEvent, AffectingMailboxList

    data class ClearAll(val spamOrTrash: SpamOrTrash) : MailboxEvent, AffectingClearAllDialog
}


