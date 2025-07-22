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

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import ch.protonmail.android.MainActivity
import ch.protonmail.android.design.compose.navigation.get
import ch.protonmail.android.design.compose.theme.ProtonInvertedTheme
import ch.protonmail.android.feature.account.RemoveAccountDialog
import ch.protonmail.android.feature.account.SignOutAccountDialog
import ch.protonmail.android.mailattachments.domain.model.OpenAttachmentIntentValues
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.presentation.SnackbarType
import ch.protonmail.android.mailcommon.presentation.extension.navigateBack
import ch.protonmail.android.mailcomposer.presentation.ui.ComposerScreen
import ch.protonmail.android.mailcomposer.presentation.ui.SetMessagePasswordScreen
import ch.protonmail.android.mailcontact.presentation.contactdetails.ui.ContactDetailsScreen
import ch.protonmail.android.mailcontact.presentation.contactgroupdetails.ContactGroupDetailsScreen
import ch.protonmail.android.mailcontact.presentation.contactlist.ui.ContactListScreen
import ch.protonmail.android.mailcontact.presentation.contactsearch.ContactSearchScreen
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetail
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetailScreen
import ch.protonmail.android.mailmailbox.presentation.mailbox.MailboxScreen
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailsettings.presentation.appsettings.AppSettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.MainSettingsScreen
import ch.protonmail.android.navigation.model.Destination
import ch.protonmail.android.uicomponents.fab.ProtonFabHostState
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountSwitchEvent
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.takeIfNotBlank

internal fun NavGraphBuilder.addConversationDetail(actions: ConversationDetail.Actions) {
    composable(route = Destination.Screen.Conversation.route) {
        ConversationDetailScreen(actions = actions)
    }
}

@Suppress("LongParameterList")
internal fun NavGraphBuilder.addMailbox(
    navController: NavHostController,
    fabHostState: ProtonFabHostState,
    openDrawerMenu: () -> Unit,
    setDrawerEnabled: (Boolean) -> Unit,
    onEvent: (AccountSwitchEvent) -> Unit,
    onAttachmentReady: (OpenAttachmentIntentValues) -> Unit,
    showSnackbar: (type: SnackbarType) -> Unit,
    showFeatureMissingSnackbar: () -> Unit
) {
    composable(route = Destination.Screen.Mailbox.route) {
        MailboxScreen(
            actions = MailboxScreen.Actions.Empty.copy(
                navigateToMailboxItem = { request ->
                    val destination = when (request.shouldOpenInComposer) {
                        true -> Destination.Screen.EditDraftComposer(MessageId(request.itemId.value))
                        false -> Destination.Screen.Conversation(
                            ConversationId(request.itemId.value),
                            request.subItemId?.let { mailboxItemId ->
                                MessageId(mailboxItemId.value)
                            },
                            request.openedFromLocation
                        )
                    }
                    navController.navigate(destination)
                },
                navigateToComposer = { navController.navigate(Destination.Screen.Composer.route) },
                openDrawerMenu = openDrawerMenu,
                showSnackbar = showSnackbar,
                onAddLabel = { navController.navigate(Destination.Screen.FolderAndLabelSettings.route) },
                onAddFolder = { navController.navigate(Destination.Screen.FolderAndLabelSettings.route) },
                onAccountAvatarClicked = {
                    navController.navigate(Destination.Screen.AccountsManager.route)
                },
                showMissingFeature = showFeatureMissingSnackbar,
                onEnterSearchMode = {
                    setDrawerEnabled(false)
                },
                onExitSearchMode = {
                    setDrawerEnabled(true)
                },
                onAttachmentReady = onAttachmentReady
            ),
            onEvent = onEvent,
            fabHostState = fabHostState
        )
    }
}

