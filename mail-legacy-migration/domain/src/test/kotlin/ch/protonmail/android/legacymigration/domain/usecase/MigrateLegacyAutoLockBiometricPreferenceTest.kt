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
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.repository.LegacyAutoLockRepository
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf

class MigrateLegacyAutoLockBiometricPreferenceTest {

    private val legacyAutoLockRepository = mockk<LegacyAutoLockRepository>()
    private val userSessionRepository = mockk<UserSessionRepository>()

    private val migrate = MigrateLegacyAutoLockBiometricPreference(
        legacyAutoLockRepository,
        userSessionRepository
    )

    @Test
    fun `returns without failure when biometric preference is disabled`() = runTest {
        // Given
        val pref = LegacyAutoLockBiometricsPreference(enabled = false)
        every { legacyAutoLockRepository.observeAutoLockBiometricsPreference() } returns flowOf(pref.right())

        // When
        val result = migrate()

        // Then
        assertEquals(Unit.right(), result)
        coVerify(exactly = 0) { userSessionRepository.setBiometricAppProtection() }
    }

    @Test
    fun `sets biometric protection when preference is enabled`() = runTest {
        // Given
        val pref = LegacyAutoLockBiometricsPreference(enabled = true)
        every { legacyAutoLockRepository.observeAutoLockBiometricsPreference() } returns flowOf(pref.right())
        coEvery { userSessionRepository.setBiometricAppProtection() } returns Unit.right()

        // When
        val result = migrate()

        // Then
        assertEquals(Unit.right(), result)
        coVerify(exactly = 1) { userSessionRepository.setBiometricAppProtection() }
    }

    @Test
    fun `returns failure when biometric preference is missing`() = runTest {
        // Given
        every { legacyAutoLockRepository.observeAutoLockBiometricsPreference() } returns emptyFlow()

        // When
        val result = migrate()

        // Then
        assertEquals(MigrationError.AutoLockFailure.FailedToReadBiometricPreference.left(), result)
        coVerify(exactly = 0) { userSessionRepository.setBiometricAppProtection() }
    }

    @Test
    fun `returns failure when biometric preference reading fails`() = runTest {
        // Given
        every {
            legacyAutoLockRepository.observeAutoLockBiometricsPreference()
        } returns flowOf(MigrationError.AutoLockFailure.FailedToReadBiometricPreference.left())

        // When
        val result = migrate()

        // Then
        assertEquals(MigrationError.AutoLockFailure.FailedToReadBiometricPreference.left(), result)
        coVerify(exactly = 0) { userSessionRepository.setBiometricAppProtection() }
    }

    @Test
    fun `returns failure when setting biometric protection fails`() = runTest {
        // Given
        val pref = LegacyAutoLockBiometricsPreference(enabled = true)
        every { legacyAutoLockRepository.observeAutoLockBiometricsPreference() } returns flowOf(pref.right())
        coEvery { userSessionRepository.setBiometricAppProtection() } returns DataError.Local.Unknown.left()

        // When
        val result = migrate()

        // Then
        assertEquals(MigrationError.AutoLockFailure.FailedToSetBiometricPreference.left(), result)
    }
}
