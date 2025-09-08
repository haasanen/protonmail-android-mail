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

package ch.protonmail.android.maillabel.presentation.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.component.ProtonSwitch
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.labelLargeInverted
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import ch.protonmail.android.uicomponents.BottomNavigationBarSpacer
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LabelAsBottomSheetContent(
    labelAsDataState: LabelAsState.Data,
    actions: LabelAsBottomSheetContent.Actions,
    modifier: Modifier = Modifier
) {

    var archiveSelectedState by remember { mutableStateOf(false) }

    ConsumableLaunchedEffect(labelAsDataState.shouldDismissEffect) {
        actions.onLabelAsComplete(archiveSelectedState, labelAsDataState.entryPoint)
    }

    ConsumableTextEffect(labelAsDataState.errorEffect) {
        actions.onError(it)
        actions.onDismiss()
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        TopBar(
            onClose = actions.onDismiss,
            onSaveClicked = { actions.onDoneClick(archiveSelectedState) }
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ProtonDimens.Spacing.Large
                )
        ) {
            AlsoArchiveRow(
                modifier = Modifier.fillMaxWidth(),
                archiveSelectedState = archiveSelectedState,
                onArchiveToggle = { archiveSelectedState = it }
            )
        }

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ProtonDimens.Spacing.Large
                )
                .verticalScroll(rememberScrollState())
        ) {

            LabelGroupWithActionButton(
                modifier = Modifier.fillMaxWidth(),
                labelUiModelsWithSelectedState = labelAsDataState.labelUiModels,
                actions = actions
            )

            BottomNavigationBarSpacer()
        }
    }
}

@Composable
private fun LabelGroupWithActionButton(
    modifier: Modifier = Modifier,
    labelUiModelsWithSelectedState: ImmutableList<LabelUiModelWithSelectedState>,
    actions: LabelAsBottomSheetContent.Actions
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            CreateLabelButton(
                onClick = actions.onCreateNewLabelClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(0.dp),
                thickness = 1.dp,
                color = ProtonTheme.colors.separatorNorm
            )

            labelUiModelsWithSelectedState.forEachIndexed { _, labelUiModelWithSelectedState ->
                SelectableLabelGroupItem(
                    labelUiModelWithSelectedState = labelUiModelWithSelectedState,
                    onClick = { actions.onLabelToggled(labelUiModelWithSelectedState.labelUiModel.id.labelId) }
                )
            }
        }
    }
}

@Composable
private fun SelectableLabelGroupItem(
    modifier: Modifier = Modifier,
    labelUiModelWithSelectedState: LabelUiModelWithSelectedState,
    onClick: () -> Unit
) {
    val backgroundColor = when (labelUiModelWithSelectedState.selectedState) {
        LabelSelectedState.PartiallySelected -> ProtonTheme.colors.interactionWeakPressed
        LabelSelectedState.Selected,
        LabelSelectedState.NotSelected -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .background(backgroundColor)
            .padding(
                vertical = ProtonDimens.Spacing.Large,
                horizontal = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(end = ProtonDimens.Spacing.Large),
            painter = painterResource(id = labelUiModelWithSelectedState.labelUiModel.icon),
            tint = labelUiModelWithSelectedState.labelUiModel.iconTint ?: Color.Unspecified,
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            modifier = Modifier
                .weight(1f),
            text = labelUiModelWithSelectedState.labelUiModel.text.string(),
            style = ProtonTheme.typography.bodyLargeNorm,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        SelectedStateIcon(
            selectedState = labelUiModelWithSelectedState.selectedState
        )
    }
}

@Composable
private fun TopBar(
    onClose: () -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ProtonTheme.colors.backgroundInvertedNorm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose
        ) {
            Icon(
                painter = painterResource(
                    id =
                    ch.protonmail.android.mailcommon.presentation.R.drawable.ic_proton_cross
                ),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm

            )
        }

        Text(
            text = stringResource(R.string.bottom_sheet_label_as_title),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.titleLargeNorm,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = onSaveClicked,
            modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Standard),
            shape = ProtonTheme.shapes.huge,
            colors = ButtonDefaults.buttonColors(
                containerColor = ProtonTheme.colors.interactionBrandDefaultNorm,
                disabledContainerColor = ProtonTheme.colors.interactionBrandWeakDisabled,
                contentColor = ProtonTheme.colors.textAccent,
                disabledContentColor = ProtonTheme.colors.brandMinus20
            ),
            contentPadding = PaddingValues(
                horizontal = ProtonDimens.Spacing.Large,
                vertical = ProtonDimens.Spacing.Standard
            )
        ) {
            Text(
                text = stringResource(R.string.bottom_sheet_save_action),
                style = ProtonTheme.typography.labelLargeInverted
            )
        }
    }
}

@Composable
private fun SelectedStateIcon(modifier: Modifier = Modifier, selectedState: LabelSelectedState) {

    when (selectedState) {
        LabelSelectedState.Selected -> Icon(
            modifier = modifier,
            painter = painterResource(id = R.drawable.ic_proton_checkmark),
            contentDescription = NO_CONTENT_DESCRIPTION,
            tint = ProtonTheme.colors.iconAccent
        )

        LabelSelectedState.PartiallySelected -> Icon(
            modifier = modifier,
            painter = painterResource(id = R.drawable.ic_proton_minus),
            contentDescription = NO_CONTENT_DESCRIPTION,
            tint = ProtonTheme.colors.iconAccent
        )

        LabelSelectedState.NotSelected -> Box(
            modifier = modifier
                .size(ProtonDimens.IconSize.Default)
        )
    }
}

@Composable
private fun CreateLabelButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                vertical = ProtonDimens.Spacing.Large,
                horizontal = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = ProtonDimens.Spacing.Large),
            painter = painterResource(id = R.drawable.ic_proton_plus),
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.label_title_create_label),
            style = ProtonTheme.typography.bodyLargeNorm,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AlsoArchiveRow(
    modifier: Modifier = Modifier,
    archiveSelectedState: Boolean,
    onArchiveToggle: (Boolean) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    vertical = ProtonDimens.Spacing.Large,
                    horizontal = ProtonDimens.Spacing.Large
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = ProtonDimens.Spacing.Large),
                painter = painterResource(id = R.drawable.ic_proton_archive_box),
                contentDescription = NO_CONTENT_DESCRIPTION
            )
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.bottom_sheet_archive_action),
                style = ProtonTheme.typography.bodyLargeNorm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            ProtonSwitch(
                modifier = Modifier.height(ProtonDimens.IconSize.Default),
                checked = archiveSelectedState,
                onCheckedChange = { onArchiveToggle(it) }
            )
        }
    }
}

internal object LabelAsBottomSheetContent {
    data class Actions(
        val onCreateNewLabelClick: () -> Unit,
        val onLabelToggled: (LabelId) -> Unit,
        val onDoneClick: (archiveSelected: Boolean) -> Unit,
        val onLabelAsComplete: (archiveSelected: Boolean, entryPoint: LabelAsBottomSheetEntryPoint) -> Unit,
        val onError: (String) -> Unit,
        val onDismiss: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onCreateNewLabelClick = {},
                onLabelToggled = {},
                onDoneClick = {},
                onLabelAsComplete = { _, _ -> },
                onError = {},
                onDismiss = {}
            )
        }
    }
}

object LabelAsBottomSheetTestTags {

    const val RootItem = "LabelAsBottomSheetRootItem"
}