@Suppress("LongParameterList")
internal fun NavGraphBuilder.addComposer(
    navController: NavHostController,
    activityActions: MainActivity.Actions,
    showDraftSavedSnackbar: (messageId: MessageId) -> Unit,
    showMessageSendingSnackbar: () -> Unit,
    showMessageSendingOfflineSnackbar: () -> Unit,
    showMessageSchedulingSnackbar: () -> Unit,
    showMessageSchedulingOfflineSnackbar: () -> Unit,
    showDraftDiscardedSnackbar: () -> Unit
) {
    val actions = ComposerScreen.Actions(
        onCloseComposerClick = navController::navigateBack,
        onSetMessagePasswordClick = { messageId, senderEmail ->
            navController.navigate(Destination.Screen.SetMessagePassword(messageId, senderEmail))
        },
        showDraftSavedSnackbar = showDraftSavedSnackbar,
        showMessageSendingSnackbar = showMessageSendingSnackbar,
        showMessageSendingOfflineSnackbar = showMessageSendingOfflineSnackbar,
        showMessageSchedulingSnackbar = { showMessageSchedulingSnackbar() },
        showMessageSchedulingOfflineSnackbar = { showMessageSchedulingOfflineSnackbar() },
        showDraftDiscardedSnackbar = showDraftDiscardedSnackbar
    )
    composable(route = Destination.Screen.Composer.route) { ComposerScreen(actions) }
    composable(route = Destination.Screen.EditDraftComposer.route) { ComposerScreen(actions) }
    composable(route = Destination.Screen.MessageActionComposer.route) { ComposerScreen(actions) }
    composable(route = Destination.Screen.ShareFileComposer.route) {
        ComposerScreen(
            actions.copy(
                onCloseComposerClick = { activityActions.finishActivity() }
            )
        )
    }
}

internal fun NavGraphBuilder.addSignOutAccountDialog(navController: NavHostController) {
    dialog(route = Destination.Dialog.SignOut.route) {
        SignOutAccountDialog(
            userId = it.get<String>(SignOutAccountDialog.USER_ID_KEY)?.takeIfNotBlank()?.let(::UserId),
            actions = SignOutAccountDialog.Actions(
                onSignedOut = { navController.navigateBack() },
                onRemoved = { navController.navigateBack() },
                onCancelled = { navController.navigateBack() }
            )
        )
    }
}

internal fun NavGraphBuilder.addSetMessagePassword(navController: NavHostController) {
    composable(route = Destination.Screen.SetMessagePassword.route) {
        SetMessagePasswordScreen(
            onBackClick = {
                navController.navigateBack()
            }
        )
    }
}

internal fun NavGraphBuilder.addRemoveAccountDialog(navController: NavHostController) {
    dialog(route = Destination.Dialog.RemoveAccount.route) {
        RemoveAccountDialog(
            userId = it.get<String>(RemoveAccountDialog.USER_ID_KEY)?.takeIfNotBlank()?.let(::UserId),
            onRemoved = { navController.navigateBack() },
            onCancelled = { navController.navigateBack() }
        )
    }
}

internal fun NavGraphBuilder.addSettings(navController: NavHostController) {
    composable(route = Destination.Screen.Settings.route) {
        ProtonInvertedTheme {
            MainSettingsScreen(
                actions = MainSettingsScreen.Actions(
                    onAccountClick = {
                        navController.navigate(Destination.Screen.AccountSettings.route)
                    },
                    onAppSettingsClick = {
                        navController.navigate(Destination.Screen.AppSettings.route)
                    },
                    onEmailSettingsClick = {
                        navController.navigate(Destination.Screen.EmailSettings.route)
                    },
                    onFolderAndLabelSettingsClicked = {
                        navController.navigate(Destination.Screen.FolderAndLabelSettings.route)
                    },
                    onSpamFilterSettingsClicked = {
                        navController.navigate(Destination.Screen.SpamFilterSettings.route)
                    },
                    onPrivacyAndSecuritySettingsClicked = {
                        navController.navigate(Destination.Screen.PrivacyAndSecuritySettings.route)
                    },
                    onBackClick = {
                        navController.navigateBack()
                    }
                )
            )
        }
    }
}

