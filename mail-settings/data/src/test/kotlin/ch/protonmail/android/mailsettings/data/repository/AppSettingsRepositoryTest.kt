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

package ch.protonmail.android.mailsettings.data.repository

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsession.data.wrapper.MailSessionWrapper
import ch.protonmail.android.mailsettings.data.local.RustAppSettingsDataSource
import ch.protonmail.android.mailsettings.domain.model.AppLanguage
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.model.AppSettingsDiff
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.domain.repository.AppLanguageRepository
import ch.protonmail.android.mailsettings.domain.repository.AppSettingsRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.AppAppearance
import uniffi.proton_mail_uniffi.AppProtection
import uniffi.proton_mail_uniffi.AutoLock
import uniffi.proton_mail_uniffi.MailSession

class AppSettingsRepositoryTest {

    @get:Rule
    val loggingTestRule = LoggingTestRule()

    private val mockMailSession = mockk<MailSession>()
    private val appSettingsDataSource = mockk<RustAppSettingsDataSource> {
        coEvery { this@mockk.updateAppSettings(mockMailSession, any()) } returns Unit.right()
    }
    private val appLanguageRepository = mockk<AppLanguageRepository> {
        every { this@mockk.observe() } returns flowOf(AppLanguage.FRENCH)
    }

    private val mockMailSessionWrapper = mockk<MailSessionWrapper> {
        every { this@mockk.getRustMailSession() } returns mockMailSession
    }

    private val mailSessionRepository = mockk<MailSessionRepository> {
        every { this@mockk.getMailSession() } returns mockMailSessionWrapper
    }

    private lateinit var appSettingsRepository: AppSettingsRepository

    private val mockAppSettings = uniffi.proton_mail_uniffi.AppSettings(
        AppAppearance.LIGHT_MODE,
        AppProtection.PIN,
        AutoLock.Always,
        useCombineContacts = true,
        useAlternativeRouting = true
    )

    private val expectedAppSettings = AppSettings(
        autolockInterval = AutoLockInterval.Immediately,
        autolockProtection = Protection.Pin,
        hasAlternativeRouting = true,
        customAppLanguage = AppLanguage.FRENCH.langName,
        hasDeviceContactsEnabled = true,
        theme = Theme.LIGHT
    )

    @Before
    fun setUp() {
        appSettingsRepository =
            AppSettingsRepository(mailSessionRepository, appSettingsDataSource, appLanguageRepository)
    }


    @Test
    fun `returns theme when observed`() = runTest {
        // Given
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right()
        // When
        appSettingsRepository.observeTheme().test {
            // Then
            assertEquals(expectedAppSettings.theme, awaitItem())
        }
    }

    @Test
    fun `returns language when observed`() = runTest {
        // Given
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right()
        // When
        appSettingsRepository.observeAppSettings().test {
            // Then
            assertEquals(expectedAppSettings.customAppLanguage, awaitItem().customAppLanguage)
        }
    }

    @Test
    fun `when theme is updated then theme observer is also updated`() = runTest {
        // Given
        val expectedInitialTheme = Theme.LIGHT
        val updatedAppSettings = mockAppSettings.copy(AppAppearance.DARK_MODE)
        val expectedUpdatedTheme = Theme.DARK
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right() andThen updatedAppSettings.right()
        // When
        appSettingsRepository.observeTheme().test {
            assertEquals(expectedInitialTheme, awaitItem())

            appSettingsRepository.updateTheme(expectedUpdatedTheme)

            assertEquals(expectedUpdatedTheme, awaitItem())
        }
    }


    @Test
    fun `when theme is updated then appSettings observer is also updated`() = runTest {
        // Given
        val expectedInitialAppSettings = expectedAppSettings
        val expectedUpdatedTheme = Theme.DARK
        val expectedUpdatedAppSettings = expectedAppSettings.copy(theme = expectedUpdatedTheme)

        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right() andThen mockAppSettings.copy(AppAppearance.DARK_MODE).right()
        // When
        appSettingsRepository.observeAppSettings().test {
            assertEquals(expectedInitialAppSettings, awaitItem())

            appSettingsRepository.updateTheme(expectedUpdatedTheme)

            assertEquals(expectedUpdatedAppSettings, awaitItem())
        }
    }

