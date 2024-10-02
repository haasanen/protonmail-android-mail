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

package ch.protonmail.android.maillabel.presentation

import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.presentation.model.toFolderUiModel
import ch.protonmail.android.testdata.folder.FolderTestData
import ch.protonmail.android.testdata.label.LabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class FolderUiModelMapperTest {

    private val colorMapper = ColorMapper()

    private val userId = UserIdTestData.userId

    @Test
    fun `return correct folders with parent`() = runTest {
        // Given
        val noFolderColor = null
        val items = listOf(
            LabelTestData.buildLabel(
                id = "0",
                type = LabelType.MessageFolder,
                order = 0,
                color = noFolderColor
            ),
            LabelTestData.buildLabel(
                id = "0.1",
                type = LabelType.MessageFolder,
                order = 0,
                color = noFolderColor,
                parentId = "0"
            ),
            LabelTestData.buildLabel(
                id = "0.2",
                type = LabelType.MessageFolder,
                order = 1,
                color = noFolderColor,
                parentId = "0"
            ),
            LabelTestData.buildLabel(
                id = "0.2.1",
                type = LabelType.MessageFolder,
                order = 0,
                color = noFolderColor,
                parentId = "0.2"
            ),
            LabelTestData.buildLabel(
                id = "0.2.2",
                type = LabelType.MessageFolder,
                order = 1,
                color = noFolderColor,
                parentId = "0.2"
            )
        )
        val labelColor = colorMapper.toColor(items.first().color).getOrNull()

        // When
        val actual = items.toFolderUiModel(colorMapper)

        // Then
        val f0 = FolderTestData.buildFolderUiModel(
            id = LabelId("0"), color = labelColor, level = 0,
            order = 0,
            children = listOf(LabelId("0.1"), LabelId("0.2")), icon = R.drawable.ic_proton_folders
        )
        val f01 = FolderTestData.buildFolderUiModel(
            id = LabelId("0.1"), parent = f0, color = labelColor, level = 1,
            order = 0, icon = R.drawable.ic_proton_folder
        )
        val f02 = FolderTestData.buildFolderUiModel(
            id = LabelId("0.2"), parent = f0, color = labelColor, level = 1,
            order = 1,
            children = listOf(LabelId("0.2.1"), LabelId("0.2.2")), icon = R.drawable.ic_proton_folders
        )
        val f021 = FolderTestData.buildFolderUiModel(
            id = LabelId("0.2.1"), parent = f02, color = labelColor, level = 2,
            order = 0, icon = R.drawable.ic_proton_folder
        )
        val f022 = FolderTestData.buildFolderUiModel(
            id = LabelId("0.2.2"), parent = f02, color = labelColor, level = 2,
            order = 1, icon = R.drawable.ic_proton_folder
        )
        val expected = listOf(f0, f01, f02, f021, f022)

        assertEquals(expected, actual)
    }

    @Test
    fun `when a parent does not exist, ignore its children`() = runTest {
        // Given
        val noFolderColor = null
        val items = listOf(
            LabelTestData.buildLabel(
                id = "0",
                type = LabelType.MessageFolder,
                order = 0,
                color = noFolderColor
            ),
            LabelTestData.buildLabel(
                id = "0.1",
                type = LabelType.MessageFolder,
                order = 0,
                color = noFolderColor,
                parentId = "0"
            ),
            LabelTestData.buildLabel(
                id = "0.2.1",
                type = LabelType.MessageFolder,
                order = 0,
                color = noFolderColor,
                parentId = "0.2"
            ),
            LabelTestData.buildLabel(
                id = "0.2.2",
                type = LabelType.MessageFolder,
                order = 1,
                color = noFolderColor,
                parentId = "0.2"
            )
        )
        val labelColor = colorMapper.toColor(items.first().color).getOrNull()

        // When
        val actual = items.toFolderUiModel(colorMapper)

        // Then
        val f0 = FolderTestData.buildFolderUiModel(
            id = LabelId("0"), color = labelColor, level = 0,
            order = 0,
            children = listOf(LabelId("0.1")), icon = R.drawable.ic_proton_folders
        )
        val f01 = FolderTestData.buildFolderUiModel(
            id = LabelId("0.1"), parent = f0, color = labelColor, level = 1,
            order = 0, icon = R.drawable.ic_proton_folder
        )
        val expected = listOf(f0, f01)

        assertEquals(expected, actual)
    }
}
