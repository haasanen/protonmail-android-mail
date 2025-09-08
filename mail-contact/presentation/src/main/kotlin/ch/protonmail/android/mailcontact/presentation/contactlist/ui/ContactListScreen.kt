package ch.protonmail.android.mailcontact.presentation.contactlist.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonModalBottomSheetLayout
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetVisibilityEffect
import ch.protonmail.android.mailcommon.presentation.ui.CommonTestTags
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListState
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListViewAction
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListViewModel
import ch.protonmail.android.mailcontact.presentation.contactlist.ui.ContactListScreen.DELAY_SHOWING
import ch.protonmail.android.mailcontact.presentation.dialogs.ContactDeleteConfirmationDialog
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.ui.RedirectToWebBottomSheetContent
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(listActions: ContactListScreen.Actions, viewModel: ContactListViewModel = hiltViewModel()) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    val state = viewModel.state.collectAsStateWithLifecycle().value
    var showBottomSheet by remember { mutableStateOf(false) }

    val defaultColor = ProtonTheme.colors.backgroundNorm
    val backgroundColor = ProtonTheme.colors.backgroundSecondary
    val view = LocalView.current
    val context = LocalContext.current

    val deleteDialogState = remember { mutableStateOf<ContactListItemUiModel.Contact?>(null) }

    val actions = listActions.copy(
        onBackClick = {
            // Restore default color when this Composable is removed from composition
            val activity = view.context as? Activity
            activity?.window?.statusBarColor = defaultColor.toArgb()

            listActions.onBackClick()
        },
        onDeleteContactRequest = { contact ->
            viewModel.submit(ContactListViewAction.OnDeleteContactRequested(contact))
        }
    )

    // In this screen, "Background inverted" theme is used for colouring, which is different
    // from the default theme. Therefore, we need to set/reset the status bar colour manually.
    LaunchedEffect(Unit) {

        val activity = view.context as? Activity
        activity?.window?.statusBarColor = backgroundColor.toArgb()
    }

    BackHandler(bottomSheetState.isVisible) {
        viewModel.submit(ContactListViewAction.OnDismissBottomSheet)
    }

    ProtonModalBottomSheetLayout(
        showBottomSheet = showBottomSheet,
        sheetState = bottomSheetState,
        onDismissed = { showBottomSheet = false },
        dismissOnBack = true,
        sheetContent = {
            if (state is ContactListState.Loaded) {
                when (state.bottomSheetType) {
                    ContactListState.BottomSheetType.RedirectToWeb -> {
                        RedirectToWebBottomSheetContent(
                            description = R.string.add_contact_bottom_sheet_redirect_to_web_descrption,
                            buttonText = R.string.contact_bottom_sheet_redirect_to_web_button,
                            onConfirm = { launchBrowser(context) },
                            onDismiss = { showBottomSheet = false }
                        )
                    }

                    ContactListState.BottomSheetType.Menu -> {
                        ContactBottomSheetContent(
                            actions = ContactBottomSheet.Actions(
                                onNewContactClick = { /* No-op, unimplemented */ },
                                onNewContactGroupClick = { /* No-op, unimplemented */ },
                                onImportContactClick = { /* No-op, unimplemented */ }
                            )
                        )
                    }

                    ContactListState.BottomSheetType.Upselling -> Unit
                }
            }
        }
    ) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                ContactListTopBar(
                    actions = ContactListTopBar.Actions(
                        onBackClick = actions.onBackClick,
                        onAddClick = {
                            viewModel.submit(ContactListViewAction.OnOpenBottomSheet)
                        },
                        onSearchClick = {
                            viewModel.submit(ContactListViewAction.OnOpenContactSearch)
                        }
                    ),
                    isAddButtonVisible = true
                )
            },
            content = { paddingValues ->
                if (state is ContactListState.Loaded) {
                    ConsumableLaunchedEffect(effect = state.openContactSearch) {
                        actions.onNavigateToContactSearch()
                    }
                    ConsumableLaunchedEffect(effect = state.bottomSheetVisibilityEffect) { bottomSheetEffect ->
                        when (bottomSheetEffect) {
                            BottomSheetVisibilityEffect.Hide -> {
                                bottomSheetState.hide()
                            }

                            BottomSheetVisibilityEffect.Show -> {
                                if (!showBottomSheet) {
                                    showBottomSheet = true
                                    delay(DELAY_SHOWING)
                                }
                                bottomSheetState.show()
                            }
                        }
                    }
                }

                when (state) {
                    is ContactListState.Loaded.Data -> {
                        ConsumableLaunchedEffect(effect = state.showDeleteConfirmDialog) { contact ->
                            deleteDialogState.value = contact
                        }
                        ConsumableTextEffect(effect = state.showDeleteConfirmationSnackbar) { message ->
                            snackbarHostState.showSnackbar(ProtonSnackbarType.NORM, message)
                        }

                        ContactListScreenContent(
                            modifier = Modifier.padding(paddingValues),
                            state = state,
                            actions = actions
                        )
                    }

                    is ContactListState.Loaded.Empty -> {
                        ContactEmptyDataScreen(
                            iconResId = R.drawable.ic_proton_users_plus,
                            title = stringResource(R.string.no_contacts),
                            description = stringResource(R.string.no_contacts_description),
                            buttonText = stringResource(R.string.add_contact),
                            showAddButton = false, // Hide button as feature isn't currently on the roadmap
                            onAddClick = { viewModel.submit(ContactListViewAction.OnOpenBottomSheet) }
                        )
                    }

                    is ContactListState.Loading -> {
                        ProtonCenteredProgress(modifier = Modifier.fillMaxSize())

                        ConsumableTextEffect(effect = state.errorLoading) { message ->
                            actions.exitWithErrorMessage(message)
                        }
                    }
                }
            },
            snackbarHost = {
                DismissableSnackbarHost(
                    modifier = Modifier.testTag(CommonTestTags.SnackbarHost),
                    protonSnackbarHostState = snackbarHostState
                )
            }
        )
    }

    ContactDeleteConfirmationDialog(
        contactToDelete = deleteDialogState.value,
        onDeleteConfirmed = { viewModel.submit(ContactListViewAction.OnDeleteContactConfirmed(it)) },
        onDismissRequest = { deleteDialogState.value = null }
    )
}