    @Test
    fun `when theme is updated via appDiff  then appSettings observer is also updated`() = runTest {
        // Given
        val expectedInitialAppSettings = expectedAppSettings
        val expectedUpdatedTheme = Theme.DARK
        val expectedUpdatedAppSettings = expectedAppSettings.copy(theme = expectedUpdatedTheme)

        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right() andThen mockAppSettings.copy(AppAppearance.DARK_MODE).right()
        // When
        appSettingsRepository.observeAppSettings().test {
            assertEquals(expectedInitialAppSettings, awaitItem())

            appSettingsRepository.updateAppSettings(AppSettingsDiff(theme = expectedUpdatedTheme))

            assertEquals(expectedUpdatedAppSettings, awaitItem())
        }
    }

    @Test
    fun `when interval is updated via appDiff then appSettings observer is also updated`() = runTest {
        // Given
        val expectedInitialAppSettings = expectedAppSettings
        val expectedUpdatedInterval = AutoLockInterval.FifteenMinutes
        val expectedUpdatedAppSettings = expectedAppSettings.copy(autolockInterval = expectedUpdatedInterval)
        val minutes = 15L
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right() andThen mockAppSettings.copy(autoLock = AutoLock.Minutes(minutes.toUByte()))
            .right()
        // When
        appSettingsRepository.observeAppSettings().test {
            assertEquals(expectedInitialAppSettings, awaitItem())

            appSettingsRepository.updateAppSettings(AppSettingsDiff(interval = expectedUpdatedInterval))

            assertEquals(expectedUpdatedAppSettings, awaitItem())
        }
    }

    @Test
    fun `when error retrieving settings then return default settings and log error`() = runTest {
        // Given
        val error = DataError.Local.Unknown
        val expectedSettings = AppSettings.default()
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns error.left()
        // When
        appSettingsRepository.observeAppSettings().test {
            assertEquals(expectedSettings, awaitItem())
            loggingTestRule.assertErrorLogged(
                "Unable to get app settings $error, returning default settings: $expectedSettings"
            )
        }
    }

    @Test
    fun `when theme is updated via appDiff  then appSettings observer is also updated`() = runTest {
        // Given
        val expectedInitialAppSettings = expectedAppSettings
        val expectedUpdatedTheme = Theme.DARK
        val expectedUpdatedAppSettings = expectedAppSettings.copy(theme = expectedUpdatedTheme)

        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right() andThen mockAppSettings.copy(AppAppearance.DARK_MODE).right()
        // When
        appSettingsRepository.observeAppSettings().test {
            assertEquals(expectedInitialAppSettings, awaitItem())

            appSettingsRepository.updateAppSettings(AppSettingsDiff(theme = expectedUpdatedTheme))

            assertEquals(expectedUpdatedAppSettings, awaitItem())
        }
    }

    @Test
    fun `when alternativeRouting is updated via appDiff  then appSettings observer is also updated`() = runTest {
        // Given
        val expectedInitialAppSettings = expectedAppSettings
        val expectedUpdatedRouting = false
        val expectedUpdatedAppSettings = expectedAppSettings.copy(hasAlternativeRouting = false)

        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right() andThen
            mockAppSettings.copy(useAlternativeRouting = expectedUpdatedAppSettings.hasAlternativeRouting)
                .right()
        // When
        appSettingsRepository.observeAppSettings().test {
            assertEquals(expectedInitialAppSettings, awaitItem())

            appSettingsRepository.updateAppSettings(AppSettingsDiff(alternativeRouting = expectedUpdatedRouting))

            assertEquals(expectedUpdatedAppSettings, awaitItem())
        }
    }
}
