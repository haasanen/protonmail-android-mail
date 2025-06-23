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

package ch.protonmail.android.mailcontact.presentation.contactdetails.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonModalBottomSheetLayout
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.extension.copyTextToClipboard
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailcommon.presentation.ui.MailDivider
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.contactdetails.ContactDetailsViewModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.AvatarUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsItemGroupUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsItemType
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsState
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.QuickActionType
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.QuickActionUiModel
import ch.protonmail.android.mailcontact.presentation.previewdata.ContactDetailsPreviewData

@Composable
fun ContactDetailsScreen(
    actions: ContactDetailsScreen.Actions,
    viewModel: ContactDetailsViewModel = hiltViewModel<ContactDetailsViewModel>()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    ContactDetailsScreen(
        state = state,
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactDetailsScreen(
    state: ContactDetailsState,
    actions: ContactDetailsScreen.Actions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    ProtonModalBottomSheetLayout(
        showBottomSheet = showBottomSheet,
        sheetState = bottomSheetState,
        onDismissed = { showBottomSheet = false },
        dismissOnBack = true,
        sheetContent = {
            if (state is ContactDetailsState.Data) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    state.uiModel.contactDetailsItemGroupUiModels.flatMap { itemGroupUiModel ->
                        itemGroupUiModel.contactDetailsItemUiModels.filter { itemUiModel ->
                            itemUiModel.contactDetailsItemType == ContactDetailsItemType.Phone
                        }
                    }.forEach { uiModel ->
                        val phoneNumber = uiModel.value.string()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = ProtonDimens.ListItemHeight)
                                .clickable(
                                    enabled = true,
                                    onClick = { launchPhoneApp(context, phoneNumber) }
                                )
                                .padding(horizontal = ProtonDimens.Spacing.Large),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.contact_details_action_call_label) + " \"" +
                                    uiModel.label.string() + "\" " + uiModel.value.string(),
                                style = ProtonTheme.typography.bodyLargeNorm
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = ProtonTheme.colors.backgroundInvertedNorm,
            topBar = { ContactDetailsTopBar(state, actions.onBack, actions.showFeatureMissingSnackbar) }
        ) {
            when (state) {
                is ContactDetailsState.Data -> ContactDetails(
                    uiModel = state.uiModel,
                    actions = actions,
                    onCallQuickAction = { showBottomSheet = true },
                    modifier = Modifier.padding(it)
                )

                is ContactDetailsState.Error -> ContactDetailsError(
                    onBack = actions.onBack,
                    onShowErrorSnackbar = actions.onShowErrorSnackbar
                )

                is ContactDetailsState.Loading -> ProtonCenteredProgress(
                    modifier = Modifier.padding(it)
                )
            }
        }
    }
}

@Composable
private fun ContactDetails(
    uiModel: ContactDetailsUiModel,
    actions: ContactDetailsScreen.Actions,
    onCallQuickAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ProtonDimens.Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (val avatarUiModel = uiModel.avatarUiModel) {
            is AvatarUiModel.Initials -> InitialsContactAvatar(
                initials = avatarUiModel.value,
                color = avatarUiModel.color
            )
            is AvatarUiModel.Photo -> ImageContactAvatar(
                imageBitmap = avatarUiModel.bitmap
            )
        }

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
        Text(
            text = uiModel.headerUiModel.displayName,
            style = ProtonTheme.typography.titleLargeNorm
        )
        uiModel.headerUiModel.displayEmailAddress?.let {
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
            Text(
                text = it,
                style = ProtonTheme.typography.bodyMediumWeak
            )
        }
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))

        ContactDetailsQuickActions(
            quickActionUiModels = uiModel.quickActionUiModels,
            actions = ContactDetailsScreen.QuickActions(
                onMessageContact = { uiModel.headerUiModel.displayEmailAddress?.let { actions.onMessageContact(it) } },
                onCallQuickAction = onCallQuickAction,
                showFeatureMissingSnackbar = actions.showFeatureMissingSnackbar
            )
        )

        uiModel.contactDetailsItemGroupUiModels.forEachIndexed { index, groupUiModel ->
            ContactDetailsItemGroup(itemGroupUiModel = groupUiModel, onMessageContact = actions.onMessageContact)
            if (index != uiModel.contactDetailsItemGroupUiModels.size - 1) {
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            }
        }
    }
}

@Composable
private fun ContactDetailsTopBar(
    state: ContactDetailsState,
    onBack: () -> Unit,
    showFeatureMissingSnackbar: () -> Unit
) {
    ProtonTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = ProtonTheme.colors.backgroundInvertedNorm,
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    tint = ProtonTheme.colors.iconNorm,
                    contentDescription = stringResource(id = R.string.presentation_back)
                )
            }
        },
        actions = {
            if (state is ContactDetailsState.Data) {
                IconButton(onClick = showFeatureMissingSnackbar) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_proton_pen),
                        tint = ProtonTheme.colors.iconNorm,
                        contentDescription = stringResource(
                            id = R.string.contact_details_edit_contact_content_description
                        )
                    )
                }
                IconButton(onClick = showFeatureMissingSnackbar) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_proton_trash),
                        tint = ProtonTheme.colors.iconNorm,
                        contentDescription = stringResource(
                            id = R.string.contact_details_delete_contact_content_description
                        )
                    )
                }
            }
        }
    )
}

