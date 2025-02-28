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

package ch.protonmail.android.mailmessage.presentation.ui.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.presentation.MailLabelUiModel
import ch.protonmail.android.mailmessage.presentation.R
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MoveToBottomSheetState
import kotlinx.collections.immutable.toImmutableList
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeWeak
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.presentation.iconRes
import ch.protonmail.android.maillabel.presentation.textRes
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MoveToBottomSheetEntryPoint

@Composable
fun MoveToBottomSheetContent(state: MoveToBottomSheetState, actions: MoveToBottomSheetContent.Actions) {
    when (state) {
        is MoveToBottomSheetState.Data -> MoveToBottomSheetContent(state, actions)
        else -> ProtonCenteredProgress()
    }
}

@Composable
fun MoveToBottomSheetContent(dataState: MoveToBottomSheetState.Data, actions: MoveToBottomSheetContent.Actions) {
    val customDestinations = dataState.moveToDestinations.filterIsInstance<MailLabelUiModel.Custom>()
    val systemDestinations = dataState.moveToDestinations.filterIsInstance<MailLabelUiModel.System>()
    val entryPoint = dataState.entryPoint

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MoveToBottomSheetTestTags.RootItem)
    ) {
        MoveToSheetTitle()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ProtonDimens.Spacing.Large,
                    vertical = ProtonDimens.Spacing.Large
                )
                .verticalScroll(rememberScrollState())
        ) {
            CustomMoveToGroupWithActionButton(
                destinations = customDestinations,
                onFolderSelected = { folderId, folderName ->
                    actions.onFolderSelected(folderId, folderName, entryPoint)
                },
                onAddClick = actions.onAddFolderClick
            )

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

            MoveToGroup(
                destinations = systemDestinations,
                onFolderSelected = { folderId, folderName ->
                    actions.onFolderSelected(folderId, folderName, entryPoint)
                }
            )
        }
    }
}

@Composable
private fun MoveToSheetTitle() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ProtonDimens.Spacing.Small,
                vertical = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .testTag(MoveToBottomSheetTestTags.MoveToText)
                .weight(1f),
            text = stringResource(id = R.string.bottom_sheet_move_to_title),
            style = ProtonTheme.typography.titleLargeNorm,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MoveToGroup(
    modifier: Modifier = Modifier,
    destinations: List<MailLabelUiModel>,
    onFolderSelected: (MailLabelId, String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            destinations.forEachIndexed { index, label ->
                MoveToGroupItem(
                    label = label,
                    onFolderClicked = { folderName -> onFolderSelected(label.id, folderName) }
                )
                if (index < destinations.lastIndex) {
                    HorizontalDivider(
                        thickness = MailDimens.DefaultBorder,
                        color = ProtonTheme.colors.separatorNorm
                    )
                }
            }
        }
    }
}

@Composable
fun CustomMoveToGroupWithActionButton(
    modifier: Modifier = Modifier,
    destinations: List<MailLabelUiModel.Custom>,
    onFolderSelected: (MailLabelId, String) -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            destinations.forEachIndexed { _, label ->
                MoveToGroupItem(
                    label = label,
                    onFolderClicked = { folderName ->
                        onFolderSelected(label.id, folderName)
                    }
                )
                HorizontalDivider(
                    thickness = MailDimens.DefaultBorder,
                    color = ProtonTheme.colors.separatorNorm
                )
            }
            CreateFolderButton(onClick = onAddClick)
        }
    }
}

@Composable
internal fun MoveToGroupItem(
    modifier: Modifier = Modifier,
    label: MailLabelUiModel,
    onFolderClicked: (String) -> Unit
) {
    val iconPaddingStart = (label as? MailLabelUiModel.Custom)?.iconPaddingStart ?: 0.dp
    val folderName = label.text.string()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = {
                onFolderClicked(folderName)
            })
            .padding(
                vertical = ProtonDimens.Spacing.Large,
                horizontal = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(
                    start = iconPaddingStart,
                    end = ProtonDimens.Spacing.Large
                ),
            painter = painterResource(id = label.icon),
            tint = label.iconTint ?: Color.Unspecified,
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            modifier = Modifier.weight(1f),
            text = folderName,
            style = ProtonTheme.typography.bodyLargeWeak,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (label.isSelected) {
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_checkmark),
                contentDescription = NO_CONTENT_DESCRIPTION,
                tint = ProtonTheme.colors.iconAccent
            )
        }
    }
}

