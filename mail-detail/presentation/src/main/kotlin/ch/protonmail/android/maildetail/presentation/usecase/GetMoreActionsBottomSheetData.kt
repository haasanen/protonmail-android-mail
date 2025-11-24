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
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationAvailableActions
import ch.protonmail.android.maildetail.presentation.model.MoreActionsBottomSheetEntryPoint
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageAvailableActions
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetMoreActionsBottomSheetData @Inject constructor(
    private val getMessageAvailableActions: GetMessageAvailableActions,
    private val getConversationAvailableActions: GetConversationAvailableActions
) {

    internal suspend fun forMessage(
        payload: MoreMessageActionsBottomSheetDataPayload
    ): DetailMoreActionsBottomSheetEvent.DataLoaded? = getMessageAvailableActions(
        payload.userId, payload.labelId, payload.messageId, payload.messageThemeOptions
    ).map { availableActions ->

        val requestIsFromBottomBar = payload.entryPoint is MoreActionsBottomSheetEntryPoint.BottomBar

        return DetailMoreActionsBottomSheetEvent.DataLoaded(
            messageSubject = payload.subject,
            messageIdInConversation = payload.messageId.id,
            availableActions = availableActions,
            customizeToolbarAction = if (requestIsFromBottomBar) Action.CustomizeToolbar else null
        )
    }.getOrNull()

    internal suspend fun forConversation(
        payload: MoreConversationActionsBottomSheetDataPayload
    ): DetailMoreActionsBottomSheetEvent.DataLoaded? = getConversationAvailableActions(
        payload.userId,
        payload.labelId,
        payload.conversationId
    ).map { availableActions ->
        return DetailMoreActionsBottomSheetEvent.DataLoaded(
            messageSubject = payload.subject,
            messageIdInConversation = null,
            availableActions = availableActions,
            customizeToolbarAction = Action.CustomizeToolbar
        )
    }.getOrNull()
}

internal data class MoreMessageActionsBottomSheetDataPayload(
    val userId: UserId,
    val labelId: LabelId,
    val messageId: MessageId,
    val messageThemeOptions: MessageThemeOptions,
    val entryPoint: MoreActionsBottomSheetEntryPoint,
    val subject: String
)

internal data class MoreConversationActionsBottomSheetDataPayload(
    val userId: UserId,
    val labelId: LabelId,
    val conversationId: ConversationId,
    val subject: String
)
