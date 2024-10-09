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

import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationLabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.toMailLabelCustom
import ch.protonmail.android.maillabel.presentation.toCustomUiModel
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageLabelAsActions
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetLabelAsBottomSheetData @Inject constructor(
    private val getMessageLabelAsActions: GetMessageLabelAsActions,
    private val getConversationLabelAsActions: GetConversationLabelAsActions
) {

    suspend fun forMessage(
        userId: UserId,
        labelId: LabelId,
        messageId: MessageId
    ): LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData =
        getMessageLabelAsActions(userId, labelId, listOf(messageId)).fold(
            ifLeft = { emptyBottomSheetData() },
            ifRight = { buildBottomSheetActionData(it) }
        ).copy(messageIdInConversation = messageId)

    suspend fun forConversation(
        userId: UserId,
        labelId: LabelId,
        conversationId: ConversationId
    ): LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData =
        getConversationLabelAsActions(userId, labelId, listOf(conversationId)).fold(
            ifLeft = { emptyBottomSheetData() },
            ifRight = { buildBottomSheetActionData(it) }
        )

    private fun emptyBottomSheetData() = LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
        persistentListOf(), persistentListOf(), persistentListOf()
    )

    private fun buildBottomSheetActionData(
        labelAsContent: LabelAsActions
    ): LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData {
        val mailLabels = labelAsContent.labels.toMailLabelCustom()
        return LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
            customLabelList = mailLabels.map { it.toCustomUiModel(emptyMap(), null) }
                .toImmutableList(),
            selectedLabels = labelAsContent.selected.toImmutableList(),
            partiallySelectedLabels = labelAsContent.partiallySelected.toImmutableList()
        )
    }
}
