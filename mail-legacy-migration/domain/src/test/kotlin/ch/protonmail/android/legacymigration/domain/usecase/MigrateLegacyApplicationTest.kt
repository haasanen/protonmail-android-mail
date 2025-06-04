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
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import io.mockk.coVerifySequence

class MigrateLegacyApplicationTest {

    private val migrateLegacyAccounts = mockk<MigrateLegacyAccounts>()
    private val shouldMigrateLegacyAccount = mockk<ShouldMigrateLegacyAccount>()
    private val destroyLegacyDatabases = mockk<DestroyLegacyDatabases>(relaxUnitFun = true)
    private val shouldMigrateAutoLockPin = mockk<ShouldMigrateAutoLockPin>()
    private val migrateLegacyAutoLockPinCode = mockk<MigrateLegacyAutoLockPinCode>()
    private val isLegacyAutoLockEnabled = mockk<IsLegacyAutoLockEnabled>()
    private val shouldMigrateAutoLockBiometricPreference = mockk<ShouldMigrateAutoLockBiometricPreference>()
    private val migrateLegacyAutoLockBiometricPreference = mockk<MigrateLegacyAutoLockBiometricPreference>()

    private val migrateLegacyApplication = MigrateLegacyApplication(
        migrateLegacyAccounts,
        shouldMigrateLegacyAccount,
        destroyLegacyDatabases,
        shouldMigrateAutoLockPin,
        migrateLegacyAutoLockPinCode,
        isLegacyAutoLockEnabled,
        shouldMigrateAutoLockBiometricPreference,
        migrateLegacyAutoLockBiometricPreference
    )

    @Test
    fun `should migrate everything when all are required`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns Unit.right()
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery { shouldMigrateAutoLockPin() } returns true
        coEvery { migrateLegacyAutoLockPinCode() } returns Unit.right()
        coEvery { shouldMigrateAutoLockBiometricPreference() } returns true
        coEvery { migrateLegacyAutoLockBiometricPreference() } returns Unit.right()

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            destroyLegacyDatabases()
            isLegacyAutoLockEnabled()
            shouldMigrateAutoLockPin()
            migrateLegacyAutoLockPinCode()
            shouldMigrateAutoLockBiometricPreference()
            migrateLegacyAutoLockBiometricPreference()
        }
    }

    @Test
    fun `should skip account migration but migrate both auto-lock items`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns false
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery { shouldMigrateAutoLockPin() } returns true
        coEvery { migrateLegacyAutoLockPinCode() } returns Unit.right()
        coEvery { shouldMigrateAutoLockBiometricPreference() } returns true
        coEvery { migrateLegacyAutoLockBiometricPreference() } returns Unit.right()

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            destroyLegacyDatabases()
            isLegacyAutoLockEnabled()
            shouldMigrateAutoLockPin()
            migrateLegacyAutoLockPinCode()
            shouldMigrateAutoLockBiometricPreference()
            migrateLegacyAutoLockBiometricPreference()
        }

        coVerify(exactly = 0) { migrateLegacyAccounts() }
    }

    @Test
    fun `should skip auto-lock pin migration when not needed`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns Unit.right()
        coEvery { shouldMigrateAutoLockPin() } returns false
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery { shouldMigrateAutoLockBiometricPreference() } returns false

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            destroyLegacyDatabases()
            shouldMigrateAutoLockPin()
        }

        coVerify(exactly = 0) { migrateLegacyAutoLockPinCode() }
    }

    @Test
    fun `should skip biometric preference when not needed`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns Unit.right()
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery { shouldMigrateAutoLockPin() } returns true
        coEvery { migrateLegacyAutoLockPinCode() } returns Unit.right()
        coEvery { shouldMigrateAutoLockBiometricPreference() } returns false

        // WHen
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            destroyLegacyDatabases()
            isLegacyAutoLockEnabled()
            shouldMigrateAutoLockPin()
            migrateLegacyAutoLockPinCode()
            shouldMigrateAutoLockBiometricPreference()
        }

        coVerify(exactly = 0) { migrateLegacyAutoLockBiometricPreference() }
    }

    @Test
    fun `should skip all auto-lock migration when not enabled`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns Unit.right()
        coEvery { isLegacyAutoLockEnabled() } returns false

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            destroyLegacyDatabases()
            isLegacyAutoLockEnabled()
        }

        coVerify(exactly = 0) { shouldMigrateAutoLockPin() }
        coVerify(exactly = 0) { migrateLegacyAutoLockPinCode() }
        coVerify(exactly = 0) { shouldMigrateAutoLockBiometricPreference() }
        coVerify(exactly = 0) { migrateLegacyAutoLockBiometricPreference() }
    }

    @Test
    fun `should not throw on failures and still continue`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns listOf(MigrationError.MigrateFailed.AuthenticationFailure).left()
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery { shouldMigrateAutoLockPin() } returns true
        coEvery { migrateLegacyAutoLockPinCode() } returns MigrationError.AutoLockFailure.FailedToSetAutoLockPin.left()
        coEvery { shouldMigrateAutoLockBiometricPreference() } returns true
        coEvery {
            migrateLegacyAutoLockBiometricPreference()
        } returns MigrationError.AutoLockFailure.FailedToSetBiometricPreference.left()

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            isLegacyAutoLockEnabled()
            shouldMigrateAutoLockPin()
            migrateLegacyAutoLockPinCode()
            shouldMigrateAutoLockBiometricPreference()
            migrateLegacyAutoLockBiometricPreference()
        }

        coVerify(exactly = 0) { destroyLegacyDatabases() }
    }
}
