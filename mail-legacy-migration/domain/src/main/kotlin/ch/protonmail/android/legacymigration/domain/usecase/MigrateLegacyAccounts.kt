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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.model.AccountMigrationInfo
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.repository.LegacyAccountRepository
import timber.log.Timber
import javax.inject.Inject

class MigrateLegacyAccounts @Inject constructor(
    private val legacyAccountRepository: LegacyAccountRepository,
    private val setPrimaryAccountAfterMigration: SetPrimaryAccountAfterMigration
) {

    suspend operator fun invoke(): Either<List<MigrationError>, Unit> {
        val sessions = legacyAccountRepository.getAuthenticatedLegacySessions()

        val successfulMigrationList = mutableListOf<AccountMigrationInfo>()
        val migrationErrors = mutableListOf<MigrationError>()

        sessions.forEach { session ->

            val migrationInfo = legacyAccountRepository.getLegacyAccountMigrationInfoFor(session).fold(
                { error ->
                    migrationErrors.add(error)
                    null
                },
                { accountMigrationInfo ->
                    accountMigrationInfo
                }
            )

            migrationInfo?.let {
                when (val migrationResult = legacyAccountRepository.migrateLegacyAccount(it)) {
                    is Either.Right -> {
                        Timber.d("Legacy migration: Successfully migrated account ${it.username}")
                        successfulMigrationList.add(it)
                    }
                    is Either.Left -> {
                        Timber.d("Legacy migration: Failed to migrate account ${it.username}: ${migrationResult.value}")
                        migrationErrors.add(migrationResult.value)
                    }
                }
            }
        }

        setPrimaryAccountAfterMigration(successfulMigrationList)

        return if (successfulMigrationList.isEmpty()) {
            migrationErrors.left()
        } else {
            Unit.right()
        }
    }
}
