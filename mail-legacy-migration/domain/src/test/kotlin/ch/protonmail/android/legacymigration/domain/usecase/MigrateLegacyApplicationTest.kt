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
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MigrateLegacyApplicationTest {

    private val migrateLegacyAccounts = mockk<MigrateLegacyAccounts>()
    private val shouldMigrateLegacyAccount = mockk<ShouldMigrateLegacyAccount>()
    private val destroyLegacyDatabases = mockk<DestroyLegacyDatabases>(relaxUnitFun = true)
    private val isLegacyAutoLockEnabled = mockk<IsLegacyAutoLockEnabled>()
    private val migrateAutoLockLegacyPreference = mockk<MigrateAutoLockLegacyPreference>()
    private val clearLegacyAutoLockPreferences = mockk<ClearLegacyAutoLockPreferences>()

    private val migrateLegacyApplication = MigrateLegacyApplication(
        migrateLegacyAccounts,
        shouldMigrateLegacyAccount,
        destroyLegacyDatabases,
        isLegacyAutoLockEnabled,
        migrateAutoLockLegacyPreference,
        clearLegacyAutoLockPreferences
    )

    @Test
    fun `should migrate everything when all are required`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns Unit.right()
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery { migrateAutoLockLegacyPreference() } returns Unit.right()
        coEvery { clearLegacyAutoLockPreferences() } just runs

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            destroyLegacyDatabases()
            isLegacyAutoLockEnabled()
            migrateAutoLockLegacyPreference()
            clearLegacyAutoLockPreferences()
        }
    }

    @Test
    fun `should skip account migration but migrate both auto-lock items`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns false
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery { migrateAutoLockLegacyPreference() } returns Unit.right()
        coEvery { clearLegacyAutoLockPreferences() } just runs

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            destroyLegacyDatabases()
            isLegacyAutoLockEnabled()
            migrateAutoLockLegacyPreference()
            clearLegacyAutoLockPreferences()
        }

        coVerify(exactly = 0) { migrateLegacyAccounts() }
    }

    @Test
    fun `should skip auto-lock pin migration when not needed but still clear legacy prefs`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns Unit.right()
        coEvery { isLegacyAutoLockEnabled() } returns false
        coEvery { clearLegacyAutoLockPreferences() } just runs

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            destroyLegacyDatabases()
            isLegacyAutoLockEnabled()
            clearLegacyAutoLockPreferences()
        }

        coVerify(exactly = 0) { migrateAutoLockLegacyPreference() }
    }

    @Test
    fun `should not throw on failures and still continue`() = runTest {
        // Given
        coEvery { shouldMigrateLegacyAccount() } returns true
        coEvery { migrateLegacyAccounts() } returns listOf(MigrationError.MigrateFailed.AuthenticationFailure).left()
        coEvery { isLegacyAutoLockEnabled() } returns true
        coEvery {
            migrateAutoLockLegacyPreference()
        } returns MigrationError.AutoLockFailure.FailedToSetAutoLockPin.left()

        // When
        migrateLegacyApplication()

        // Then
        coVerifySequence {
            shouldMigrateLegacyAccount()
            migrateLegacyAccounts()
            isLegacyAutoLockEnabled()
            migrateAutoLockLegacyPreference()
        }

        coVerify(exactly = 0) { destroyLegacyDatabases() }
        coVerify(exactly = 0) { clearLegacyAutoLockPreferences() }
    }
}
