/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.feature.appicon

import ch.protonmail.android.feature.appicon.model.AppIconUiModel
import ch.protonmail.android.mailsettings.presentation.settings.appicon.AppIconResourceManager
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AppIconDataMapperTest {

    private val manager = mockk<AppIconResourceManager>()
    private lateinit var iconMapper: AppIconDataMapper

    @BeforeTest
    fun setup() {
        iconMapper = AppIconDataMapper(manager)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should map to ui model`() {
        // Given
        val icon = AppIconData.DEFAULT
        val iconRes = 1
        val descriptionRes = 2

        every { manager.getIconRes(icon.id) } returns iconRes
        every { manager.getDescriptionStringRes(icon.id) } returns descriptionRes

        val expected = AppIconUiModel(
            data = icon,
            iconPreviewResId = iconRes,
            labelResId = descriptionRes
        )

        // When
        val actual = iconMapper.toUiModel(icon)

        // Then
        assertEquals(expected, actual)
    }
}
