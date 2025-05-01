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

@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package ch.protonmail.android.mailcontact.presentation.contactsearch

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.contactlist.ui.ContactListGroupItem
import ch.protonmail.android.mailcontact.presentation.contactlist.ui.ContactListItem
import ch.protonmail.android.mailcontact.presentation.contactlist.ui.ContactListScreen
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.previewdata.ContactListPreviewData
import ch.protonmail.android.uicomponents.SearchView
import ch.protonmail.android.uicomponents.dismissKeyboard

@Composable
fun ContactSearchScreen(actions: ContactSearchScreen.Actions, viewModel: ContactSearchViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val state = viewModel.state.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            ContactSearchTopBar(
                Modifier,
                actions = actions,
                state = state,
                onSearchValueChange = {
                    viewModel.submit(ContactSearchViewAction.OnSearchValueChanged(it))
                },
                onSearchValueClear = {
                    viewModel.submit(ContactSearchViewAction.OnSearchValueCleared)
                }
            )
        },
        contentWindowInsets = WindowInsets(
            left = ProtonDimens.Spacing.Large,
            right = ProtonDimens.Spacing.Large
        ),
        content = { paddingValues ->
            ContactSearchContent(
                state = state,
                actions = ContactSearchContent.Actions(
                    onContactClick = {
                        actions.onContactSelected(it)
                    },
                    onContactGroupClick = {
                        actions.onContactGroupSelected(it)
                    }
                ),
                paddingValues = paddingValues
            )
        }
    )

    ConsumableLaunchedEffect(effect = state.close) {
        dismissKeyboard(context, view, keyboardController)
        actions.onClose
    }
}

@Composable
fun ContactSearchContent(
    modifier: Modifier = Modifier,
    state: ContactSearchState,
    actions: ContactSearchContent.Actions,
    paddingValues: PaddingValues
) {

    if (state.contactUiModels?.isEmpty() == true) {
        NoResultsContent()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = paddingValues
    ) {
        state.contactUiModels?.let {
            items(state.contactUiModels) { contact ->
                when (contact) {
                    is ContactListItemUiModel.Contact -> {
                        ContactListItem(
                            modifier = Modifier.animateItem(),
                            contact = contact,
                            actions = ContactListScreen.Actions.fromContactSearchActions(
                                onContactClick = actions.onContactClick
                            )
                        )
                    }

                    is ContactListItemUiModel.ContactGroup -> {
                        ContactListGroupItem(
                            modifier = Modifier.animateItem(),
                            contactGroup = contact,
                            actions = ContactListScreen.Actions.fromContactSearchActions(
                                onContactGroupClick = actions.onContactGroupClick
                            )
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun NoResultsContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.contact_search_no_results),
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.bodyMediumWeak
        )
    }
}

@Composable
fun ContactSearchTopBar(
    modifier: Modifier,
    actions: ContactSearchScreen.Actions,
    state: ContactSearchState,
    onSearchValueChange: (String) -> Unit,
    onSearchValueClear: () -> Unit
) {

    ProtonTopAppBar(
        modifier = modifier,
        backgroundColor = ProtonTheme.colors.backgroundSecondary,
        title = {
            SearchView(
                SearchView.Parameters(
                    initialSearchValue = state.searchValue,
                    searchPlaceholderText = R.string.contact_search_placeholder,
                    closeButtonContentDescription = R.string.contact_search_content_description
                ),
                modifier = Modifier
                    .padding(end = ProtonDimens.Spacing.Small)
                    .fillMaxWidth(),
                actions = SearchView.Actions(
                    onClearSearchQuery = { onSearchValueClear() },
                    onSearchQuerySubmit = {},
                    onSearchQueryChanged = { onSearchValueChange(it) }
                ),
                backgroundColor = ProtonTheme.colors.backgroundSecondary
            )

        },
        navigationIcon = {
            IconButton(
                modifier = Modifier,
                onClick = actions.onClose
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.contact_search_arrow_back_content_description)
                )
            }
        },
        minHeight = MailDimens.Contacts.SearchTopBarHeight
    )
}

object ContactSearchScreen {

    data class Actions(
        val onContactSelected: (ContactId) -> Unit,
        val onContactGroupSelected: (ContactGroupId) -> Unit,
        val onClose: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onContactSelected = { },
                onContactGroupSelected = { },
                onClose = {}
            )
        }
    }
}

object ContactSearchContent {

    data class Actions(
        val onContactClick: (ContactId) -> Unit,
        val onContactGroupClick: (ContactGroupId) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onContactClick = {},
                onContactGroupClick = {}
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun ManageMembersContentPreview() {
    ContactSearchContent(
        state = ContactSearchState(
            contactUiModels = emptyList()
        ),
        actions = ContactSearchContent.Actions.Empty,
        paddingValues = PaddingValues()
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun ContactSearchContentPreview() {
    ContactSearchContent(
        state = ContactSearchState(
            contactUiModels = listOf(
                ContactListPreviewData.contactSampleData,
                ContactListPreviewData.contactGroupSampleData,
                ContactListPreviewData.contactSampleData,
                ContactListPreviewData.contactSampleData,
                ContactListPreviewData.contactSampleData,
                ContactListPreviewData.contactSampleData
            )
        ),
        actions = ContactSearchContent.Actions.Empty,
        paddingValues = PaddingValues()
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun ManageMembersTopBarPreview() {
    ContactSearchTopBar(
        Modifier,
        actions = ContactSearchScreen.Actions.Empty,
        state = ContactSearchState(),
        onSearchValueChange = {},
        onSearchValueClear = {}
    )
}
