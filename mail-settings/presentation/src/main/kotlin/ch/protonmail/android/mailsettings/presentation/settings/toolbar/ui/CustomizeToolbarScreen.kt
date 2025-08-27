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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonSettingsDetailsAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.CustomizeToolbarViewModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.ToolbarActionsUiModel
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarState
import me.proton.core.presentation.utils.showToast

@Composable
fun CustomizeToolbarScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onCustomize: (ToolbarType) -> Unit
) {
    val viewModel = hiltViewModel<CustomizeToolbarViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = CustomizeToolbarScreen.Actions.Empty.copy(onBack, onCustomize)

    CustomizeToolbarScreen(
        state,
        actions,
        modifier
    )
}

@Composable
private fun CustomizeToolbarScreen(
    state: CustomizeToolbarState,
    actions: CustomizeToolbarScreen.Actions,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ProtonTheme.colors.backgroundSecondary,
            topBar = {
                ProtonSettingsDetailsAppBar(
                    title = stringResource(R.string.mail_settings_custom_toolbar_title),
                    onBackClick = actions.onBack
                )
            }
        ) { contentPadding ->
            when (state) {
                CustomizeToolbarState.Loading -> ProtonCenteredProgress()
                is CustomizeToolbarState.Data -> {
                    Box(
                        modifier = Modifier
                            .padding(contentPadding)
                            .padding(top = ProtonDimens.Spacing.Large)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CustomizeToolbarScreenContent(state = state, actions = actions)
                    }
                }

                CustomizeToolbarState.Error -> CustomizeToolbarScreenError(actions.onBack)
            }
        }
    }
}

@Composable
private fun CustomizeToolbarScreenContent(
    state: CustomizeToolbarState.Data,
    actions: CustomizeToolbarScreen.Actions,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = ProtonDimens.Spacing.Large)
            .padding(horizontal = ProtonDimens.Spacing.Large),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        items(state.actions) {
            ToolbarSection(
                title = it.headerText.string(),
                description = it.descriptionText.string(),
                actions = it.actions,
                onClick = { actions.onCustomize(it.type) }
            )
        }
    }
}

@Composable
private fun CustomizeToolbarScreenError(onBack: () -> Unit) {
    val context = LocalContext.current

    val errorMessage = stringResource(R.string.mail_settings_custom_toolbar_read_error)
    LaunchedEffect(Unit) {
        context.showToast(errorMessage)
    }

    onBack()
}

object CustomizeToolbarScreen {
    data class Actions(
        val onBack: () -> Unit,
        val onCustomize: (toolbarType: ToolbarType) -> Unit
    ) {

        companion object {

            val Empty = Actions(onBack = {}, onCustomize = {})
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
private fun CustomizeToolbarScreenPreview() {
    val actions = listOf(
        ToolbarActionsUiModel(
            headerText = TextUiModel.Text("Item 1"),
            descriptionText = TextUiModel.Text("Description 1"),
            actions = listOf(
                ActionUiModel(action = Action.Move),
                ActionUiModel(action = Action.Label),
                ActionUiModel(action = Action.Trash),
                ActionUiModel(action = Action.ReportPhishing)
            ),
            type = ToolbarType.List
        ),
        ToolbarActionsUiModel(
            headerText = TextUiModel.Text("Item 2"),
            descriptionText = TextUiModel.Text("Description 2"),
            actions = listOf(
                ActionUiModel(action = Action.MarkRead),
                ActionUiModel(action = Action.Snooze),
                ActionUiModel(action = Action.Archive),
                ActionUiModel(action = Action.SaveAttachments)
            ),
            type = ToolbarType.Conversation
        ),
        ToolbarActionsUiModel(
            headerText = TextUiModel.Text("Item 3"),
            descriptionText = TextUiModel.Text("Description 3"),
            actions = listOf(
                ActionUiModel(action = Action.Move),
                ActionUiModel(action = Action.Label),
                ActionUiModel(action = Action.Trash),
                ActionUiModel(action = Action.ReportPhishing)
            ),
            type = ToolbarType.Message
        )
    )

    val state = CustomizeToolbarState.Data(actions)

    ProtonTheme {
        CustomizeToolbarScreen(state, CustomizeToolbarScreen.Actions.Empty)
    }
}
