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
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ToolbarActionUiModel
import javax.inject.Inject

class CustomizeToolbarEditActionsMapper @Inject constructor(
    private val actionUiModelMapper: ActionUiModelMapper
) {

    fun toTopbarUiModel(type: ToolbarType): TextUiModel {
        val toolbarRes = when (type) {
            ToolbarType.List -> R.string.mail_settings_custom_toolbar_nav_title_list
            ToolbarType.Message -> R.string.mail_settings_custom_toolbar_nav_title_message
            ToolbarType.Conversation -> R.string.mail_settings_custom_toolbar_nav_title_convo
        }

        return TextUiModel(toolbarRes)
    }

    fun toUiModel(toolbarAction: Action, enabled: Boolean): ToolbarActionUiModel = with(toolbarAction) {
        val action = actionUiModelMapper.toUiModel(toolbarAction)

        return ToolbarActionUiModel(
            action = action,
            enabled = enabled
        )
    }
}
