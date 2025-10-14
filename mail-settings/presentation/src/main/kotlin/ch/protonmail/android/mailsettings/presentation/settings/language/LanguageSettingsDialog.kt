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

package ch.protonmail.android.mailsettings.presentation.settings.language

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.compose.PickerDialog
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.language.LanguageSettingsState.Data
import ch.protonmail.android.mailsettings.presentation.settings.language.LanguageSettingsState.Loading

const val TEST_TAG_LANGUAGE_SETTINGS_DIALOG = "LanguageSettingsScreenTestTag"
const val TEST_TAG_LANG_SETTINGS_SCREEN_SCROLL_COL = "LanguageSettingsScreenColumnTestTag"

@Composable
fun LanguageSettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: LanguageSettingsViewModel = hiltViewModel()
) {
    val effects = viewModel.effects.collectAsStateWithLifecycle().value
    ConsumableLaunchedEffect(effects.close) {
        onDismiss()
    }
    when (
        val state = viewModel.state.collectAsStateWithLifecycle(
            Loading
        ).value
    ) {
        is Data -> {
            LanguageSettingsDialog(
                modifier = modifier,
                onDismiss = onDismiss,
                onLanguageSelected = { viewModel.onLanguageSelected(state.languageFor(it)) },
                selectedLanguage = state.selectedLanguageUiModel,
                languageChoices = state.languageChoiceUiTextModels
            )
        }

        is Loading -> Unit
    }
}

@Composable
private fun LanguageSettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onLanguageSelected: (TextUiModel) -> Unit,
    selectedLanguage: TextUiModel,
    languageChoices: List<TextUiModel>
) {
    PickerDialog(
        title = stringResource(R.string.mail_settings_app_language),
        selectedValue = selectedLanguage,
        values = languageChoices,
        onDismissRequest = onDismiss,
        onValueSelected = onLanguageSelected,
        modifier = modifier.testTag(TEST_TAG_LANGUAGE_SETTINGS_DIALOG)
    )
}

@Preview(name = "Language settings screen")
@Composable
private fun PreviewThemeSettingDialog() {
    LanguageSettingsDialog(
        onDismiss = {},
        onLanguageSelected = {},
        selectedLanguage = TextUiModel("English"),
        languageChoices = listOf(
            TextUiModel("English"),
            TextUiModel("Français"),
            TextUiModel("Italiano")
        )
    )
}
