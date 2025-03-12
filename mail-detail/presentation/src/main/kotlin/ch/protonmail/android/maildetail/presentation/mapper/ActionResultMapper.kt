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

package ch.protonmail.android.maildetail.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.ActionResult
import ch.protonmail.android.mailcommon.presentation.model.ActionResult.UndoableActionResult
import ch.protonmail.android.mailcommon.presentation.model.ActionResult.DefinitiveActionResult
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import javax.inject.Inject

class ActionResultMapper @Inject constructor() {

    fun toActionResult(operation: ConversationDetailOperation): ActionResult? = when (operation) {
        is ConversationDetailViewAction.Archive ->
            UndoableActionResult(TextUiModel(R.string.conversation_moved_to_archive))

        is ConversationDetailViewAction.MoveToSpam ->
            UndoableActionResult(TextUiModel(R.string.conversation_moved_to_spam))

        is ConversationDetailViewAction.MoveToTrash ->
            UndoableActionResult(TextUiModel(R.string.conversation_moved_to_trash))

        is ConversationDetailEvent.MoveToDestinationConfirmed ->
            UndoableActionResult(
                TextUiModel(R.string.conversation_moved_to_selected_destination, operation.mailLabelText)
            )

        is ConversationDetailViewAction.LabelAsConfirmed ->
            DefinitiveActionResult(TextUiModel(R.string.conversation_moved_to_archive))

        is ConversationDetailViewAction.DeleteConfirmed ->
            DefinitiveActionResult(TextUiModel(R.string.conversation_deleted))

        else -> null // No specific ActionResult for other operations
    }
}
