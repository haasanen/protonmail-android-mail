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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.reducer

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.mapper.CustomizeToolbarEditActionsMapper
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditState
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.SaveEvent
import javax.inject.Inject
import ch.protonmail.android.mailsettings.presentation.R as presentationR

internal class CustomizeToolbarEditActionsReducer @Inject constructor(
    private val mapper: CustomizeToolbarEditActionsMapper
) {

    fun toNewState(
        selection: ToolbarActionsPreference.ActionSelection,
        type: ToolbarType,
        saveEvent: SaveEvent
    ) = with(selection) {
        val selectedEnabled = canRemove()
        val remainingEnabled = canAddMore()
        val recognizedSelected = selected
        val remaining = all.filterNot { recognizedSelected.contains(it) }

        CustomizeToolbarEditState.Data(
            toolbarTitle = mapper.toTopbarUiModel(type),
            disclaimer = TextUiModel.TextRes(presentationR.string.mail_settings_custom_toolbar_header),
            selectedActions = recognizedSelected.map { mapper.toUiModel(it, enabled = selectedEnabled) },
            remainingActions = remaining.map { mapper.toUiModel(it, enabled = remainingEnabled) },
            close = if (saveEvent is SaveEvent.Success) Effect.of(Unit) else Effect.empty(),
            error = if (saveEvent is SaveEvent.Error) Effect.of(Unit) else Effect.empty()
        )
    }
}
