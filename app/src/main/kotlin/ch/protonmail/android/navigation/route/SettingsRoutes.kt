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
import androidx.navigation.compose.dialog
import ch.protonmail.android.design.compose.theme.ProtonInvertedTheme
import ch.protonmail.android.mailbugreport.presentation.ui.ApplicationLogsPeekView
import ch.protonmail.android.mailbugreport.presentation.ui.ApplicationLogsScreen
import ch.protonmail.android.mailbugreport.presentation.ui.report.BugReportScreen
import ch.protonmail.android.mailcommon.presentation.extension.navigateBack
import ch.protonmail.android.mailfeatureflags.presentation.ui.FeatureFlagOverridesScreen
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockInsertionMode
import ch.protonmail.android.mailpinlock.presentation.autolock.model.DialogType
import ch.protonmail.android.mailpinlock.presentation.autolock.ui.AutoLockIntervalDialog
import ch.protonmail.android.mailpinlock.presentation.autolock.ui.AutoLockSettingsScreen
import ch.protonmail.android.mailpinlock.presentation.autolock.ui.LockScreenOverlay
import ch.protonmail.android.mailpinlock.presentation.pin.ui.AutoLockPinScreen
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockPinScreenDialog
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockPinScreenDialogKeys.AutoLockPinDialogModeKey
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockPinScreenDialogKeys.AutoLockPinDialogResultKey
import ch.protonmail.android.mailsettings.domain.model.SwipeActionDirection
import ch.protonmail.android.mailsettings.presentation.settings.alternativerouting.AlternativeRoutingSettingScreen
import ch.protonmail.android.mailsettings.presentation.settings.combinedcontacts.CombinedContactsSettingScreen
import ch.protonmail.android.mailsettings.presentation.settings.language.LanguageSettingsDialog
import ch.protonmail.android.mailsettings.presentation.settings.notifications.ui.PushNotificationsSettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.privacy.PrivacySettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.swipeactions.EditSwipeActionPreferenceScreen
import ch.protonmail.android.mailsettings.presentation.settings.swipeactions.EditSwipeActionPreferenceScreen.SWIPE_DIRECTION_KEY
import ch.protonmail.android.mailsettings.presentation.settings.swipeactions.SwipeActionsPreferenceScreen
import ch.protonmail.android.mailsettings.presentation.settings.theme.ThemeSettingsDialog
import ch.protonmail.android.mailsettings.presentation.webaccountsettings.WebAccountSettingScreen
import ch.protonmail.android.mailsettings.presentation.webemailsettings.WebEmailSettingScreen
import ch.protonmail.android.mailsettings.presentation.webfoldersettings.WebFoldersAndLabelsSettingScreen
import ch.protonmail.android.mailsettings.presentation.webprivacysettings.WebPrivacyAndSecuritySettingsScreen
import ch.protonmail.android.mailsettings.presentation.websettings.WebSettingsScreenActions
import ch.protonmail.android.mailsettings.presentation.webspamsettings.WebSpamFilterSettingsScreen
import ch.protonmail.android.navigation.model.Destination.Screen
import me.proton.core.compose.navigation.require
import me.proton.core.util.kotlin.deserialize

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

@Suppress("ForbiddenComment")
internal fun NavGraphBuilder.addAutoLockSettings(navController: NavHostController) {
    composable(route = Screen.AutoLockSettings.route) {
        ProtonInvertedTheme {
            AutoLockSettingsScreen(
                modifier = Modifier,
                navController = navController,
                actions = AutoLockSettingsScreen.Actions(
                    onPinScreenNavigation = { navController.navigate(Screen.AutoLockPinScreen(it)) },
                    onBackClick = { navController.navigateBack() },
                    onChangeIntervalClick = { navController.navigate(Screen.AutoLockInterval.route) },
                    onDialogNavigation = { navController.navigate(Screen.AutoLockPinConfirmDialog(it)) }
                )
            )
        }
    }
}

internal fun NavGraphBuilder.addAutoLockOverlay(onClose: () -> Unit, navController: NavHostController) {
    composable(route = Screen.AutoLockOverlay.route) {
        LockScreenOverlay(
            onClose = onClose,
            onNavigateToPinInsertion = {
                navController.navigate(Screen.AutoLockPinScreen(AutoLockInsertionMode.VerifyPin))
            }
        )
    }
}

internal fun NavGraphBuilder.addAutoLockPinScreen(onClose: () -> Unit, onShowSuccessSnackbar: (String) -> Unit) {
    composable(route = Screen.AutoLockPinScreen.route) {
        AutoLockPinScreen(onClose = onClose, onShowSuccessSnackbar = onShowSuccessSnackbar)
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
    dialog(route = Screen.LanguageSettings.route) {
        LanguageSettingsDialog(
            modifier = Modifier,
            onDismiss = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addPinDialog(navController: NavHostController) {
    dialog(route = Screen.AutoLockPinConfirmDialog.route) { backStackEntry ->
        val dialogType = backStackEntry.arguments?.getString(AutoLockPinDialogModeKey)?.deserialize<DialogType>()
            ?: DialogType.None

        AutoLockPinScreenDialog(
            dialogType = dialogType,
            onNavigateBack = { navController.popBackStack() },
            onSuccessWithResult = { resultKey ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(AutoLockPinDialogResultKey, resultKey)
                navController.popBackStack()
            }
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

internal fun NavGraphBuilder.addAutoLockIntervalSettings(navController: NavHostController) {
    dialog(route = Screen.AutoLockInterval.route) {
        AutoLockIntervalDialog(
            modifier = Modifier,
            onDismiss = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addThemeSettings(navController: NavHostController) {
    dialog(route = Screen.ThemeSettings.route) {
        ThemeSettingsDialog(
            modifier = Modifier,
            onDismiss = { navController.navigateBack() }
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
