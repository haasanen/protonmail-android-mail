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

package ch.protonmail.android.mailconversation.data.mapper

import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailmessage.data.mapper.generalActionsToActions
import ch.protonmail.android.mailmessage.data.mapper.systemFolderActionsToActions
import timber.log.Timber
import uniffi.proton_mail_uniffi.ConversationAction
import uniffi.proton_mail_uniffi.ConversationAvailableActions


fun ConversationAvailableActions.toAvailableActions(): AvailableActions {
    return AvailableActions(
        emptyList(),
        this.conversationActions.conversationActionsToActions().filterNotNull(),
        this.moveActions.systemFolderActionsToActions(),
        this.generalActions.generalActionsToActions().filterNotNull()
    )
}

private fun List<ConversationAction>.conversationActionsToActions() = this.map { messageAction ->
    when (messageAction) {
        ConversationAction.STAR -> Action.Star
        ConversationAction.UNSTAR -> Action.Unstar
        ConversationAction.LABEL_AS -> Action.Label
        ConversationAction.MARK_READ -> Action.MarkRead
        ConversationAction.MARK_UNREAD -> Action.MarkUnread
        ConversationAction.DELETE -> Action.Delete
        ConversationAction.PIN,
        ConversationAction.UNPIN -> {
            Timber.i("rust-message: Found unhandled action while mapping: $messageAction")
            null
        }
    }
}
