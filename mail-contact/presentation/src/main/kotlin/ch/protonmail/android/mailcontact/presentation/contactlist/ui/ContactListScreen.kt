package ch.protonmail.android.mailcontact.presentation.contactlist.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.contactlist.BottomSheetVisibilityEffect
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListState
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListViewAction
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListViewModel
import ch.protonmail.android.mailcontact.presentation.utils.ContactFeatureFlags.ContactCreate
import ch.protonmail.android.uicomponents.bottomsheet.bottomSheetHeightConstrainedContent
import kotlinx.coroutines.launch
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonModalBottomSheetLayout
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcontact.domain.model.ContactId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(listActions: ContactListScreen.Actions, viewModel: ContactListViewModel = hiltViewModel()) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val state = viewModel.state.collectAsStateWithLifecycle().value

    val actions = listActions.copy(
        onNewGroupClick = { viewModel.submit(ContactListViewAction.OnNewContactGroupClick) }
    )

    if (state is ContactListState.Loaded) {
        ConsumableLaunchedEffect(effect = state.bottomSheetVisibilityEffect) { bottomSheetEffect ->
            when (bottomSheetEffect) {
                BottomSheetVisibilityEffect.Hide -> scope.launch { bottomSheetState.hide() }
                BottomSheetVisibilityEffect.Show -> scope.launch { bottomSheetState.show() }
            }
        }
    }

    if (bottomSheetState.currentValue != SheetValue.Hidden) {
        DisposableEffect(Unit) { onDispose { viewModel.submit(ContactListViewAction.OnDismissBottomSheet) } }
    }

    BackHandler(bottomSheetState.isVisible) {
        viewModel.submit(ContactListViewAction.OnDismissBottomSheet)
    }

    // In this screen, "Background inverted" theme is used for colouring, which different
    // from the default theme. Therefore, we need to set/reset the status bar colour manually.
    val defaultColor = ProtonTheme.colors.backgroundNorm
    val backgroundColor = ProtonTheme.colors.backgroundSecondary
    val view = LocalView.current
    DisposableEffect(Unit) {
        val activity = view.context as? Activity ?: return@DisposableEffect onDispose {}

        // Set the status bar color
        activity.window.statusBarColor = backgroundColor.toArgb()

        onDispose {
            // Restore default color when this Composable is removed from composition
            activity.window.statusBarColor = defaultColor.toArgb()
        }
    }

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = bottomSheetHeightConstrainedContent {
            ContactBottomSheetContent(
                isContactGroupsCrudEnabled = state.isContactGroupsCrudEnabled,
                actions = ContactBottomSheet.Actions(
                    onNewContactClick = {
                        viewModel.submit(ContactListViewAction.OnNewContactClick)
                    },
                    onNewContactGroupClick = {
                        viewModel.submit(ContactListViewAction.OnNewContactGroupClick)
                    },
                    onImportContactClick = {
                        viewModel.submit(ContactListViewAction.OnImportContactClick)
                    }
                )
            )
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
                    isAddButtonVisible = state is ContactListState.Loaded.Data,
                    isContactGroupsCrudEnabled = state.isContactGroupsCrudEnabled,
                    isContactSearchEnabled = state.isContactSearchEnabled
                )
            },
            content = { paddingValues ->
                if (state is ContactListState.Loaded) {
                    ConsumableLaunchedEffect(effect = state.openContactForm) {
                        actions.onNavigateToNewContactForm()
                    }
                    ConsumableLaunchedEffect(effect = state.openContactGroupForm) {
                        actions.onNavigateToNewGroupForm()
                    }
                    ConsumableLaunchedEffect(effect = state.openImportContact) {
                        actions.openImportContact()
                    }
                    ConsumableLaunchedEffect(effect = state.openContactSearch) {
                        actions.onNavigateToContactSearch()
                    }
                }

                when (state) {
                    is ContactListState.Loaded.Data -> {
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
                            showAddButton = ContactCreate.value,
                            onAddClick = { viewModel.submit(ContactListViewAction.OnOpenBottomSheet) }
                        )

                        ConsumableTextEffect(effect = state.subscriptionError) { message ->
                            actions.onSubscriptionUpgradeRequired(message)
                        }
                    }

                    is ContactListState.Loading -> {
                        ProtonCenteredProgress(modifier = Modifier.fillMaxSize())

                        ConsumableTextEffect(effect = state.errorLoading) { message ->
                            actions.exitWithErrorMessage(message)
                        }
                    }
                }
            }
        )
    }
}

object ContactListScreen {

    data class Actions(
        val onBackClick: () -> Unit,
        val onContactSelected: (ContactId) -> Unit,
        val onContactGroupSelected: (ContactGroupId) -> Unit,
        val onNavigateToNewContactForm: () -> Unit,
        val onNavigateToNewGroupForm: () -> Unit,
        val onNavigateToContactSearch: () -> Unit,
        val onNewGroupClick: () -> Unit,
        val openImportContact: () -> Unit,
        val onSubscriptionUpgradeRequired: (String) -> Unit,
        val exitWithErrorMessage: (String) -> Unit
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
                onSubscriptionUpgradeRequired = {},
                exitWithErrorMessage = {}
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
