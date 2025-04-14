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

import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import ch.protonmail.android.MainActivity
import ch.protonmail.android.design.compose.theme.ProtonInvertedTheme
import ch.protonmail.android.feature.account.RemoveAccountDialog
import ch.protonmail.android.feature.account.SignOutAccountDialog
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.presentation.extension.navigateBack
import ch.protonmail.android.mailcomposer.presentation.ui.ComposerScreen
import ch.protonmail.android.mailcomposer.presentation.ui.SetMessagePasswordScreen
import ch.protonmail.android.mailcontact.presentation.contactgroupdetails.ContactGroupDetailsScreen
import ch.protonmail.android.mailcontact.presentation.contactgroupform.ContactGroupFormScreen
import ch.protonmail.android.mailcontact.presentation.contactlist.ui.ContactListScreen
import ch.protonmail.android.mailcontact.presentation.contactsearch.ContactSearchScreen
import ch.protonmail.android.mailcontact.presentation.managemembers.ManageMembersScreen
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetail
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetailScreen
import ch.protonmail.android.mailmailbox.presentation.mailbox.MailboxScreen
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailsettings.presentation.appsettings.AppSettingsScreen
import ch.protonmail.android.mailsettings.presentation.settings.MainSettingsScreen
import ch.protonmail.android.navigation.model.Destination
import ch.protonmail.android.navigation.model.SavedStateKey
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountSwitchEvent
import me.proton.core.compose.navigation.get
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
    openDrawerMenu: () -> Unit,
    onEvent: (AccountSwitchEvent) -> Unit,
    showOfflineSnackbar: () -> Unit,
    showNormalSnackbar: (message: String) -> Unit,
    showErrorSnackbar: (String) -> Unit,
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
                showOfflineSnackbar = showOfflineSnackbar,
                showNormalSnackbar = showNormalSnackbar,
                showErrorSnackbar = showErrorSnackbar,
                onAddLabel = { navController.navigate(Destination.Screen.FolderAndLabelSettings.route) },
                onAddFolder = { navController.navigate(Destination.Screen.FolderAndLabelSettings.route) },
                onAccountAvatarClicked = {
                    navController.navigate(Destination.Screen.AccountsManager.route)
                },
                showMissingFeature = showFeatureMissingSnackbar
            ),
            onEvent = onEvent
        )
    }
}

internal fun NavGraphBuilder.addComposer(
    navController: NavHostController,
    activityActions: MainActivity.Actions,
    showDraftSavedSnackbar: (messasgeId: MessageId) -> Unit,
    showMessageSendingSnackbar: () -> Unit,
    showMessageSendingOfflineSnackbar: () -> Unit
) {
    val actions = ComposerScreen.Actions(
        onCloseComposerClick = navController::navigateBack,
        onSetMessagePasswordClick = { messageId, senderEmail ->
            navController.navigate(Destination.Screen.SetMessagePassword(messageId, senderEmail))
        },
        showDraftSavedSnackbar = showDraftSavedSnackbar,
        showMessageSendingSnackbar = showMessageSendingSnackbar,
        showMessageSendingOfflineSnackbar = showMessageSendingOfflineSnackbar
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
                onAlternativeRoutingClick = showFeatureMissingSnackbar,
                onAppLanguageClick = {
                    navController.navigate(Destination.Screen.LanguageSettings.route)
                },
                onCombinedContactsClick = showFeatureMissingSnackbar,
                onSwipeActionsClick = showFeatureMissingSnackbar,
                onClearCacheClick = {},
                onBackClick = {
                    navController.navigateBack()
                }
            )
        )
    }
}