@Composable
private fun ContactDetailsQuickActions(
    quickActionUiModels: List<QuickActionUiModel>,
    actions: ContactDetailsScreen.QuickActions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ProtonDimens.Spacing.Large)
            .background(color = ProtonTheme.colors.backgroundInvertedNorm)
    ) {
        quickActionUiModels.forEachIndexed { index, uiModel ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = ProtonTheme.colors.backgroundInvertedSecondary,
                        shape = ProtonTheme.shapes.extraLarge
                    )
                    .clip(ProtonTheme.shapes.extraLarge)
                    .clickable(
                        enabled = uiModel.isEnabled,
                        role = Role.Button,
                        onClick = {
                            when (uiModel.quickActionType) {
                                QuickActionType.Message -> actions.onMessageContact()
                                QuickActionType.Call -> actions.onCallQuickAction()
                                QuickActionType.Share -> actions.showFeatureMissingSnackbar()
                            }
                        }
                    )
                    .padding(ProtonDimens.Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = uiModel.icon),
                    tint = if (uiModel.isEnabled) ProtonTheme.colors.iconNorm else ProtonTheme.colors.iconDisabled,
                    contentDescription = NO_CONTENT_DESCRIPTION
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
                Text(
                    text = stringResource(id = uiModel.label),
                    style = ProtonTheme.typography.bodyMedium.copy(
                        color = if (uiModel.isEnabled) ProtonTheme.colors.textWeak else ProtonTheme.colors.textDisabled
                    )
                )
            }
            if (index != quickActionUiModels.size - 1) {
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
            }
        }
    }
}

@Composable
private fun ContactDetailsItemGroup(
    itemGroupUiModel: ContactDetailsItemGroupUiModel,
    onMessageContact: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
            .clip(ProtonTheme.shapes.extraLarge)
    ) {
        itemGroupUiModel.contactDetailsItemUiModels.forEachIndexed { index, uiModel ->
            val context = LocalContext.current
            val contactItemLabel = uiModel.label.string()
            val contactItemValue = uiModel.value.string()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onLongClickLabel = stringResource(id = R.string.contact_details_action_copy_label),
                        onLongClick = { context.copyTextToClipboard(contactItemLabel, contactItemValue) },
                        onClickLabel = when (uiModel.contactDetailsItemType) {
                            ContactDetailsItemType.Email -> stringResource(
                                id = R.string.contact_details_action_message_label
                            )

                            ContactDetailsItemType.Phone -> stringResource(
                                id = R.string.contact_details_action_call_label
                            )

                            ContactDetailsItemType.Other -> null
                        },
                        onClick = {
                            when (uiModel.contactDetailsItemType) {
                                ContactDetailsItemType.Email -> onMessageContact(contactItemValue)
                                ContactDetailsItemType.Phone -> launchPhoneApp(context, contactItemValue)
                                ContactDetailsItemType.Other -> Unit
                            }
                        }
                    )
                    .padding(ProtonDimens.Spacing.Large)
            ) {
                Text(
                    text = uiModel.label.string(),
                    style = ProtonTheme.typography.bodyMedium.copy(
                        color = ProtonTheme.colors.textHint
                    )
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))
                Text(
                    text = uiModel.value.string(),
                    style = ProtonTheme.typography.bodyLarge.copy(
                        color = if (uiModel.contactDetailsItemType == ContactDetailsItemType.Other) {
                            ProtonTheme.colors.textNorm
                        } else {
                            ProtonTheme.colors.textAccent
                        }
                    )
                )
            }
            if (index != itemGroupUiModel.contactDetailsItemUiModels.size - 1) {
                MailDivider()
            }
        }
    }
}

@Composable
private fun ContactDetailsError(onBack: () -> Unit, onShowErrorSnackbar: (String) -> Unit) {
    val errorMessage = stringResource(id = R.string.contact_details_loading_error)

    LaunchedEffect(Unit) {
        onShowErrorSnackbar(errorMessage)
        onBack()
    }
}

private fun launchPhoneApp(context: Context, phoneNumber: String) {
    val uri = "tel:$phoneNumber".toUri()
    val intent = Intent(Intent.ACTION_DIAL, uri)
    context.startActivity(intent)
}

@Preview
@Composable
private fun ContactDetailsScreenPreview() {
    ContactDetailsScreen(
        state = ContactDetailsPreviewData.contactDetailsState,
        actions = ContactDetailsScreen.Actions(
            onBack = {},
            onShowErrorSnackbar = {},
            onMessageContact = {},
            showFeatureMissingSnackbar = {}
        )
    )
}

object ContactDetailsScreen {
    const val CONTACT_DETAILS_ID_KEY = "ContactDetailsIdKey"

    data class Actions(
        val onBack: () -> Unit,
        val onShowErrorSnackbar: (String) -> Unit,
        val onMessageContact: (String) -> Unit,
        val showFeatureMissingSnackbar: () -> Unit
    )

    data class QuickActions(
        val onMessageContact: () -> Unit,
        val onCallQuickAction: () -> Unit,
        val showFeatureMissingSnackbar: () -> Unit
    )
}
