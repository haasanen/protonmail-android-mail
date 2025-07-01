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

import arrow.core.Either
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import jakarta.inject.Inject
import timber.log.Timber

class MigrateLegacyApplication @Inject constructor(
    private val migrateLegacyAccounts: MigrateLegacyAccounts,
    private val shouldMigrateLegacyAccount: ShouldMigrateLegacyAccount,
    private val destroyLegacyDatabases: DestroyLegacyDatabases,
    private val isLegacyAutoLockEnabled: IsLegacyAutoLockEnabled,
    private val migrateAutoLockLegacyPreference: MigrateAutoLockLegacyPreference,
    private val clearLegacyAutoLockPreferences: ClearLegacyAutoLockPreferences,
    private val cleanupLegacyNotificationChannels: CleanupLegacyNotificationChannels
) {

    suspend operator fun invoke() {
        if (shouldMigrateLegacyAccount()) {
            migrateLegacyAccounts().handleAccountMigration()
        } else {
            Timber.d("Legacy migration: No legacy account to migrate")
            destroyLegacyDatabases()
        }

        if (isLegacyAutoLockEnabled()) {
            migrateAutoLockLegacyPreference().handleAutoLockMigration()
        } else {
            Timber.d("Legacy migration: Legacy auto-lock is not enabled, skipping migration")
            clearLegacyAutoLockPreferences()
        }

        cleanupLegacyNotificationChannels()
    }

    private fun Either<List<MigrationError>, Unit>.handleAccountMigration() {
        onLeft { Timber.e("Legacy migration: Failed to migrate legacy accounts") }
        onRight {
            Timber.d("Legacy migration: Successfully migrated legacy accounts")
            destroyLegacyDatabases()
        }
    }

    private suspend fun Either<MigrationError, Unit>.handleAutoLockMigration() {
        onLeft { Timber.e("Legacy migration: Failed to migrate legacy auto-lock preference") }
        onRight { clearLegacyAutoLockPreferences() }
    }
}
