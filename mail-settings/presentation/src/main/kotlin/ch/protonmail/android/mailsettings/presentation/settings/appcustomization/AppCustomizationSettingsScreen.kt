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

package ch.protonmail.android.mailsettings.presentation.settings.appcustomization

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonAppCustomizationSettingsItemInvert
import ch.protonmail.android.design.compose.component.ProtonAppCustomizationSettingsItemNorm
import ch.protonmail.android.design.compose.component.ProtonAppCustomizationSettingsToggleItem
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonMainSettingsIcon
import ch.protonmail.android.design.compose.component.ProtonSettingsAppCustomizationTopBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonInvertedTheme
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.appsettings.AppSettingsScreen
import ch.protonmail.android.mailsettings.presentation.appsettings.AppSettingsScreenPreviewData
import ch.protonmail.android.mailsettings.presentation.appsettings.AppSettingsState
import ch.protonmail.android.mailsettings.presentation.settings.MainSettingsHeader
import ch.protonmail.android.mailsettings.presentation.settings.SettingsItemDivider

@Composable
fun AppCustomizationSettingsScreen(
    modifier: Modifier = Modifier,
    actions: AppSettingsScreen.Actions,
    viewModel: AppCustomizationSettingsViewModel = hiltViewModel()
) {
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        is AppSettingsState.Data -> {
            AppCustomizationSettingsScreen(
                modifier = modifier,
                actions = actions,
                state = state
            )
        }

        is AppSettingsState.Loading -> ProtonCenteredProgress(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun AppCustomizationSettingsScreen(
    modifier: Modifier = Modifier,
    actions: AppSettingsScreen.Actions,
    state: AppSettingsState.Data
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsAppCustomizationTopBar(
                title = stringResource(id = R.string.mail_settings_app_customization),
                onBackClick = actions.onBackClick
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = ProtonDimens.Spacing.Large)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))

                NotificationLanguageSettingsItem(
                    notificationStatus = "On",
                    language = state.appSettings.customAppLanguage ?: "English",
                    onNotificationClick = { launchNotificationSettingsIntent(context) },
                    onLanguageClick = actions.onAppLanguageClick
                )

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))

                AppearanceSettingsItem(
                    appearance = "System default",
                    onClick = actions.onThemeClick
                )

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))

                ProtectionSettingsItem(
                    autoLockStatus = if (state.appSettings.hasAutoLock) {
                        stringResource(id = R.string.mail_settings_app_customization_protection_enabled_description)
                    } else {
                        stringResource(id = R.string.mail_settings_app_customization_protection_disabled_description)
                    },
                    onClick = actions.onAutoLockClick
                )

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))

                UseDeviceContactsSettingsItem(
                    useDeviceContacts = state.appSettings.hasCombinedContacts,
                    onToggle = {
                        actions.onCombinedContactsClick()
                    }
                )

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Medium))

                MainSettingsHeader(titleRes = R.string.mail_settings_app_customization_mail_experience_header)

                MailExperienceSettingsItem(
                    swipeToNextEmail = true,
                    actions = actions
                )

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Medium))

                MainSettingsHeader(titleRes = R.string.mail_settings_app_customization_advanced_header)
                AdvancedSettingsItem(
                    alternativeRouting = state.appSettings.hasAlternativeRouting,
                    actions = actions
                )

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Jumbo))
            }
        }
    )
}

private fun launchNotificationSettingsIntent(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    startActivity(context, intent, null)
}

@Composable
private fun NotificationLanguageSettingsItem(
    modifier: Modifier = Modifier,
    notificationStatus: String,
    language: String,
    onNotificationClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {}
) {

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
            ProtonAppCustomizationSettingsItemInvert(
                modifier = modifier,
                name = stringResource(id = R.string.mail_settings_app_customization_notification),
                hint = notificationStatus,
                onClick = onNotificationClick,
                icon = {
                    ProtonMainSettingsIcon(
                        iconRes = R.drawable.ic_proton_arrow_out_over_square,
                        contentDescription = stringResource(id = R.string.mail_settings_app_customization_notification),
                        tint = ProtonTheme.colors.iconHint
                    )
                }
            )

            SettingsItemDivider()

            ProtonAppCustomizationSettingsItemInvert(
                modifier = modifier,
                name = stringResource(id = R.string.mail_settings_app_customization_language),
                hint = language,
                onClick = onLanguageClick,
                icon = {
                    ProtonMainSettingsIcon(
                        iconRes = R.drawable.ic_proton_arrow_out_over_square,
                        contentDescription = stringResource(id = R.string.mail_settings_app_customization_language),
                        tint = ProtonTheme.colors.iconHint
                    )
                }
            )
        }
    }
}

@Composable
private fun AppearanceSettingsItem(
    modifier: Modifier = Modifier,
    appearance: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {

        ProtonAppCustomizationSettingsItemInvert(
            modifier = modifier,
            name = stringResource(id = R.string.mail_settings_app_customization_appearance),
            hint = appearance,
            onClick = onClick,
            icon = {
                ProtonMainSettingsIcon(
                    iconRes = R.drawable.ic_proton_chevron_up_down,
                    contentDescription = stringResource(id = R.string.mail_settings_app_customization_appearance),
                    tint = ProtonTheme.colors.iconHint
                )
            }
        )
    }
}

