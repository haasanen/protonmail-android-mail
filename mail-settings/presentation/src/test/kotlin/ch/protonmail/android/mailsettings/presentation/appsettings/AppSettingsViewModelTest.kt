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

package ch.protonmail.android.mailsettings.presentation.appsettings

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.testdata.AppSettingsTestData
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class AppSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appSettingsFlow = MutableSharedFlow<AppSettings>()
    private val observeAppSettings = mockk<AppSettingsRepository> {
        every { this@mockk.observeAppSettings() } returns appSettingsFlow
    }

    private lateinit var viewModel: AppSettingsViewModel

    @Before
    fun setUp() {

        viewModel = AppSettingsViewModel(observeAppSettings)
    }

    @Test
    fun `emits loading state when initialised`() = runTest {
        viewModel.state.test {
            assertEquals(AppSettingsState.Loading, awaitItem())
        }
    }

    @Test
    fun `state has app settings info when get app settings use case returns valid data`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()

            // When
            appSettingsFlow.emit(AppSettingsTestData.appSettings)

            // Then
            val actual = awaitItem() as AppSettingsState.Data
            val expected = AppSettingsUiModel(
                autoLockEnabled = false,
                alternativeRoutingEnabled = true,
                customLanguage = null,
                deviceContactsEnabled = true,
                theme = TextUiModel.TextRes(R.string.mail_settings_system_default)
            )
            assertEquals(expected, actual.settings)
        }
    }

    private suspend fun ReceiveTurbine<AppSettingsState>.initialStateEmitted() {
        awaitItem() as AppSettingsState.Loading
    }
}
