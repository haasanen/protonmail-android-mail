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

package ch.protonmail.android.mailmessage.presentation.mapper

import io.mockk.mockk
import ch.protonmail.android.mailcommon.presentation.model.AvatarImageUiModel
import ch.protonmail.android.mailmessage.domain.model.AvatarImageState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class AvatarImageUiModelMapperTest {

    private val mapper = AvatarImageUiModelMapper()

    @Test
    fun `should map NotLoaded correctly`() {
        // Given
        val avatarImageState = AvatarImageState.NotLoaded

        // When
        val uiModel = mapper.toUiModel(avatarImageState)

        // Then
        assertEquals(AvatarImageUiModel.NotLoaded, uiModel)
    }

    @Test
    fun `should map Loading correctly`() {
        // Given
        val avatarImageState = AvatarImageState.Loading

        // When
        val uiModel = mapper.toUiModel(avatarImageState)

        // Then
        assertEquals(AvatarImageUiModel.Loading, uiModel)
    }

    @Test
    fun `should map NoImageAvailable correctly`() {
        // Given
        val avatarImageState = AvatarImageState.NoImageAvailable

        // When
        val uiModel = mapper.toUiModel(avatarImageState)

        // Then
        assertEquals(AvatarImageUiModel.NoImageAvailable, uiModel)
    }

    @Test
    fun `should map Data correctly`() {
        // Given
        val mockFile = mockk<File>(relaxed = true)
        val avatarImageState = AvatarImageState.Data(mockFile)

        // When
        val uiModel = mapper.toUiModel(avatarImageState)

        // Then
        assertEquals(AvatarImageUiModel.Data(mockFile), uiModel)
    }
}