@Composable
private fun ProtectionSettingsItem(
    modifier: Modifier = Modifier,
    autoLockStatus: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {

        ProtonAppCustomizationSettingsItemInvert(
            modifier = modifier,
            name = stringResource(id = R.string.mail_settings_app_customization_protection),
            hint = autoLockStatus,
            onClick = onClick,
            icon = {
                ProtonMainSettingsIcon(
                    iconRes = R.drawable.ic_proton_chevron_right,
                    contentDescription = stringResource(id = R.string.mail_settings_app_customization_protection),
                    tint = ProtonTheme.colors.iconHint
                )
            }
        )
    }
}

@Composable
private fun UseDeviceContactsSettingsItem(
    modifier: Modifier = Modifier,
    useDeviceContacts: Boolean,
    onToggle: (Boolean) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {

        ProtonAppCustomizationSettingsToggleItem(
            modifier = modifier,
            name = stringResource(id = R.string.mail_settings_app_customization_use_device_contacts),
            hint = stringResource(id = R.string.mail_settings_app_customization_use_device_contacts_hint),
            value = useDeviceContacts,
            onToggle = onToggle
        )
    }
}

@Composable
private fun MailExperienceSettingsItem(
    modifier: Modifier = Modifier,
    swipeToNextEmail: Boolean,
    actions: AppSettingsScreen.Actions
) {
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
            ProtonAppCustomizationSettingsToggleItem(
                modifier = modifier,
                name = stringResource(id = R.string.mail_settings_app_customization_swipe_to_next_email),
                hint = stringResource(id = R.string.mail_settings_app_customization_swipe_to_next_email_hint),
                value = swipeToNextEmail,
                onToggle = {
                    actions.onSwipeToNextEmailClick()
                }
            )

            SettingsItemDivider()

            ProtonAppCustomizationSettingsItemNorm(
                modifier = modifier,
                name = stringResource(id = R.string.mail_settings_app_customization_swipe_action),
                hint = stringResource(id = R.string.mail_settings_app_customization_swipe_action_hint),
                onClick = { actions.onSwipeActionsClick() },
                icon = {
                    ProtonMainSettingsIcon(
                        iconRes = R.drawable.ic_proton_chevron_right,
                        contentDescription = stringResource(
                            id = R.string.mail_settings_app_customization_swipe_action_hint
                        ),
                        tint = ProtonTheme.colors.iconHint
                    )
                }
            )

            SettingsItemDivider()

            ProtonAppCustomizationSettingsItemNorm(
                modifier = modifier,
                name = stringResource(id = R.string.mail_settings_app_customization_customize_toolbar),
                onClick = { actions.onCustomizeToolbarClick() },
                icon = {
                    ProtonMainSettingsIcon(
                        iconRes = R.drawable.ic_proton_chevron_right,
                        contentDescription = stringResource(
                            id = R.string.mail_settings_app_customization_customize_toolbar
                        ),
                        tint = ProtonTheme.colors.iconHint
                    )
                }
            )
        }
    }
}

@Composable
private fun AdvancedSettingsItem(
    modifier: Modifier = Modifier,
    alternativeRouting: Boolean,
    actions: AppSettingsScreen.Actions
) {
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

            ProtonAppCustomizationSettingsToggleItem(
                modifier = modifier,
                name = stringResource(id = R.string.mail_settings_app_customization_alternative_routing),
                hint = stringResource(id = R.string.mail_settings_app_customization_alternative_routing_hint),
                value = alternativeRouting,
                onToggle = { actions.onAlternativeRoutingClick() }
            )

            SettingsItemDivider()

            ProtonAppCustomizationSettingsItemNorm(
                modifier = modifier,
                name = stringResource(id = R.string.mail_settings_app_customization_view_application_logs),
                onClick = { actions.onViewApplicationLogsClick() },
                icon = {
                    ProtonMainSettingsIcon(
                        iconRes = R.drawable.ic_proton_chevron_right,
                        contentDescription = stringResource(
                            id = R.string.mail_settings_app_customization_view_application_logs
                        ),
                        tint = ProtonTheme.colors.iconHint
                    )
                }
            )
        }
    }
}

@Preview(
    name = "App customization settings screen light mode",
    showBackground = true
)
@Composable
fun PreviewAppCustomizationScreenLight() {
    ProtonInvertedTheme {
        AppCustomizationSettingsScreen(
            actions = AppSettingsScreenPreviewData.Actions,
            state = AppSettingsScreenPreviewData.Data
        )
    }
}

@Preview(
    name = "App customization settings screen dark mode",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewAppCustomizationScreenDark() {
    ProtonInvertedTheme {
        AppCustomizationSettingsScreen(
            actions = AppSettingsScreenPreviewData.Actions,
            state = AppSettingsScreenPreviewData.Data
        )
    }
}
