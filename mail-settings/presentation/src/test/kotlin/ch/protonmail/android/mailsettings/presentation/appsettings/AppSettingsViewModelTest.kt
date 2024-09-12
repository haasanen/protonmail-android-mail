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

import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.model.LocalStorageUsageInformation
import ch.protonmail.android.mailsettings.domain.usecase.ClearLocalStorage
import ch.protonmail.android.mailsettings.domain.usecase.ObserveAppSettings
import ch.protonmail.android.mailsettings.domain.usecase.ObserveOverallLocalStorageUsage
import ch.protonmail.android.mailsettings.presentation.testdata.AppSettingsTestData
import ch.protonmail.android.testdata.user.UserTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.core.user.domain.entity.User
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppSettingsViewModelTest {

    private val userFlow = MutableSharedFlow<User?>()

    private val appSettingsFlow = MutableSharedFlow<AppSettings>()
    private val observeAppSettings = mockk<ObserveAppSettings> {
        every { this@mockk.invoke() } returns appSettingsFlow
    }
    private val overallStorageFlow = flowOf(BaseLocalStorageUsageInformation)
    private val observeOverallLocalStorageUsage = mockk<ObserveOverallLocalStorageUsage> {
        every { this@mockk.invoke() } returns overallStorageFlow

    }
    private val clearLocalStorage = mockk<ClearLocalStorage>()

    private lateinit var viewModel: AppSettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = AppSettingsViewModel(
            observeAppSettings,
            observeOverallLocalStorageUsage,
            clearLocalStorage

        )
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
            userFlow.emit(UserTestData.Primary)

            // When
            appSettingsFlow.emit(AppSettingsTestData.appSettings)

            // Then
            val actual = awaitItem() as AppSettingsState.Data
            val expected = AppSettings(
                hasAutoLock = false,
                hasAlternativeRouting = true,
                customAppLanguage = null,
                hasCombinedContacts = true
            )
            assertEquals(expected, actual.appSettings)
        }
    }

    @Test
    fun `state has local storage usage info when observe local storage usage succeeds`() = runTest {
        viewModel.state.test {
            // Given
            initialStateEmitted()
            userFlow.emit(UserTestData.Primary)

            // When
            appSettingsFlow.emit(AppSettingsTestData.appSettings)

            // Then
            val actual = awaitItem() as AppSettingsState.Data
            assertEquals(BaseLocalStorageUsageInformation, actual.totalSizeInformation)
        }
    }

    private suspend fun ReceiveTurbine<AppSettingsState>.initialStateEmitted() {
        awaitItem() as AppSettingsState.Loading
    }

    private companion object {

        val BaseLocalStorageUsageInformation = LocalStorageUsageInformation(123)
    }
}
