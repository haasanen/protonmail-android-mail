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

package ch.protonmail.android.navigation.route

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ch.protonmail.android.LockScreenActivity
import ch.protonmail.android.design.compose.theme.ProtonInvertedTheme
import ch.protonmail.android.mailbugreport.presentation.ui.ApplicationLogsPeekView
import ch.protonmail.android.mailbugreport.presentation.ui.ApplicationLogsScreen
import ch.protonmail.android.mailbugreport.presentation.ui.report.BugReportScreen
import ch.protonmail.android.mailcommon.presentation.extension.navigateBack
import ch.protonmail.android.mailfeatureflags.presentation.ui.FeatureFlagOverridesScreen
import ch.protonmail.android.mailsettings.domain.model.SwipeActionDirection
import ch.protonmail.android.mailsettings.presentation.settings.alternativerouting.AlternativeRoutingSettingScreen
import ch.protonmail.android.mailsettings.presentation.settings.autolock.ui.AutoLockSettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.autolock.ui.pin.AutoLockPinScreen
import ch.protonmail.android.mailsettings.presentation.settings.combinedcontacts.CombinedContactsSettingScreen
import ch.protonmail.android.mailsettings.presentation.settings.language.LanguageSettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.notifications.ui.PushNotificationsSettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.privacy.PrivacySettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.swipeactions.EditSwipeActionPreferenceScreen
import ch.protonmail.android.mailsettings.presentation.settings.swipeactions.EditSwipeActionPreferenceScreen.SWIPE_DIRECTION_KEY
import ch.protonmail.android.mailsettings.presentation.settings.swipeactions.SwipeActionsPreferenceScreen
import ch.protonmail.android.mailsettings.presentation.settings.theme.ThemeSettingsScreen
import ch.protonmail.android.mailsettings.presentation.webaccountsettings.WebAccountSettingScreen
import ch.protonmail.android.mailsettings.presentation.webemailsettings.WebEmailSettingScreen
import ch.protonmail.android.mailsettings.presentation.webfoldersettings.WebFoldersAndLabelsSettingScreen
import ch.protonmail.android.mailsettings.presentation.webprivacysettings.WebPrivacyAndSecuritySettingsScreen
import ch.protonmail.android.mailsettings.presentation.websettings.WebSettingsScreenActions
import ch.protonmail.android.mailsettings.presentation.webspamsettings.WebSpamFilterSettingsScreen
import ch.protonmail.android.navigation.model.Destination.Screen
import me.proton.core.compose.navigation.require

fun NavGraphBuilder.addWebAccountSettings(navController: NavHostController) {
    composable(route = Screen.AccountSettings.route) {
        ProtonInvertedTheme {
            WebAccountSettingScreen(
                actions = WebSettingsScreenActions(
                    onBackClick = { navController.popBackStack() }
                )
            )
        }
    }
}

fun NavGraphBuilder.addWebEmailSettings(navController: NavHostController) {
    composable(route = Screen.EmailSettings.route) {
        ProtonInvertedTheme {
            WebEmailSettingScreen(
                actions = WebSettingsScreenActions(
                    onBackClick = { navController.popBackStack() }
                )
            )
        }
    }
}

fun NavGraphBuilder.addWebFolderAndLabelSettings(navController: NavHostController) {
    composable(route = Screen.FolderAndLabelSettings.route) {
        ProtonInvertedTheme {
            WebFoldersAndLabelsSettingScreen(
                actions = WebSettingsScreenActions(
                    onBackClick = { navController.popBackStack() }
                )
            )
        }
    }
}

fun NavGraphBuilder.addWebPrivacyAndSecuritySettings(navController: NavHostController) {
    composable(route = Screen.PrivacyAndSecuritySettings.route) {
        ProtonInvertedTheme {
            WebPrivacyAndSecuritySettingsScreen(
                actions = WebSettingsScreenActions(
                    onBackClick = { navController.popBackStack() }
                )
            )
        }
    }
}

fun NavGraphBuilder.addWebSpamFilterSettings(navController: NavHostController) {
    composable(route = Screen.SpamFilterSettings.route) {
        ProtonInvertedTheme {
            WebSpamFilterSettingsScreen(
                actions = WebSettingsScreenActions(
                    onBackClick = { navController.popBackStack() }
                )
            )
        }
    }
}

