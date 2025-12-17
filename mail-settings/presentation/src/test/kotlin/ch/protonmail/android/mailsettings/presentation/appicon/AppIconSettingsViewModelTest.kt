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

package ch.protonmail.android.mailsettings.presentation.appicon

import ch.protonmail.android.mailsettings.presentation.settings.appicon.AppIconManager
import ch.protonmail.android.mailsettings.presentation.settings.appicon.AppIconSettingsViewModel
import ch.protonmail.android.mailsettings.presentation.settings.appicon.mapper.AppIconDataMapper
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconUiModel
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class AppIconSettingsViewModelTest {

    private val iconManager = mockk<AppIconManager>()
    private val iconDataMapper = mockk<AppIconDataMapper>()

    private lateinit var viewModel: AppIconSettingsViewModel

    @BeforeTest
    fun setup() {
        viewModel = AppIconSettingsViewModel(iconManager, iconDataMapper)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should call the icon manager to set the app icon`() {
        // Given
        every { iconManager.setNewAppIcon(appIconUiModel.data) } just runs

        // When
        viewModel.setNewAppIcon(appIconUiModel)

        // Then
        verify(exactly = 1) { iconManager.setNewAppIcon(appIconUiModel.data) }
        confirmVerified(iconManager, iconDataMapper)
    }

    @Test
    fun `should return the current app icon`() {
        // Given
        every { iconManager.getCurrentIconData() } returns AppIconData.DEFAULT
        every { iconDataMapper.toUiModel(AppIconData.DEFAULT) } returns appIconUiModel

        // When
        viewModel.getCurrentAppIcon()

        // Then
        verify(exactly = 1) { iconManager.getCurrentIconData() }
        verify(exactly = 1) { iconDataMapper.toUiModel(AppIconData.DEFAULT) }
        confirmVerified(iconManager, iconDataMapper)
    }

    private companion object {
        val appIconUiModel = AppIconUiModel(
            data = AppIconData.DEFAULT,
            iconPreviewResId = 1,
            labelResId = 2
        )
    }
}
