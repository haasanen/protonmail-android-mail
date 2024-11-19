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

package ch.protonmail.android.maillabel.presentation.folderlist

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.previewdata.FolderListPreviewData.folderUiModelSampleData
import ch.protonmail.android.mailupselling.presentation.model.BottomSheetVisibilityEffect
import kotlinx.coroutines.launch
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonSecondaryButton
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleMediumNorm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(actions: FolderListScreen.Actions, viewModel: FolderListViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle(viewModel.initialState).value
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (bottomSheetState.currentValue != SheetValue.Hidden) {
        DisposableEffect(Unit) { onDispose { viewModel.submit(FolderListViewAction.OnDismissSettings) } }
    }

    BackHandler(bottomSheetState.isVisible) {
        viewModel.submit(FolderListViewAction.OnDismissSettings)
    }

    Scaffold(
        topBar = {
            FolderListTopBar(
                actions = FolderListTopBar.Actions(
                    onBackClick = actions.onBackClick,
                    onAddFolderClick = { viewModel.submit(FolderListViewAction.OnAddFolderClick) },
                    onFolderSettingsClick = { viewModel.submit(FolderListViewAction.OnOpenSettingsClick) }
                ),
                isAddFolderButtonVisible = state is FolderListState.ListLoaded.Data,
                isSettingsButtonVisible = state is FolderListState.ListLoaded
            )
        },
        content = { paddingValues ->
            if (state is FolderListState.ListLoaded) {
                ConsumableLaunchedEffect(effect = state.openFolderForm) {
                    actions.onAddFolderClick()
                }
                ConsumableLaunchedEffect(effect = state.bottomSheetVisibilityEffect) {
                    when (it) {
                        BottomSheetVisibilityEffect.Hide -> scope.launch { bottomSheetState.hide() }
                        BottomSheetVisibilityEffect.Show -> scope.launch { bottomSheetState.show() }
                    }
                }
            }
            when (state) {
                is FolderListState.ListLoaded.Data -> {
                    FolderListScreenContent(
                        modifier = Modifier.padding(
                            PaddingValues(
                                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                                top = paddingValues.calculateTopPadding() + ProtonDimens.Spacing.Standard,
                                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                                bottom = paddingValues.calculateBottomPadding()
                            )
                        ),
                        state = state,
                        actions = actions
                    )
                }

                is FolderListState.ListLoaded.Empty -> {
                    EmptyFolderListScreen(
                        modifier = Modifier.padding(paddingValues),
                        onAddFolderClick = { viewModel.submit(FolderListViewAction.OnAddFolderClick) }
                    )
                }

                is FolderListState.Loading -> {
                    ProtonCenteredProgress(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )

                    ConsumableTextEffect(effect = state.errorLoading) { message ->
                        actions.exitWithErrorMessage(message)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderListScreenContent(
    modifier: Modifier = Modifier,
    state: FolderListState.ListLoaded.Data,
    actions: FolderListScreen.Actions
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(state.folders) { index, folder ->
            if (index != 0 && folder.parent == null) {
                HorizontalDivider()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            actions.onFolderSelected(folder.id)
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(
                        start = ProtonDimens.Spacing.Large.times(folder.level.plus(1)),
                        end = ProtonDimens.Spacing.Large
                    ),
                    painter = painterResource(id = folder.icon),
                    tint = folder.color ?: ProtonTheme.colors.iconNorm,
                    contentDescription = NO_CONTENT_DESCRIPTION
                )
                Text(
                    text = folder.name,
                    modifier = Modifier.padding(
                        start = ProtonDimens.Spacing.Small,
                        top = ProtonDimens.Spacing.Large,
                        end = ProtonDimens.Spacing.Large,
                        bottom = ProtonDimens.Spacing.Large
                    ),
                    style = ProtonTheme.typography.bodyLargeNorm
                )
            }
        }
    }
}

@Composable
fun EmptyFolderListScreen(modifier: Modifier = Modifier, onAddFolderClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier
                .padding(start = ProtonDimens.Spacing.Small)
                .background(
                    color = ProtonTheme.colors.backgroundSecondary,
                    shape = RoundedCornerShape(MailDimens.IconWeakRoundBackgroundRadius)
                )
                .padding(ProtonDimens.Spacing.Standard),
            painter = painterResource(id = R.drawable.ic_proton_folder_plus),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            stringResource(R.string.folder_list_no_folders_found),
            Modifier.padding(
                start = ProtonDimens.Spacing.Huge,
                top = ProtonDimens.Spacing.ExtraLarge,
                end = ProtonDimens.Spacing.Huge
            ),
            style = ProtonTheme.typography.titleMediumNorm
        )
        Text(
            stringResource(R.string.folder_list_create_folder_placeholder_description),
            Modifier.padding(
                start = ProtonDimens.Spacing.Huge,
                top = ProtonDimens.Spacing.Tiny,
                end = ProtonDimens.Spacing.Huge
            ),
            style = ProtonTheme.typography.bodyMediumWeak,
            textAlign = TextAlign.Center
        )
        ProtonSecondaryButton(
            modifier = Modifier.padding(top = ProtonDimens.Spacing.Huge),
            onClick = onAddFolderClick
        ) {
            Text(
                text = stringResource(R.string.label_title_create_folder),
                Modifier.padding(
                    horizontal = ProtonDimens.Spacing.Standard
                ),
                style = ProtonTheme.typography.bodySmallNorm
            )
        }
    }
}

@Composable
fun FolderListTopBar(
    modifier: Modifier = Modifier,
    actions: FolderListTopBar.Actions,
    isAddFolderButtonVisible: Boolean,
    isSettingsButtonVisible: Boolean
) {
    ProtonTopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(text = stringResource(id = R.string.label_title_folders))
        },
        navigationIcon = {
            IconButton(onClick = actions.onBackClick) {
                Icon(
                    tint = ProtonTheme.colors.iconNorm,
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.presentation_back)
                )
            }
        },
        actions = {
            if (isAddFolderButtonVisible) {
                IconButton(onClick = actions.onAddFolderClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_proton_plus),
                        tint = ProtonTheme.colors.iconNorm,
                        contentDescription = stringResource(R.string.create_folder_content_description)
                    )
                }
            }
            if (isSettingsButtonVisible) {
                IconButton(onClick = actions.onFolderSettingsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_proton_cog_wheel),
                        tint = ProtonTheme.colors.iconNorm,
                        contentDescription = stringResource(R.string.folder_settings_content_description)
                    )
                }
            }
        }
    )
}

