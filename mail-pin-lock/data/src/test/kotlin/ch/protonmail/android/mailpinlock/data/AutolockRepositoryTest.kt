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

package ch.protonmail.android.mailpinlock.data

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalAppSettings
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailpinlock.domain.BiometricsSystemStateRepository
import ch.protonmail.android.mailpinlock.model.AutoLockBiometricsState
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.model.BiometricsSystemState
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsession.data.wrapper.MailSessionWrapper
import ch.protonmail.android.mailsettings.data.local.RustAppSettingsDataSource
import ch.protonmail.android.mailsettings.data.repository.AppSettingsRepository
import ch.protonmail.android.mailsettings.domain.model.AppLanguage
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.domain.repository.AppLanguageRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.AppAppearance
import uniffi.proton_mail_uniffi.AppProtection
import uniffi.proton_mail_uniffi.AutoLock
import uniffi.proton_mail_uniffi.MailSession
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutolockRepositoryTest {

    @get:Rule
    val loggingTestRule = LoggingTestRule()

    private val mockMailSession = mockk<MailSession>()
    private val mockMailSessionWrapper = mockk<MailSessionWrapper> {
        every { this@mockk.getRustMailSession() } returns mockMailSession
    }
    private val appSettingsDataSource = mockk<RustAppSettingsDataSource> {
        coEvery { this@mockk.updateAppSettings(mockMailSessionWrapper, any()) } returns Unit.right()
    }
    private val appLockDataSource = mockk<AppLockDataSource>()
    private val appLanguageRepository = mockk<AppLanguageRepository> {
        every { this@mockk.observe() } returns flowOf(AppLanguage.FRENCH)
    }

    private val mailSessionRepository = mockk<MailSessionRepository> {
        every { this@mockk.getMailSession() } returns mockMailSessionWrapper
    }

    private val expectedBiometrics = AutoLockBiometricsState.BiometricsAvailable.BiometricsEnrolled(false)
    private val biometricsStateRepository = mockk<BiometricsSystemStateRepository> {
        every { this@mockk.observe() } returns flowOf(BiometricsSystemState.BiometricEnrolled)
    }

    private val appSettingsRepository: AppSettingsRepository =
        AppSettingsRepository(
            mailSessionRepository = mailSessionRepository,
            rustAppSettingsDataSource = appSettingsDataSource,
            appLanguageRepository = appLanguageRepository
        )

    private var autolockRepository: AutolockRepository =
        AutolockRepository(
            biometricsSystemStateRepository = biometricsStateRepository,
            appSettingsRepository = appSettingsRepository,
            mailSessionRepository = mailSessionRepository,
            appLockDataSource = appLockDataSource
        )

    private val mockAppSettings = LocalAppSettings(
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
        hasCombinedContactsEnabled = true,
        theme = Theme.LIGHT
    )

    @Test
    fun `returns protection when observed`() = runTest {
        // Given
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right()
        // When
        autolockRepository.observeAppLock().test {
            // Then
            assertEquals(expectedAppSettings.autolockProtection, awaitItem().protectionType)
        }
    }

    @Test
    fun `returns interval when observed`() = runTest {
        // Given
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right()
        // When
        autolockRepository.observeAppLock().test {
            // Then
            assertEquals(expectedAppSettings.autolockInterval, awaitItem().autolockInterval)
        }
    }

    @Test
    fun `returns biometrics when observed`() = runTest {
        // Given
        coEvery { appLockDataSource.shouldAutoLock(mockMailSession) } returns false.right()
        coEvery {
            appSettingsDataSource.getAppSettings(any())
        } returns mockAppSettings.right()
        // When
        autolockRepository.observeAppLock().test {
            // Then
            assertEquals(expectedBiometrics, awaitItem().biometricsState)
        }
    }

    @Test
    fun `when interval is updated then observer is also updated`() = runTest {
        val expectedUpdatedInterval = AutoLockInterval.FifteenMinutes
        val updatedAppSettings = mockAppSettings.copy(autoLock = AutoLock.Minutes(15L.toUByte()))
        // Given
        coEvery {
            appSettingsDataSource.getAppSettings(mockMailSessionWrapper)
        } returns mockAppSettings.right() andThen updatedAppSettings.right()
        // When

        autolockRepository.observeAppLock().test {
            // Then
            assertEquals(expectedAppSettings.autolockInterval, awaitItem().autolockInterval)

            autolockRepository.updateAutolockInterval(expectedUpdatedInterval)

            assertEquals(expectedUpdatedInterval, awaitItem().autolockInterval)
        }
    }

    @Test
    fun `when shouldAutolock THEN return result`() = runTest {
        coEvery { appLockDataSource.shouldAutoLock(mockMailSession) } returns true.right()
        val result = autolockRepository.shouldAutolock()
        assert(result.isRight())
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `when shouldAutolock is False THEN return result`() = runTest {
        coEvery { appLockDataSource.shouldAutoLock(mockMailSession) } returns false.right()
        val result = autolockRepository.shouldAutolock()

        assert(result.isRight())
        assertFalse(result.getOrNull()!!)
    }

    @Test
    fun `when shouldAutolock fails then return error`() = runTest {
        coEvery { appLockDataSource.shouldAutoLock(mockMailSession) } returns DataError.Local.Unknown.left()
        val result = autolockRepository.shouldAutolock()

        assert(result.isLeft())
    }
}
