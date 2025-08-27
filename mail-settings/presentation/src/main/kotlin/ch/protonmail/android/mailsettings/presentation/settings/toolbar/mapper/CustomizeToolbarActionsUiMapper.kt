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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.mapper

import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ToolbarActionsUiModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.ToolbarActionsSet
import me.proton.core.mailsettings.domain.entity.ViewMode
import javax.inject.Inject

internal class CustomizeToolbarActionsUiMapper @Inject constructor(
    private val actionUiMapper: ActionUiModelMapper
) {

    fun mapToList(actionsSet: ToolbarActionsSet, viewMode: ViewMode) = buildList {
        add(mapToType(actionsSet.list, ToolbarType.List))

        when (viewMode) {
            ViewMode.ConversationGrouping -> add(mapToType(actionsSet.conversation, ToolbarType.Conversation))
            ViewMode.NoConversationGrouping -> add(mapToType(actionsSet.messages, ToolbarType.Message))
        }
    }

    private fun mapToType(actions: List<Action>, type: ToolbarType): ToolbarActionsUiModel {
        val (titleRes, subtitleRes) = when (type) {
            ToolbarType.Conversation -> Pair(
                R.string.mail_settings_custom_toolbar_conversation_view_title,
                R.string.mail_settings_custom_toolbar_conversation_view_description
            )

            ToolbarType.List -> Pair(
                R.string.mail_settings_custom_toolbar_list_view_title,
                R.string.mail_settings_custom_toolbar_list_view_description
            )

            ToolbarType.Message -> Pair(
                R.string.mail_settings_custom_toolbar_message_view_title,
                R.string.mail_settings_custom_toolbar_message_view_description
            )
        }

        return ToolbarActionsUiModel(
            headerText = TextUiModel.TextRes(titleRes),
            descriptionText = TextUiModel.TextRes(subtitleRes),
            type = type,
            actions = actions.map { actionUiMapper.toUiModel(it) }
        )
    }
}