@Composable
private fun CreateFolderButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .testTag(MoveToBottomSheetTestTags.AddFolderRow)
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                vertical = ProtonDimens.Spacing.Large,
                horizontal = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .testTag(MoveToBottomSheetTestTags.AddFolderIcon)
                .padding(end = ProtonDimens.Spacing.Large),
            painter = painterResource(id = R.drawable.ic_proton_plus),
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            modifier = Modifier
                .testTag(MoveToBottomSheetTestTags.AddFolderText)
                .weight(1f),
            text = stringResource(id = R.string.label_title_create_folder),
            style = ProtonTheme.typography.bodyLargeWeak,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


object MoveToBottomSheetContent {

    data class Actions(
        val onAddFolderClick: () -> Unit,
        val onFolderSelected: (MailLabelId, String, MoveToBottomSheetEntryPoint) -> Unit,
        val onDismiss: () -> Unit
    )
}

@Preview(showBackground = true)
@Composable
fun MoveToBottomSheetContentPreview() {
    MoveToBottomSheetContent(
        dataState = MoveToBottomSheetState.Data(
            moveToDestinations = listOf(
                MailLabelUiModel.Custom(
                    id = MailLabelId.Custom.Folder(LabelId("folder1")),
                    text = TextUiModel.Text("Folder1"),
                    icon = R.drawable.ic_proton_folders_filled,
                    iconTint = Color.Blue,
                    isSelected = false,
                    count = 1,
                    isVisible = true,
                    isExpanded = true,
                    iconPaddingStart = 0.dp
                ),
                MailLabelUiModel.Custom(
                    id = MailLabelId.Custom.Folder(LabelId("folder2")),
                    text = TextUiModel.Text("Folder2"),
                    icon = R.drawable.ic_proton_folder_filled,
                    iconTint = Color.Red,
                    isSelected = true,
                    count = 2,
                    isVisible = true,
                    isExpanded = true,
                    iconPaddingStart = ProtonDimens.Spacing.Large * 1
                ),
                MailLabelUiModel.Custom(
                    id = MailLabelId.Custom.Folder(LabelId("folder3")),
                    text = TextUiModel.Text("Folder3"),
                    icon = R.drawable.ic_proton_folder_filled,
                    iconTint = Color.Yellow,
                    isSelected = false,
                    count = null,
                    isVisible = true,
                    isExpanded = true,
                    iconPaddingStart = ProtonDimens.Spacing.Large * 2
                ),
                MailLabelUiModel.Custom(
                    id = MailLabelId.Custom.Folder(LabelId("really long folder name")),
                    text = TextUiModel.Text("THis folder is really long so that truncation can be tested"),
                    icon = R.drawable.ic_proton_folders_filled,
                    iconTint = Color.Blue,
                    isSelected = true,
                    count = 1,
                    isVisible = true,
                    isExpanded = true,
                    iconPaddingStart = 0.dp
                ),
                MailLabelUiModel.System(
                    id = MailLabelId.System(LabelId("inbox")),
                    text = TextUiModel.TextRes(SystemLabelId.Inbox.textRes()),
                    icon = SystemLabelId.Inbox.iconRes(),
                    iconTint = null,
                    isSelected = false,
                    count = null
                ),
                MailLabelUiModel.System(
                    id = MailLabelId.System(LabelId("spam")),
                    text = TextUiModel.TextRes(SystemLabelId.Spam.textRes()),
                    icon = SystemLabelId.Spam.iconRes(),
                    iconTint = null,
                    isSelected = false,
                    count = null
                ),
                MailLabelUiModel.System(
                    id = MailLabelId.System(LabelId("trash")),
                    text = TextUiModel.TextRes(SystemLabelId.Trash.textRes()),
                    icon = SystemLabelId.Trash.iconRes(),
                    iconTint = null,
                    isSelected = false,
                    count = null
                ),
                MailLabelUiModel.System(
                    id = MailLabelId.System(LabelId("archive")),
                    text = TextUiModel.TextRes(SystemLabelId.Archive.textRes()),
                    icon = SystemLabelId.Archive.iconRes(),
                    iconTint = null,
                    isSelected = false,
                    count = null
                )
            ).toImmutableList(),
            entryPoint = MoveToBottomSheetEntryPoint.Conversation
        ),
        actions = MoveToBottomSheetContent.Actions(
            onAddFolderClick = {},
            onFolderSelected = { _, _, _ -> },
            onDismiss = {}
        )
    )
}

object MoveToBottomSheetTestTags {

    const val RootItem = "MoveToBottomSheetRootItem"
    const val MoveToText = "MoveToText"
    const val DoneButton = "DoneButton"
    const val FolderItem = "FolderItem"
    const val FolderIcon = "FolderIcon"
    const val FolderNameText = "FolderNameText"
    const val FolderSelectionIcon = "FolderSelectionIcon"
    const val AddFolderRow = "AddFolderRow"
    const val AddFolderIcon = "AddFolderIcon"
    const val AddFolderText = "AddFolderText"
}