object FolderListScreen {

    data class Actions(
        val onBackClick: () -> Unit,
        val onFolderSelected: (LabelId) -> Unit,
        val onAddFolderClick: () -> Unit,
        val exitWithErrorMessage: (String) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onBackClick = {},
                onFolderSelected = {},
                onAddFolderClick = {},
                exitWithErrorMessage = {}
            )
        }
    }
}

object FolderSettingsScreen {

    data class Actions(
        val onChangeUseFolderColor: (Boolean) -> Unit,
        val onChangeInheritParentFolderColor: (Boolean) -> Unit,
        val onDoneClick: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onChangeUseFolderColor = {},
                onChangeInheritParentFolderColor = {},
                onDoneClick = {}
            )
        }
    }
}

object FolderListTopBar {

    data class Actions(
        val onBackClick: () -> Unit,
        val onAddFolderClick: () -> Unit,
        val onFolderSettingsClick: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onBackClick = {},
                onAddFolderClick = {},
                onFolderSettingsClick = {}
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun FolderListScreenPreview() {
    FolderListScreenContent(
        state = FolderListState.ListLoaded.Data(
            folders = listOf(
                folderUiModelSampleData,
                folderUiModelSampleData,
                folderUiModelSampleData
            )
        ),
        actions = FolderListScreen.Actions.Empty
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun EmptyFolderListScreenPreview() {
    EmptyFolderListScreen(
        onAddFolderClick = {}
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun FolderListTopBarPreview() {
    FolderListTopBar(
        actions = FolderListTopBar.Actions.Empty,
        isAddFolderButtonVisible = true,
        isSettingsButtonVisible = true
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun EmptyFolderListTopBarPreview() {
    FolderListTopBar(
        actions = FolderListTopBar.Actions.Empty,
        isAddFolderButtonVisible = false,
        isSettingsButtonVisible = true
    )
}
