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

import android.app.Activity
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonMainSettingsItem
import ch.protonmail.android.design.compose.component.ProtonSettingsTopBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonInvertedTheme
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.Avatar
import ch.protonmail.android.mailsession.presentation.model.AccountInformationUiModel
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.R.string
import ch.protonmail.android.mailsettings.presentation.settings.SettingsState.Data
import ch.protonmail.android.mailsettings.presentation.settings.SettingsState.Loading
import me.proton.android.core.devicemigration.presentation.origin.settings.SignInOnTargetDeviceItem
import me.proton.core.domain.entity.UserId

@Composable
fun MainSettingsScreen(
    modifier: Modifier = Modifier,
    actions: MainSettingsScreen.Actions,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {

    val defaultColor = ProtonTheme.colors.backgroundSecondary
    val backgroundColor = ProtonTheme.colors.backgroundNorm
    val view = LocalView.current

    val mainActions = actions.copy(
        onBackClick = {
            // Restore default color when this Composable is removed from composition
            val activity = view.context as? Activity
            activity?.window?.statusBarColor = defaultColor.toArgb()

            actions.onBackClick()
        }
    )
    when (val settingsState = settingsViewModel.state.collectAsStateWithLifecycle(Loading).value) {
        is Data -> MainSettingsScreen(
            modifier = modifier,
            state = settingsState,
            actions = mainActions
        )

        is Loading -> ProtonCenteredProgress(modifier = Modifier.fillMaxSize())
    }

    // In this screen, "Background inverted" theme is used for colouring, which is different
    // from the default theme. Therefore, we need to set/reset the status bar colour manually.
    LaunchedEffect(Unit) {

        val activity = view.context as? Activity
        activity?.window?.statusBarColor = backgroundColor.toArgb()
    }

    BackHandler { mainActions.onBackClick() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreen(
    state: Data,
    actions: MainSettingsScreen.Actions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag(SettingsScreenTestTags.RootItem),
        contentWindowInsets = WindowInsets(
            left = ProtonDimens.Spacing.Large,
            right = ProtonDimens.Spacing.Large
        ),
        topBar = {
            ProtonSettingsTopBar(
                title = stringResource(id = string.mail_settings_settings),
                onBackClick = actions.onBackClick
            )
        }
    ) { contentPadding ->
        Column(
            modifier = modifier
                .testTag(SettingsScreenTestTags.SettingsList)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
        ) {
            AccountSettingsItem(
                modifier = Modifier.testTag(SettingsScreenTestTags.AccountSettingsItem),
                accountInfo = state.accountInfoUiModel,
                onAccountClicked = actions.onAccountClick,
                onSecurityKeysClicked = actions.onSecurityKeysClicked,
                onPasswordManagementClicked = actions.onPasswordManagementClicked
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Medium))

            MainSettingsHeader(titleRes = string.mail_settings_preferences)

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = ProtonTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(),
                colors = CardDefaults.cardColors().copy(
                    containerColor = ProtonTheme.colors.backgroundInvertedSecondary
                )
            ) {

                Column {
                    ProtonMainSettingsItem(
                        name = stringResource(id = string.mail_settings_mailbox),
                        iconRes = R.drawable.ic_proton_envelopes,
                        onClick = actions.onEmailSettingsClick
                    )
                    SettingsItemDivider()


                    ProtonMainSettingsItem(
                        name = stringResource(id = string.mail_settings_folders_labels),
                        iconRes = R.drawable.ic_proton_folder_open,
                        onClick = actions.onFolderAndLabelSettingsClicked
                    )
                    SettingsItemDivider()


                    ProtonMainSettingsItem(
                        name = stringResource(id = string.mail_settings_spam_and_custom_filters),
                        iconRes = R.drawable.ic_proton_sliders,
                        onClick = actions.onSpamFilterSettingsClicked
                    )
                    SettingsItemDivider()


                    ProtonMainSettingsItem(
                        name = stringResource(id = string.mail_settings_privacy_and_security),
                        iconRes = R.drawable.ic_proton_shield_2_bolt,
                        onClick = actions.onPrivacyAndSecuritySettingsClicked
                    )
                    SettingsItemDivider()

                    ProtonMainSettingsItem(
                        name = stringResource(id = string.mail_settings_app),
                        iconRes = R.drawable.ic_proton_mobile,
                        onClick = actions.onAppSettingsClick
                    )
                    SettingsItemDivider()
                }
            }
        }
    }
}

@Composable
fun MainSettingsHeader(@StringRes titleRes: Int) {
    Text(
        modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Large),
        text = stringResource(id = titleRes),
        color = ProtonTheme.colors.textWeak,
        style = ProtonTheme.typography.titleMedium
    )
}

@Composable
fun SettingsItemDivider() {
    HorizontalDivider(
        color = ProtonTheme.colors.backgroundInvertedNorm
    )
}

@Suppress("UseComposableActions")
@Composable
fun AccountSettingsItem(
    modifier: Modifier = Modifier,
    accountInfo: AccountInformationUiModel?,
    onAccountClicked: () -> Unit,
    onSecurityKeysClicked: () -> Unit,
    onPasswordManagementClicked: (UserId?) -> Unit
) {
    val header = accountInfo?.name
        ?: stringResource(id = string.mail_settings_no_information_available)
    val hint = accountInfo?.email ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        ProtonMainSettingsItem(
            modifier = modifier,
            name = header,
            hint = {
                Text(
                    modifier = Modifier.padding(top = ProtonDimens.Spacing.Small),
                    text = hint,
                    color = ProtonTheme.colors.textHint,
                    style = ProtonTheme.typography.bodyMedium
                )
            },
            icon = {
                accountInfo?.avatarUiModel?.let {
                    Avatar(
                        avatarUiModel = it
                    )
                }
            },
            onClick = onAccountClicked
        )
        SettingsItemDivider()

        SignInOnTargetDeviceItem(
            content = { label, onClick ->
                ProtonMainSettingsItem(
                    name = label,
                    iconRes = R.drawable.ic_proton_qr_code,
                    onClick = onClick
                )
            }
        )
        SettingsItemDivider()

        ProtonMainSettingsItem(
            name = stringResource(id = string.mail_settings_fido_keys),
            iconRes = R.drawable.ic_proton_key,
            onClick = onSecurityKeysClicked
        )

        SettingsItemDivider()

        ProtonMainSettingsItem(
            name = stringResource(id = string.mail_settings_change_password),
            iconRes = R.drawable.ic_proton_lock,
            onClick = { onPasswordManagementClicked(accountInfo?.userId) }
        )
    }
}

object MainSettingsScreen {

    data class Actions(
        val onAccountClick: () -> Unit,
        val onAppSettingsClick: () -> Unit,
        val onEmailSettingsClick: () -> Unit,
        val onFolderAndLabelSettingsClicked: () -> Unit,
        val onSpamFilterSettingsClicked: () -> Unit,
        val onPrivacyAndSecuritySettingsClicked: () -> Unit,
        val onSecurityKeysClicked: () -> Unit,
        val onPasswordManagementClicked: (UserId?) -> Unit,
        val onAccountStorageClicked: () -> Unit,
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
    ProtonInvertedTheme {
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
