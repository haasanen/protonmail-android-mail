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

package ch.protonmail.android.testdata.maillabel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.protonmail.android.mailcommon.presentation.model.CappedNumberUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.presentation.MailLabelUiModel
import ch.protonmail.android.maillabel.presentation.iconRes
import ch.protonmail.android.maillabel.presentation.textRes
import ch.protonmail.android.testdata.label.rust.LabelAsActionsTestData
import kotlinx.collections.immutable.toImmutableList

object MailLabelUiModelTestData {

    val spamFolder = MailLabelUiModel.System(
        id = MailLabelTestData.spamSystemLabel.id,
        text = TextUiModel.TextRes(SystemLabelId.Spam.textRes()),
        icon = SystemLabelId.Spam.iconRes(),
        iconTint = null,
        isSelected = false,
        count = CappedNumberUiModel.Empty
    )
    val spamAndCustomFolder = listOf(
        spamFolder,
        MailLabelUiModel.Custom(
            id = MailLabelId.Custom.Folder(LabelId("folder1")),
            text = TextUiModel.Text("Folder1"),
            icon = R.drawable.ic_proton_folders_filled,
            iconTint = Color.Blue,
            isSelected = false,
            count = CappedNumberUiModel.Exact(1),
            isVisible = true,
            isExpanded = true,
            iconPaddingStart = 0.dp
        )
    ).toImmutableList()
    val spamAndCustomFolderWithSpamSelected = listOf(
        spamFolder.copy(isSelected = true),
        MailLabelUiModel.Custom(
            id = MailLabelId.Custom.Folder(LabelId("folder1")),
            text = TextUiModel.Text("Folder1"),
            icon = R.drawable.ic_proton_folders_filled,
            iconTint = Color.Blue,
            isSelected = false,
            count = CappedNumberUiModel.Exact(1),
            isVisible = true,
            isExpanded = true,
            iconPaddingStart = 0.dp
        )
    ).toImmutableList()
    val spamAndCustomFolderWithCustomSelected = listOf(
        spamFolder,
        MailLabelUiModel.Custom(
            id = MailLabelId.Custom.Folder(LabelId("folder1")),
            text = TextUiModel.Text("Folder1"),
            icon = R.drawable.ic_proton_folders_filled,
            iconTint = Color.Blue,
            isSelected = true,
            count = CappedNumberUiModel.Exact(1),
            isVisible = true,
            isExpanded = true,
            iconPaddingStart = 0.dp
        )
    ).toImmutableList()
    val systemAndTwoCustomFolders = listOf(
        spamFolder.copy(isSelected = true),
        MailLabelUiModel.Custom(
            id = MailLabelId.Custom.Folder(LabelId("folder1")),
            text = TextUiModel.Text("Folder1"),
            icon = R.drawable.ic_proton_folders_filled,
            iconTint = Color.Blue,
            isSelected = false,
            count = CappedNumberUiModel.Exact(1),
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
            count = CappedNumberUiModel.Exact(2),
            isVisible = true,
            isExpanded = true,
            iconPaddingStart = 0.dp
        )
    ).toImmutableList()
    val archiveAndCustomFolder = listOf(
        MailLabelUiModel.System(
            id = MailLabelTestData.archiveSystemLabel.id,
            text = TextUiModel.TextRes(SystemLabelId.Archive.textRes()),
            icon = SystemLabelId.Archive.iconRes(),
            iconTint = null,
            isSelected = false,
            count = CappedNumberUiModel.Empty
        ),
        MailLabelUiModel.Custom(
            id = MailLabelId.Custom.Folder(LabelId("folder1")),
            text = TextUiModel.Text("Folder1"),
            icon = R.drawable.ic_proton_folders_filled,
            iconTint = Color.Blue,
            isSelected = false,
            count = CappedNumberUiModel.Exact(1),
            isVisible = true,
            isExpanded = true,
            iconPaddingStart = 0.dp
        )
    ).toImmutableList()

    val customLabelList = MailLabelTestData.listOfCustomLabels.map {
        MailLabelUiModel.Custom(
            id = it.id,
            text = TextUiModel.Text(it.text),
            icon = R.drawable.ic_proton_circle_filled,
            iconTint = Color(it.color!!),
            isSelected = false,
            count = CappedNumberUiModel.Empty,
            isVisible = true,
            isExpanded = true,
            iconPaddingStart = 0.dp
        )
    }.toImmutableList()

    val customLabelForActions = LabelAsActionsTestData.actions.labels.map {
        MailLabelUiModel.Custom(
            id = MailLabelId.Custom.Folder(it.labelId),
            text = TextUiModel.Text(it.name),
            icon = R.drawable.ic_proton_circle_filled,
            iconTint = Color(0),
            isSelected = false,
            count = CappedNumberUiModel.Empty,
            isVisible = true,
            isExpanded = true,
            iconPaddingStart = 0.dp
        )
    }.toImmutableList()
}
