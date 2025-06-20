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

package ch.protonmail.android.legacymigration.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockBiometricsPreference
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockPin
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.repository.LegacyAutoLockRepository
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.domain.model.autolock.SetAutoLockPinError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MigrateAutoLockLegacyPreferenceTest {

    private val legacyAutoLockRepository = mockk<LegacyAutoLockRepository>()
    private val userSessionRepository = mockk<UserSessionRepository>()

    private lateinit var migrateAutoLockLegacyPreference: MigrateAutoLockLegacyPreference

    @BeforeTest
    fun setup() {
        migrateAutoLockLegacyPreference = MigrateAutoLockLegacyPreference(
            legacyAutoLockRepository = legacyAutoLockRepository,
            userSessionRepository = userSessionRepository
        )
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should migrate biometrics and not pin when both are enabled`() = runTest {
        // Given
        expectPinLockEnabled(true)
        expectBiometricsEnabled(true)
        coEvery { userSessionRepository.setBiometricAppProtection() } returns Unit.right()

        // When
        val result = migrateAutoLockLegacyPreference()

        // Then
        assertEquals(Unit.right(), result)
        coVerifySequence {
            legacyAutoLockRepository.hasAutoLockPinCode()
            legacyAutoLockRepository.getAutoLockBiometricsPreference()
            userSessionRepository.setBiometricAppProtection()
        }
        confirmVerified(legacyAutoLockRepository, userSessionRepository)
    }

    @Test
    fun `should migrate pin when present`() = runTest {
        // Given
        expectPinLockEnabled(true)
        expectBiometricsEnabled(false)
        val pinCode = "1234"
        coEvery {
            legacyAutoLockRepository.observeAutoLockPinCode()
        } returns flowOf(LegacyAutoLockPin(pinCode).right())
        val autoLockPin = AutoLockPin(pinCode)

        coEvery {
            userSessionRepository.setAutoLockPinCode(autoLockPin)
        } returns Unit.right()

        // When
        val result = migrateAutoLockLegacyPreference()

        // Then
        assertEquals(Unit.right(), result)
        coVerify { userSessionRepository.setAutoLockPinCode(autoLockPin) }
        confirmVerified(userSessionRepository)
    }

    @Test
    fun `should return migration error on pin migration when it can't be observed`() = runTest {
        // Given
        expectPinLockEnabled(true)
        expectBiometricsEnabled(false)

        val pinCode = "1234"
        val autoLockPin = AutoLockPin(pinCode)
        val expected = MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left()

        coEvery {
            legacyAutoLockRepository.observeAutoLockPinCode()
        } returns flowOf()

        coEvery {
            userSessionRepository.setAutoLockPinCode(autoLockPin)
        } returns Unit.right()

        // When
        val result = migrateAutoLockLegacyPreference()

        // Then
        assertEquals(expected, result)
        confirmVerified(userSessionRepository)
    }

    @Test
    fun `should return migration error on pin migration when it can't be set on Rust side`() = runTest {
        // Given
        expectPinLockEnabled(true)
        expectBiometricsEnabled(false)

        val pinCode = "1234"
        val autoLockPin = AutoLockPin(pinCode)
        val expected = MigrationError.AutoLockFailure.FailedToSetAutoLockPin.left()

        coEvery {
            legacyAutoLockRepository.observeAutoLockPinCode()
        } returns flowOf(LegacyAutoLockPin(pinCode).right())

        coEvery {
            userSessionRepository.setAutoLockPinCode(autoLockPin)
        } returns SetAutoLockPinError.PinIsMalformed.left()

        // When
        val result = migrateAutoLockLegacyPreference()

        // Then
        assertEquals(expected, result)
        coVerify { userSessionRepository.setAutoLockPinCode(autoLockPin) }
        confirmVerified(userSessionRepository)
    }

    @Test
    fun `should return migration error on biometrics when it can't be set on Rust side`() = runTest {
        // Given
        expectPinLockEnabled(false)
        expectBiometricsEnabled(true)
        val expected = MigrationError.AutoLockFailure.FailedToSetBiometricPreference.left()

        coEvery { userSessionRepository.setBiometricAppProtection() } returns DataError.Local.Unknown.left()

        // When
        val result = migrateAutoLockLegacyPreference()

        // Then
        assertEquals(expected, result)
        coVerify { userSessionRepository.setBiometricAppProtection() }
        confirmVerified(userSessionRepository)
    }

    @Test
    fun `should return an unknown error when invoked but no policy is present`() = runTest {
        // Given
        val expected = MigrationError.Unknown.left()
        expectPinLockEnabled(false)
        coEvery {
            legacyAutoLockRepository.getAutoLockBiometricsPreference()
        } returns MigrationError.AutoLockFailure.FailedToReadBiometricPreference.left()

        // When
        val result = migrateAutoLockLegacyPreference()

        // Then
        assertEquals(expected, result)
        confirmVerified(userSessionRepository)
    }

    private fun expectPinLockEnabled(value: Boolean) {
        coEvery { legacyAutoLockRepository.hasAutoLockPinCode() } returns value
    }

    private fun expectBiometricsEnabled(value: Boolean) {
        coEvery {
            legacyAutoLockRepository.getAutoLockBiometricsPreference()
        } returns LegacyAutoLockBiometricsPreference(value).right()
    }
}
