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

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.ui.delete.DeleteDialogState
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDeleteState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation.AffectingDeleteDialog
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import javax.inject.Inject

class ConversationDeleteDialogReducer @Inject constructor() {

    internal fun newStateFrom(operation: AffectingDeleteDialog) = when (operation) {
        is ConversationDetailViewAction.DeleteRequested -> newStateFromDeleteRequested()
        is ConversationDetailViewAction.DeleteMessageRequested ->
            newStateFromDeleteMessageRequested(operation.messageId)
        is ConversationDetailEvent.ErrorDeletingMessage,
        is ConversationDetailEvent.ErrorDeletingConversation,
        is ConversationDetailViewAction.DeleteConfirmed,
        is ConversationDetailViewAction.DeleteMessageConfirmed,
        is ConversationDetailViewAction.DeleteDialogDismissed -> ConversationDeleteState.Hidden
    }

    private fun newStateFromDeleteRequested(): ConversationDeleteState = ConversationDeleteState(
        DeleteDialogState.Shown(
            title = TextUiModel.TextRes(R.string.conversation_delete_dialog_title),
            message = TextUiModel.TextRes(R.string.conversation_delete_dialog_message)
        )
    )

    private fun newStateFromDeleteMessageRequested(messageId: MessageId): ConversationDeleteState =
        ConversationDeleteState(
            deleteDialogState = DeleteDialogState.Shown(
                title = TextUiModel.TextRes(R.string.message_delete_dialog_title),
                message = TextUiModel.TextRes(R.string.message_delete_dialog_message)
            ),
            messageIdInConversation = messageId
        )
}
