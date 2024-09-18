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

package ch.protonmail.android.mailcommon.presentation.mapper

import androidx.compose.ui.graphics.Color
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class AvatarInformationMapperTest {

    private val colorMapper: ColorMapper = mockk()
    private val avatarInformationMapper = AvatarInformationMapper(colorMapper)

    @Test
    fun `should return ParticipantAvatar with correct values`() {
        // Given
        val avatarInformation = AvatarInformation(
            initials = "TS",
            color = "#FF5733"
        )
        val address = "test@example.com"
        val bimiSelector = "selector"
        val expectedColor = Color(0xFFFF5733)
        every { colorMapper.toColor(avatarInformation.color) } returns expectedColor.right()

        // When
        val result = avatarInformationMapper.toUiModel(avatarInformation, address, bimiSelector)

        // Then
        val expectedUiModel = AvatarUiModel.ParticipantAvatar(
            initial = "TS",
            address = address,
            bimiSelector = bimiSelector,
            color = expectedColor
        )
        assertEquals(expectedUiModel, result)
    }

    @Test
    fun `should return ParticipantAvatar with unspecified color if color mapping fails`() {
        // Given
        val avatarInformation = AvatarInformation(
            initials = "TS",
            color = "#FF5733"
        )
        val address = "test@example.com"
        val bimiSelector = "selector"

        // Mock the color mapping to fail
        every { colorMapper.toColor(avatarInformation.color) } returns avatarInformation.color.left()

        // When
        val result = avatarInformationMapper.toUiModel(avatarInformation, address, bimiSelector)

        // Then
        val expectedUiModel = AvatarUiModel.ParticipantAvatar(
            initial = "TS",
            address = address,
            bimiSelector = bimiSelector,
            color = Color.Unspecified
        )
        assertEquals(expectedUiModel, result)
    }
}