internal fun NavGraphBuilder.addContacts(
    navController: NavHostController,
    showErrorSnackbar: (message: String) -> Unit,
    showNormalSnackbar: (message: String) -> Unit,
    showFeatureMissingSnackbar: () -> Unit
) {
    composable(route = Destination.Screen.Contacts.route) {
        ContactListScreen(
            listActions = ContactListScreen.Actions.Empty.copy(
                onNavigateToNewContactForm = {
                    navController.navigate(Destination.Screen.CreateContact.route)
                },
                onNavigateToNewGroupForm = {
                    navController.navigate(Destination.Screen.CreateContactGroup.route)
                },
                onNavigateToContactSearch = {
                    navController.navigate(Destination.Screen.ContactSearch.route)
                },
                openImportContact = {
                    showFeatureMissingSnackbar()
                },
                onContactSelected = { contactId ->
                    showFeatureMissingSnackbar()
                    // navController.navigate(Destination.Screen.ContactDetails(contactId))
                },
                onContactGroupSelected = { contactGroupId ->
                    navController.navigate(Destination.Screen.ContactGroupDetails(contactGroupId))
                },
                onBackClick = {
                    navController.navigateBack()
                },
                onSubscriptionUpgradeRequired = {
                    showNormalSnackbar(it)
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

internal fun NavGraphBuilder.addContactGroupDetails(
    navController: NavHostController,
    showErrorSnackbar: (message: String) -> Unit,
    showNormSnackbar: (message: String) -> Unit
) {
    val actions = ContactGroupDetailsScreen.Actions(
        onBackClick = { navController.navigateBack() },
        exitWithErrorMessage = { message ->
            navController.navigateBack()
            showErrorSnackbar(message)
        },
        exitWithNormMessage = { message ->
            navController.navigateBack()
            showNormSnackbar(message)
        },
        showErrorMessage = { message ->
            showErrorSnackbar(message)
        },
        onEditClick = { labelId ->
            navController.navigate(Destination.Screen.EditContactGroup(labelId))
        },
        navigateToComposer = { emails ->
            navController.navigate(Destination.Screen.MessageActionComposer(DraftAction.ComposeToAddresses(emails)))
        }
    )
    composable(route = Destination.Screen.ContactGroupDetails.route) {
        ContactGroupDetailsScreen(actions)
    }
}

internal fun NavGraphBuilder.addContactGroupForm(
    navController: NavHostController,
    showSuccessSnackbar: (message: String) -> Unit,
    showErrorSnackbar: (message: String) -> Unit,
    showNormSnackbar: (message: String) -> Unit
) {
    val actions = ContactGroupFormScreen.Actions(
        onClose = { navController.navigateBack() },
        exitWithErrorMessage = { message ->
            navController.navigateBack()
            showErrorSnackbar(message)
        },
        exitWithSuccessMessage = { message ->
            navController.navigateBack()
            showSuccessSnackbar(message)
        },
        manageMembers = { selectedContactEmailsIds ->
            navController.currentBackStackEntry?.savedStateHandle?.set(
                SavedStateKey.SelectedContactIds.key,
                selectedContactEmailsIds.map { it.id }
            )
            navController.navigate(Destination.Screen.ManageMembers.route)
        },
        exitToContactsWithNormMessage = { message ->
            navController.popBackStack(Destination.Screen.Contacts.route, inclusive = false)
            showNormSnackbar(message)
        },
        showErrorMessage = { message ->
            showErrorSnackbar(message)
        }
    )
    composable(route = Destination.Screen.CreateContactGroup.route) {
        ContactGroupFormScreen(
            actions,
            selectedContactIds = navController.currentBackStackEntry?.savedStateHandle?.getLiveData<List<String>>(
                SavedStateKey.SelectedContactIds.key
            )?.observeAsState()
        )
    }
    composable(route = Destination.Screen.EditContactGroup.route) {
        ContactGroupFormScreen(
            actions,
            selectedContactIds = navController.currentBackStackEntry?.savedStateHandle?.getLiveData<List<String>>(
                SavedStateKey.SelectedContactIds.key
            )?.observeAsState()
        )
    }
}

internal fun NavGraphBuilder.addManageMembers(
    navController: NavHostController,
    showErrorSnackbar: (message: String) -> Unit
) {
    val actions = ManageMembersScreen.Actions(
        onDone = { selectedContactEmailsIds ->
            navController.previousBackStackEntry?.savedStateHandle?.set(
                SavedStateKey.SelectedContactIds.key,
                selectedContactEmailsIds.map { it.id }
            )
            navController.navigateBack()
        },
        onClose = { navController.navigateBack() },
        exitWithErrorMessage = { message ->
            navController.navigateBack()
            showErrorSnackbar(message)
        }
    )
    composable(route = Destination.Screen.ManageMembers.route) {
        ManageMembersScreen(
            actions,
            selectedContactIds = navController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<List<String>>(SavedStateKey.SelectedContactIds.key)
                ?.observeAsState()
        )
    }
}

internal fun NavGraphBuilder.addContactSearch(
    navController: NavHostController,
    showFeatureMissingSnackbar: () -> Unit
) {
    val actions = ContactSearchScreen.Actions(
        onContactSelected = { contactId ->
            showFeatureMissingSnackbar()
//            navController.navigate(Destination.Screen.ContactDetails(contactId))
        },
        onContactGroupSelected = { labelId ->
            navController.navigate(Destination.Screen.ContactGroupDetails(labelId))
        },
        onClose = { navController.navigateBack() }
    )
    composable(route = Destination.Screen.ContactSearch.route) {
        ContactSearchScreen(
            actions
        )
    }
}
