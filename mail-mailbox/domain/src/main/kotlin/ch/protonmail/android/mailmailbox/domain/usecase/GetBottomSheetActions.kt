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

package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.usecase.GetAllConversationBottomBarActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetAllMessageBottomBarActions
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.maillabel.domain.model.ViewMode
import javax.inject.Inject

class GetBottomSheetActions @Inject constructor(
    private val getAllMessageBottomBarActions: GetAllMessageBottomBarActions,
    private val getAllConversationBottomBarActions: GetAllConversationBottomBarActions
) {

    suspend operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        mailboxItemIds: List<MailboxItemId>,
        viewMode: ViewMode
    ): Either<DataError, AllBottomBarActions> = when (viewMode) {
        ViewMode.ConversationGrouping -> {
            val conversationIds = mailboxItemIds.map { ConversationId(it.value) }
            getAllConversationBottomBarActions(userId, labelId, conversationIds).removeMoreAction()
        }

        ViewMode.NoConversationGrouping -> {
            val messageIds = mailboxItemIds.map { MessageId(it.value) }
            getAllMessageBottomBarActions(userId, labelId, messageIds).removeMoreAction()
        }
    }

    private fun Either<DataError, AllBottomBarActions>.removeMoreAction() = this.map {
        it.copy(
            hiddenActions = it.hiddenActions.removeMoreAction(),
            visibleActions = it.visibleActions.removeMoreAction()
        )
    }

    private fun List<Action>.removeMoreAction(): List<Action> {
        val mutableActions = this.toMutableList()
        mutableActions.remove(Action.More)
        return mutableActions
    }

}
