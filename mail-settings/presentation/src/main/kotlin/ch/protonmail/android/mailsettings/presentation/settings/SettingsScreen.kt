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

package ch.protonmail.android.mailsettings.presentation.settings

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.R.string
import ch.protonmail.android.mailsettings.presentation.settings.SettingsState.Data
import ch.protonmail.android.mailsettings.presentation.settings.SettingsState.Loading
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.component.ProtonSettingsList
import me.proton.core.compose.component.ProtonSettingsTopBar
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun MainSettingsScreen(
    modifier: Modifier = Modifier,
    actions: MainSettingsScreen.Actions,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {

    when (val settingsState = rememberAsState(flow = settingsViewModel.state, Loading).value) {
        is Data -> MainSettingsScreen(
            modifier = modifier,
            state = settingsState,
            actions = actions
        )

        is Loading -> ProtonCenteredProgress(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun MainSettingsScreen(
    state: Data,
    actions: MainSettingsScreen.Actions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag(SettingsScreenTestTags.RootItem),
        topBar = {
            ProtonSettingsTopBar(
                title = stringResource(id = string.mail_settings_settings),
                onBackClick = actions.onBackClick
            )
        }
    ) { contentPadding ->
        ProtonSettingsList(
            modifier = modifier
                .testTag(SettingsScreenTestTags.SettingsList)
                .padding(contentPadding)
        ) {
            item { ProtonSettingsHeader(title = string.mail_settings_account_settings) }
            item {
                AccountSettingsItem(
                    modifier = Modifier.testTag(SettingsScreenTestTags.AccountSettingsItem),
                    accountInfo = state.account,
                    onAccountClicked = actions.onAccountClick
                )
            }
            item { ProtonSettingsHeader(title = string.mail_settings_preferences) }
            item {
                ProtonSettingsItem(
                    name = stringResource(id = string.mail_settings_email),
                    hint = stringResource(id = string.mail_settings_email_hint),
                    onClick = actions.onEmailSettingsClick
                )
                Divider()
            }
            item {
                ProtonSettingsItem(
                    name = stringResource(id = string.mail_settings_folders_labels),
                    hint = stringResource(id = string.mail_settings_folders_labels_hint),
                    onClick = actions.onAppSettingsClick
                )
                Divider()
            }
            item {
                ProtonSettingsItem(
                    name = stringResource(id = string.mail_settings_spam_and_custom_filters),
                    hint = stringResource(id = string.mail_settings_spam_and_custom_filters_hint),
                    onClick = actions.onAppSettingsClick
                )
                Divider()
            }
            item {
                ProtonSettingsItem(
                    name = stringResource(id = string.mail_settings_privacy_and_security),
                    hint = stringResource(id = string.mail_settings_privacy_and_security_hint),
                    onClick = actions.onAppSettingsClick
                )
                Divider()
            }
            item {
                ProtonSettingsItem(
                    name = stringResource(id = string.mail_settings_app),
                    hint = stringResource(id = string.mail_settings_app_hint),
                    onClick = actions.onAppSettingsClick
                )
                Divider()
            }
            item { ProtonSettingsHeader(title = string.mail_settings_app_information) }
            item {
                ProtonSettingsItem(
                    name = stringResource(id = string.mail_settings_app_version),
                    hint = "${state.appInformation.appVersionName} (${state.appInformation.appVersionCode})",
                    isClickable = false
                )
            }
        }
    }
}

@Composable
fun AccountSettingsItem(
    modifier: Modifier = Modifier,
    accountInfo: AccountInfo?,
    onAccountClicked: () -> Unit
) {
    val header = accountInfo?.name
        ?: stringResource(id = R.string.mail_settings_no_information_available)
    val hint = accountInfo?.email

    ProtonSettingsItem(
        modifier = modifier,
        name = header,
        hint = hint,
        onClick = onAccountClicked
    )
    Divider()
}

object MainSettingsScreen {

    data class Actions(
        val onAccountClick: () -> Unit,
        val onAppSettingsClick: () -> Unit,
        val onEmailSettingsClick: () -> Unit,
        val onBackClick: () -> Unit
    )
}

@Preview(
    name = "Main settings screen light mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "Main settings screen dark mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun PreviewMainSettingsScreen() {
    ProtonTheme {
        MainSettingsScreen(
            state = SettingsScreenPreviewData.Data,
            actions = SettingsScreenPreviewData.Actions
        )
    }
}

object SettingsScreenTestTags {
    const val RootItem = "SettingsScreenTestTag"
    const val SettingsList = "SettingsListTestTag"
    const val AccountSettingsItem = "AccountSettingsItemTestTag"
}
