/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.data.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.data.local.ToolbarActionSettingsDataSource
import ch.protonmail.android.mailsettings.data.mapper.toAction
import ch.protonmail.android.mailsettings.data.mapper.toLocalMobileAction
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.domain.repository.ToolbarActionsRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ToolbarActionsRepositoryImpl @Inject constructor(
    private val toolbarActionsDataSource: ToolbarActionSettingsDataSource
) : ToolbarActionsRepository {

    override suspend fun getToolbarActions(userId: UserId, type: ToolbarType): Either<DataError, List<Action>> {
        return when (type) {
            ToolbarType.List -> toolbarActionsDataSource.getListActions(userId)
            ToolbarType.Conversation -> toolbarActionsDataSource.getConversationActions(userId)
            ToolbarType.Message -> toolbarActionsDataSource.getMessageActions(userId)
        }.map { list ->
            list.map { localAction ->
                localAction.toAction()
            }
        }
    }

    override suspend fun getAllActions(type: ToolbarType): List<Action> {
        return when (type) {
            ToolbarType.List -> toolbarActionsDataSource.getAllListActions()
            ToolbarType.Conversation -> toolbarActionsDataSource.getAllConversationActions()
            ToolbarType.Message -> toolbarActionsDataSource.getAllMessageActions()
        }.map { localAction ->
            localAction.toAction()
        }
    }


    override suspend fun saveActions(
        userId: UserId,
        type: ToolbarType,
        actions: List<Action>
    ): Either<DataError, Unit> {
        val mappedActions = actions.mapNotNull { it.toLocalMobileAction() }
        return when (type) {
            ToolbarType.List -> toolbarActionsDataSource.updateListActions(userId, mappedActions)
            ToolbarType.Conversation -> toolbarActionsDataSource.updateConversationActions(userId, mappedActions)
            ToolbarType.Message -> toolbarActionsDataSource.updateMessageActions(userId, mappedActions)
        }
    }
}
