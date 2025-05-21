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

package ch.protonmail.android.mailsettings.presentation.settings.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.compose.PickerDialog
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.theme.ThemeSettingsState.Data
import ch.protonmail.android.mailsettings.presentation.settings.theme.ThemeSettingsState.Loading

const val TEST_TAG_THEME_SETTINGS_DIALOG = "ThemeSettingsDialogTestTag"

@Composable
fun ThemeSettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    when (
        val state = viewModel.state.collectAsStateWithLifecycle(Loading).value
    ) {
        is Data -> {
            ThemeSettingsDialog(
                modifier = modifier,
                onDismiss = onDismiss,
                onThemeSelected = { viewModel.onThemeSelected(state.themeFor(it)) },
                selectedTheme = state.selectedTheme,
                themeChoices = state.themeChoices
            )
        }

        is Loading -> Unit
    }
}

@Composable
fun ThemeSettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onThemeSelected: (TextUiModel) -> Unit,
    selectedTheme: TextUiModel,
    themeChoices: List<TextUiModel>
) {
    PickerDialog(
        title = stringResource(R.string.mail_settings_app_customization_appearance),
        selectedValue = selectedTheme,
        values = themeChoices,
        onDismissRequest = onDismiss,
        onValueSelected = onThemeSelected,
        modifier = modifier.testTag(TEST_TAG_THEME_SETTINGS_DIALOG)
    )
}

@Preview(name = "Theme settings screen")
@Composable
fun PreviewThemeSettingDialog() {
    ThemeSettingsDialog(
        onDismiss = {},
        onThemeSelected = {},
        selectedTheme = TextUiModel(R.string.mail_settings_system_default),
        themeChoices = listOf(
            TextUiModel(R.string.mail_settings_system_default),
            TextUiModel(R.string.mail_settings_theme_light),
            TextUiModel(R.string.mail_settings_theme_dark)
        )
    )
}