internal fun NavGraphBuilder.addAlternativeRoutingSetting(navController: NavHostController) {
    composable(route = Screen.AlternativeRoutingSettings.route) {
        AlternativeRoutingSettingScreen(
            modifier = Modifier,
            onBackClick = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addCombinedContactsSetting(navController: NavHostController) {
    composable(route = Screen.CombinedContactsSettings.route) {
        CombinedContactsSettingScreen(
            modifier = Modifier,
            onBackClick = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addPrivacySettings(navController: NavHostController) {
    composable(route = Screen.PrivacySettings.route) {
        PrivacySettingsScreen(
            modifier = Modifier,
            onBackClick = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addAutoLockSettings(navController: NavHostController) {
    composable(route = Screen.AutoLockSettings.route) {
        AutoLockSettingsScreen(
            modifier = Modifier,
            onBackClick = { navController.navigateBack() },
            onPinScreenNavigation = { navController.navigate(Screen.AutoLockPinScreen(it)) }
        )
    }
}

internal fun NavGraphBuilder.addAutoLockPinScreen(
    activityActions: LockScreenActivity.Actions,
    onBack: () -> Unit,
    onShowSuccessSnackbar: (String) -> Unit
) {
    composable(route = Screen.AutoLockPinScreen.route) {
        AutoLockPinScreen(
            modifier = Modifier,
            onBackClick = onBack,
            onShowSuccessSnackbar = onShowSuccessSnackbar,
            onBiometricsClick = activityActions.showBiometricPrompt
        )
    }
}

internal fun NavGraphBuilder.addEditSwipeActionsSettings(navController: NavHostController) {
    composable(route = Screen.EditSwipeActionSettings.route) {
        EditSwipeActionPreferenceScreen(
            modifier = Modifier,
            direction = SwipeActionDirection(it.require(SWIPE_DIRECTION_KEY)),
            onBack = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addLanguageSettings(navController: NavHostController) {
    composable(route = Screen.LanguageSettings.route) {
        LanguageSettingsScreen(
            modifier = Modifier,
            onBackClick = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addSwipeActionsSettings(navController: NavHostController) {
    composable(route = Screen.SwipeActionsSettings.route) {
        SwipeActionsPreferenceScreen(
            modifier = Modifier,
            actions = SwipeActionsPreferenceScreen.Actions(
                onBackClick = { navController.navigateBack() },
                onChangeSwipeLeftClick = {
                    navController.navigate(Screen.EditSwipeActionSettings(SwipeActionDirection.LEFT))
                },
                onChangeSwipeRightClick = {
                    navController.navigate(Screen.EditSwipeActionSettings(SwipeActionDirection.RIGHT))
                }
            )
        )
    }
}

internal fun NavGraphBuilder.addThemeSettings(navController: NavHostController) {
    composable(route = Screen.ThemeSettings.route) {
        ThemeSettingsScreen(
            modifier = Modifier,
            onBackClick = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addNotificationsSettings(navController: NavHostController) {
    composable(route = Screen.Notifications.route) {
        PushNotificationsSettingsScreen(
            modifier = Modifier,
            onBackClick = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addExportLogsSettings(navController: NavHostController) {
    composable(route = Screen.ApplicationLogs.route) {
        ApplicationLogsScreen(
            actions = ApplicationLogsScreen.Actions(
                onBackClick = { navController.navigateBack() },
                onViewItemClick = { navController.navigate(Screen.ApplicationLogsView(it)) },
                onFeatureFlagsNavigation = {
                    navController.navigate(Screen.FeatureFlagsOverrides.route)
                }
            )
        )
    }
    composable(route = Screen.ApplicationLogsView.route) {
        ApplicationLogsPeekView(
            onBack = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addFeatureFlagsOverrides(navController: NavHostController) {
    composable(route = Screen.FeatureFlagsOverrides.route) {
        FeatureFlagOverridesScreen(
            onBack = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addBugReporting(navController: NavController, onShowNormalSnackbar: (String) -> Unit) {
    composable(route = Screen.BugReporting.route) {
        BugReportScreen(
            onBack = { navController.navigateBack() },
            onSuccess = {
                navController.navigateBack()
                onShowNormalSnackbar(it)
            }
        )
    }
}