internal fun NavGraphBuilder.addAppSettings(navController: NavHostController, showFeatureMissingSnackbar: () -> Unit) {
    composable(route = Destination.Screen.AppSettings.route) {
        ProtonInvertedTheme {
            AppSettingsScreen(
                actions = AppSettingsScreen.Actions(
                    onThemeClick = {
                        navController.navigate(Destination.Screen.ThemeSettings.route)
                    },
                    onPushNotificationsClick = {
                        navController.navigate(Destination.Screen.Notifications.route)
                    },
                    onAutoLockClick = {
                        navController.navigate(Destination.Screen.AutoLockSettings.route)
                    },
                    onAppLanguageClick = {
                        navController.navigate(Destination.Screen.LanguageSettings.route)
                    },
                    onSwipeToNextEmailClick = showFeatureMissingSnackbar,
                    onSwipeActionsClick = showFeatureMissingSnackbar,
                    onViewApplicationLogsClick = showFeatureMissingSnackbar,
                    onCustomizeToolbarClick = showFeatureMissingSnackbar,
                    onBackClick = {
                        navController.navigateBack()
                    }
                )
            )
        }
    }
}

internal fun NavGraphBuilder.addContacts(
    navController: NavHostController,
    showErrorSnackbar: (message: String) -> Unit,
    showFeatureMissingSnackbar: () -> Unit
) {
    composable(route = Destination.Screen.Contacts.route) {
        ContactListScreen(
            listActions = ContactListScreen.Actions.Empty.copy(
                onNavigateToNewContactForm = {
                    navController.navigate(Destination.Screen.CreateContact.route)
                },
                onNavigateToNewGroupForm = showFeatureMissingSnackbar,
                onNavigateToContactSearch = {
                    navController.navigate(Destination.Screen.ContactSearch.route)
                },
                openImportContact = {
                    showFeatureMissingSnackbar()
                },
                onContactSelected = { contactId ->
                    navController.navigate(Destination.Screen.ContactDetails(contactId))
                },
                onContactGroupSelected = { contactGroupId ->
                    navController.navigate(Destination.Screen.ContactGroupDetails(contactGroupId))
                },
                onBackClick = {
                    navController.navigateBack()
                },
                onNewGroupClick = {
                    // Defined at the inner call site.
                },
                exitWithErrorMessage = { message ->
                    navController.navigateBack()
                    showErrorSnackbar(message)
                }
            )
        )
    }
}

internal fun NavGraphBuilder.addContactDetails(
    navController: NavHostController,
    onShowErrorSnackbar: (String) -> Unit,
    onMessageContact: (String) -> Unit,
    showFeatureMissingSnackbar: () -> Unit
) {
    composable(route = Destination.Screen.ContactDetails.route) {
        ContactDetailsScreen(
            actions = ContactDetailsScreen.Actions(
                onBack = { navController.navigateBack() },
                onShowErrorSnackbar = onShowErrorSnackbar,
                onMessageContact = onMessageContact,
                showFeatureMissingSnackbar = showFeatureMissingSnackbar
            )
        )
    }
}

internal fun NavGraphBuilder.addContactSearch(
    navController: NavHostController,
    showFeatureMissingSnackbar: () -> Unit
) {
    val actions = ContactSearchScreen.Actions(
        onContactSelected = { contactId ->
            navController.navigate(Destination.Screen.ContactDetails(contactId))
        },
        onContactGroupSelected = { _ ->
            showFeatureMissingSnackbar()
        },
        onClose = { navController.navigateBack() }
    )
    composable(route = Destination.Screen.ContactSearch.route) {
        ContactSearchScreen(
            actions
        )
    }
}

internal fun NavGraphBuilder.addContactGroupDetails(
    navController: NavHostController,
    onShowErrorSnackbar: (String) -> Unit,
    showFeatureMissingSnackbar: () -> Unit
) {
    composable(route = Destination.Screen.ContactGroupDetails.route) {
        ContactGroupDetailsScreen(
            actions = ContactGroupDetailsScreen.Actions(
                onBack = { navController.navigateBack() },
                onShowErrorSnackbar = onShowErrorSnackbar,
                showFeatureMissingSnackbar = showFeatureMissingSnackbar
            )
        )
    }
}
