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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ToolbarActionUiModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarEditState

internal object CustomizeToolbarPreviewData {

    val ActionEnabled = action()
    val ActionDisabled = action().copy(enabled = false)

    private val SelectedActions = (0..3).map { action() }
    private val UnselectedActions = (10..13).map { action() }

    private val Page = CustomizeToolbarEditState.Data(
        TextUiModel.Text("Edit message toolbar"),
        TextUiModel.Text("This toolbar is visible when reading a message."),
        SelectedActions, UnselectedActions
    )
    private val PageDisabledAdd = CustomizeToolbarEditState.Data(
        TextUiModel.Text("Edit message toolbar"),
        TextUiModel.Text("This toolbar is visible when reading a message."),
        SelectedActions,
        UnselectedActions.map {
            it.copy(enabled = false)
        }
    )
    private val PageDisabledRemove = CustomizeToolbarEditState.Data(
        TextUiModel.Text("Edit message toolbar"),
        TextUiModel.Text("This toolbar is visible when reading a message."),
        SelectedActions.map { it.copy(enabled = false) },
        UnselectedActions
    )

    val Normal = CustomizeToolbarEditState.Data(
        TextUiModel.Text("Edit message toolbar"),
        TextUiModel.Text("This toolbar is visible when reading a message."),
        Page.selectedActions,
        Page.remainingActions
    )
    val DisabledAdd = CustomizeToolbarEditState.Data(
        TextUiModel.Text("Edit message toolbar"),
        TextUiModel.Text("This toolbar is visible when reading a message."),
        PageDisabledAdd.selectedActions,
        PageDisabledAdd.remainingActions
    )
    val DisabledARemove = CustomizeToolbarEditState.Data(
        TextUiModel.Text("Edit message toolbar"),
        TextUiModel.Text("This toolbar is visible when reading a message."),
        PageDisabledRemove.selectedActions,
        PageDisabledRemove.remainingActions
    )

    fun action() = ToolbarActionUiModel(
        ActionUiModel(Action.Reply),
        enabled = true
    )
}

internal data class CustomizeToolbarPreview(
    val uiModel: CustomizeToolbarEditState
)

internal data class ToolbarActionPreview(
    val uiModel: ToolbarActionUiModel
)

internal class CustomizeToolbarPreviewProvider : PreviewParameterProvider<CustomizeToolbarPreview> {

    override val values = sequenceOf(
        CustomizeToolbarPreview(CustomizeToolbarEditState.Error),
        CustomizeToolbarPreview(CustomizeToolbarEditState.Loading),
        CustomizeToolbarPreview(CustomizeToolbarPreviewData.Normal),
        CustomizeToolbarPreview(CustomizeToolbarPreviewData.DisabledAdd),
        CustomizeToolbarPreview(CustomizeToolbarPreviewData.DisabledARemove)
    )
}

internal class SelectedToolbarActionPreviewProvider : PreviewParameterProvider<ToolbarActionPreview> {

    override val values = sequenceOf(
        ToolbarActionPreview(CustomizeToolbarPreviewData.ActionEnabled),
        ToolbarActionPreview(CustomizeToolbarPreviewData.ActionDisabled)
    )
}