private fun launchBrowser(context: Context) {
    val uri = "https://mail.proton.me/inbox#create-contact".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

object ContactListScreen {

    const val DELAY_SHOWING = 100L

    data class Actions(
        val onBackClick: () -> Unit,
        val onContactSelected: (ContactId) -> Unit,
        val onContactGroupSelected: (ContactGroupId) -> Unit,
        val onNavigateToNewContactForm: () -> Unit,
        val onNavigateToNewGroupForm: () -> Unit,
        val onNavigateToContactSearch: () -> Unit,
        val onNewGroupClick: () -> Unit,
        val openImportContact: () -> Unit,
        val exitWithErrorMessage: (String) -> Unit,
        val onDeleteContactRequest: (ContactListItemUiModel.Contact) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onBackClick = {},
                onContactSelected = {},
                onContactGroupSelected = {},
                onNavigateToNewContactForm = {},
                onNavigateToNewGroupForm = {},
                onNavigateToContactSearch = {},
                openImportContact = {},
                onNewGroupClick = {},
                exitWithErrorMessage = {},
                onDeleteContactRequest = {}
            )

            fun fromContactSearchActions(
                onContactClick: (ContactId) -> Unit = {},
                onContactGroupClick: (ContactGroupId) -> Unit = {}
            ) = Empty.copy(
                onContactSelected = onContactClick,
                onContactGroupSelected = onContactGroupClick
            )
        }
    }
}
