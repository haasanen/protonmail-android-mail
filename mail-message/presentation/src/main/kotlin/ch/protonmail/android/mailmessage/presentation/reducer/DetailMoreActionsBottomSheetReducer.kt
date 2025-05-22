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

package ch.protonmail.android.mailmessage.presentation.reducer

import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetState
import ch.protonmail.android.mailmessage.presentation.mapper.DetailMoreActionsBottomSheetUiMapper
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class DetailMoreActionsBottomSheetReducer @Inject constructor(
    private val mapper: DetailMoreActionsBottomSheetUiMapper,
    private val actionsUiModelMapper: ActionUiModelMapper
) {

    fun newStateFrom(
        currentState: BottomSheetState?,
        operation: DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetOperation
    ): BottomSheetState {
        return when (operation) {
            is DataLoaded -> operation.toNewBottomSheetState(currentState)
        }
    }

    private fun DataLoaded.toNewBottomSheetState(currentState: BottomSheetState?): BottomSheetState {
        val headerUiModel = mapper.toHeaderUiModel(messageSubject, messageIdInConversation)
        val replyActions = availableActions.replyActions.toActionUiModels()
        val messageActions = availableActions.mailboxItemActions.toActionUiModels()
        val moveActions = availableActions.moveActions.toActionUiModels()
        val genericActions = availableActions.genericActions.toActionUiModels()
        val customizeToolbarActionUiModel = customizeToolbarAction?.let { actionsUiModelMapper.toUiModel(it) }

        return BottomSheetState(
            contentState = DetailMoreActionsBottomSheetState.Data(
                detailDataUiModel = headerUiModel,
                replyActions = replyActions,
                messageActions = messageActions,
                moveActions = moveActions,
                genericActions = genericActions,
                customizeToolbarActionUiModel = customizeToolbarActionUiModel
            ),
            bottomSheetVisibilityEffect = currentState?.bottomSheetVisibilityEffect ?: Effect.empty()
        )
    }

    private fun List<Action>.toActionUiModels() = this.map { actionsUiModelMapper.toUiModel(it) }.toImmutableList()

}
