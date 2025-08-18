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
import arrow.core.right
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.appsettings.usecase.GetNotificationsEnabled
import ch.protonmail.android.mailsettings.presentation.testdata.AppSettingsTestData
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
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
        coEvery { this@mockk.updateAlternativeRouting(any()) } returns Unit.right()
        coEvery { this@mockk.updateUseCombineContacts(any()) } returns Unit.right()
    }

    private val notificationSettingsUsecase = mockk<GetNotificationsEnabled> {
        every { this@mockk.invoke() } returns true
    }

    private lateinit var viewModel: AppSettingsViewModel

    @Before
    fun setUp() {

        viewModel = AppSettingsViewModel(observeAppSettings, notificationSettingsUsecase)
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
                theme = TextUiModel.TextRes(R.string.mail_settings_system_default),
                notificationsEnabledStatus = TextUiModel(R.string.notifications_on)
            )
            assertEquals(expected, actual.settings)
        }
    }

    @Test
    fun `on Toggle alternative routing then update alternative routing via repository`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()

            viewModel.submit(ToggleAlternativeRouting(true))
            coVerify { observeAppSettings.updateAlternativeRouting(true) }
        }
    }

    @Test
    fun `on Toggle combined contacts then update device contacts via repository`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()

            viewModel.submit(ToggleUseCombinedContacts(true))
            coVerify { observeAppSettings.updateUseCombineContacts(true) }
        }
    }

    @Test
    fun `on new value received for alternativeRoutingEnabled THEN state is updated`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()

            // When
            appSettingsFlow.emit(AppSettingsTestData.appSettings)

            // Then
            val expectedFirstValue = true
            val actual = awaitItem() as AppSettingsState.Data
            assertEquals(expectedFirstValue, actual.settings.alternativeRoutingEnabled)

            val expectedSecondValue = false
            appSettingsFlow.emit(AppSettingsTestData.appSettings.copy(hasAlternativeRouting = expectedSecondValue))

            val actualUpdatedData = awaitItem() as AppSettingsState.Data
            assertEquals(expectedSecondValue, actualUpdatedData.settings.alternativeRoutingEnabled)
        }
    }

    @Test
    fun `on new value received for CombinedContacts THEN state is updated`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()

            // When
            appSettingsFlow.emit(AppSettingsTestData.appSettings)

            // Then
            val expectedFirstValue = true
            val actual = awaitItem() as AppSettingsState.Data
            assertEquals(expectedFirstValue, actual.settings.deviceContactsEnabled)

            val expectedSecondValue = false
            appSettingsFlow.emit(AppSettingsTestData.appSettings.copy(hasCombinedContactsEnabled = expectedSecondValue))

            val actualUpdatedData = awaitItem() as AppSettingsState.Data
            assertEquals(expectedSecondValue, actualUpdatedData.settings.deviceContactsEnabled)
        }
    }


    private suspend fun ReceiveTurbine<AppSettingsState>.initialStateEmitted() {
        awaitItem() as AppSettingsState.Loading
    }
}
