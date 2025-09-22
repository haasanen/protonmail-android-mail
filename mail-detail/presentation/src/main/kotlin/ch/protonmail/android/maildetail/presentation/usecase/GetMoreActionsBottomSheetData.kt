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

package ch.protonmail.android.maildetail.presentation.usecase

import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailconversation.domain.entity.ConversationDetailEntryPoint
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationAvailableActions
import ch.protonmail.android.mailconversation.domain.usecase.ObserveConversation
import ch.protonmail.android.maildetail.presentation.model.MoreActionsBottomSheetEntryPoint
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageAvailableActions
import ch.protonmail.android.mailmessage.domain.usecase.ObserveMessage
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetMoreActionsBottomSheetData @Inject constructor(
    private val getMessageAvailableActions: GetMessageAvailableActions,
    private val getConversationAvailableActions: GetConversationAvailableActions,
    private val observeMessage: ObserveMessage,
    private val observeConversation: ObserveConversation
) {

    suspend fun forMessage(
        userId: UserId,
        labelId: LabelId,
        messageId: MessageId,
        messageThemeOptions: MessageThemeOptions,
        entryPoint: MoreActionsBottomSheetEntryPoint
    ): DetailMoreActionsBottomSheetEvent.DataLoaded? = getMessageAvailableActions(
        userId, labelId, messageId, messageThemeOptions
    ).map { availableActions ->
        val message = observeMessage(userId, messageId).firstOrNull()?.getOrNull()
            ?: return null

        val requestIsFromBottomBar = entryPoint is MoreActionsBottomSheetEntryPoint.BottomBar

        return DetailMoreActionsBottomSheetEvent.DataLoaded(
            messageSender = message.sender.name,
            messageSubject = message.subject,
            messageIdInConversation = message.messageId.id,
            availableActions = availableActions,
            customizeToolbarAction = if (requestIsFromBottomBar) Action.CustomizeToolbar else null
        )
    }.getOrNull()

    suspend fun forConversation(
        userId: UserId,
        labelId: LabelId,
        conversationId: ConversationId,
        entryPoint: ConversationDetailEntryPoint
    ): DetailMoreActionsBottomSheetEvent.DataLoaded? =
        getConversationAvailableActions(userId, labelId, conversationId).map { availableActions ->
            val conversation = observeConversation(userId, conversationId, labelId, entryPoint)
                .firstOrNull()
                ?.getOrNull()
                ?: return null

            return DetailMoreActionsBottomSheetEvent.DataLoaded(
                messageSender = conversation.senders.first().name,
                messageSubject = conversation.subject,
                messageIdInConversation = null,
                availableActions = availableActions,
                customizeToolbarAction = Action.CustomizeToolbar
            )
        }.getOrNull()

}

