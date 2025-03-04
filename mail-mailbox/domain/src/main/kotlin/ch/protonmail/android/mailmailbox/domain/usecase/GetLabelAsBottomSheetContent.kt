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
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationLabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.LabelAsItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageLabelAsActions
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ViewMode
import javax.inject.Inject

class GetLabelAsBottomSheetContent @Inject constructor(
    private val getMessageLabelAsActions: GetMessageLabelAsActions,
    private val getConversationLabelAsActions: GetConversationLabelAsActions
) {

    suspend operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        labelAsItemIds: List<LabelAsItemId>,
        viewMode: ViewMode
    ): Either<DataError, LabelAsActions> = when (viewMode) {
        ViewMode.ConversationGrouping -> {
            val conversationIds = labelAsItemIds.map { ConversationId(it.value) }
            getConversationLabelAsActions(userId, labelId, conversationIds)
        }

        ViewMode.NoConversationGrouping -> {
            val messageIds = labelAsItemIds.map { MessageId(it.value) }
            getMessageLabelAsActions(userId, labelId, messageIds)
        }
    }

}
