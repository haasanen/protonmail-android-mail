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

package ch.protonmail.android.maillabel.presentation.labellist

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.getColorFromHexString
import ch.protonmail.android.maillabel.presentation.previewdata.LabelListPreviewData.labelSampleData
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonSecondaryButton
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar

import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.uicomponents.thenIf

@Composable
fun LabelListScreen(actions: LabelListScreen.Actions, viewModel: LabelListViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle(viewModel.initialState).value

    Scaffold(
        topBar = {
            LabelListTopBar(
                actions = actions,
                onAddLabelClick = { viewModel.submit(LabelListViewAction.OnAddLabelClick) },
                isAddLabelButtonVisible = state is LabelListState.ListLoaded.Data
            )
        },
        content = { paddingValues ->
            when (state) {
                is LabelListState.ListLoaded.Data -> {
                    LabelListScreenContent(
                        state = state,
                        actions = actions,
                        paddingValues = paddingValues
                    )

                    ConsumableLaunchedEffect(effect = state.openLabelForm) {
                        actions.onAddLabelClick()
                    }
                }
                is LabelListState.ListLoaded.Empty -> {
                    EmptyLabelListScreen(
                        onAddLabelClick = { viewModel.submit(LabelListViewAction.OnAddLabelClick) },
                        paddingValues = paddingValues
                    )

                    ConsumableLaunchedEffect(effect = state.openLabelForm) {
                        actions.onAddLabelClick()
                    }
                }
                is LabelListState.Loading -> {
                    ProtonCenteredProgress(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )

                    ConsumableLaunchedEffect(effect = state.errorLoading) {
                        actions.onBackClick()
                        actions.showLabelListErrorLoadingSnackbar()
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabelListScreenContent(
    state: LabelListState.ListLoaded.Data,
    actions: LabelListScreen.Actions,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .padding(
                PaddingValues(
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    top = paddingValues.calculateTopPadding() + ProtonDimens.Spacing.Standard,
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = paddingValues.calculateBottomPadding()
                )
            )
            .fillMaxSize()
    ) {
        items(state.labels) { label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .clickable(
                        onClickLabel = stringResource(R.string.label_list_item_content_description),
                        role = Role.Button,
                        onClick = {
                            actions.onLabelSelected(label.labelId)
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hasLabelColor = label.color != null
                Box(
                    modifier = Modifier
                        .padding(MailDimens.ListItemCircleFilledPadding)
                        .size(MailDimens.ListItemCircleFilledSize)
                        .clip(CircleShape)
                        .thenIf(hasLabelColor) { this.background(label.color!!.getColorFromHexString()) }
                )
                Text(
                    text = label.name,
                    modifier = Modifier.padding(
                        start = ProtonDimens.Spacing.Small,
                        top = ProtonDimens.Spacing.Large,
                        end = ProtonDimens.Spacing.Large,
                        bottom = ProtonDimens.Spacing.Large
                    ),
                    style = ProtonTheme.typography.bodyLargeNorm
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun EmptyLabelListScreen(onAddLabelClick: () -> Unit, paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
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
            painter = painterResource(id = R.drawable.ic_proton_tag_plus),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            stringResource(R.string.label_list_no_labels_found),
            Modifier.padding(
                start = ProtonDimens.Spacing.Huge,
                top = ProtonDimens.Spacing.ExtraLarge,
                end = ProtonDimens.Spacing.Huge
            ),
            style = ProtonTheme.typography.titleMediumNorm
        )
        Text(
            stringResource(R.string.label_list_create_label_placeholder_description),
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
            onClick = onAddLabelClick
        ) {
            Text(
                text = stringResource(R.string.label_title_create_label),
                Modifier.padding(
                    horizontal = ProtonDimens.Spacing.Standard
                ),
                style = ProtonTheme.typography.bodySmallNorm
            )
        }
    }
}

@Composable
fun LabelListTopBar(
    actions: LabelListScreen.Actions,
    onAddLabelClick: () -> Unit,
    isAddLabelButtonVisible: Boolean
) {
    ProtonTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(text = stringResource(id = R.string.label_title_labels))
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
            if (isAddLabelButtonVisible) {
                IconButton(onClick = onAddLabelClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_proton_plus),
                        tint = ProtonTheme.colors.iconNorm,
                        contentDescription = stringResource(R.string.create_label_content_description)
                    )
                }
            }
        }
    )
}

object LabelListScreen {

    data class Actions(
        val onBackClick: () -> Unit,
        val onLabelSelected: (LabelId) -> Unit,
        val onAddLabelClick: () -> Unit,
        val showLabelListErrorLoadingSnackbar: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onBackClick = {},
                onLabelSelected = {},
                onAddLabelClick = {},
                showLabelListErrorLoadingSnackbar = {}
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun LabelListScreenPreview() {
    LabelListScreenContent(
        state = LabelListState.ListLoaded.Data(
            labels = listOf(
                labelSampleData,
                labelSampleData,
                labelSampleData
            )
        ),
        actions = LabelListScreen.Actions.Empty,
        paddingValues = PaddingValues()
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun EmptyLabelListScreenPreview() {
    EmptyLabelListScreen(
        onAddLabelClick = {},
        paddingValues = PaddingValues()
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun LabelListTopBarPreview() {
    LabelListTopBar(
        actions = LabelListScreen.Actions.Empty,
        onAddLabelClick = {},
        isAddLabelButtonVisible = true
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun EmptyLabelListTopBarPreview() {
    LabelListTopBar(
        actions = LabelListScreen.Actions.Empty,
        onAddLabelClick = {},
        isAddLabelButtonVisible = false
    )
}
