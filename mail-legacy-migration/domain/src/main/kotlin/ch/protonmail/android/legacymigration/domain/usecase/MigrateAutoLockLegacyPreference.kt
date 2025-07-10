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
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockBiometricsPreference
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockIntervalPreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.model.toAutoLockPin
import ch.protonmail.android.legacymigration.domain.repository.LegacyAutoLockRepository
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

class MigrateAutoLockLegacyPreference @Inject constructor(
    private val legacyAutoLockRepository: LegacyAutoLockRepository,
    private val userSessionRepository: UserSessionRepository,
    private val autoLockRepository: AutoLockRepository
) {

    suspend operator fun invoke(): Either<MigrationError, Unit> = either {
        val shouldMigrateAutoLockPin = shouldMigrateAutoLockPin()
        val shouldMigrateAutoLockBiometricPreference = shouldMigrateBiometricPreference()

        when {
            // Biometrics always has precedence over PIN lock in V7
            shouldMigrateAutoLockBiometricPreference -> {
                migrationResultWrapper("legacy auto-lock biometric preference") {
                    migrateLegacyAutoLockBiometricPreference().flatMap {
                        migrateLegacyAutoLockInterval()
                    }
                }.bind()
            }

            shouldMigrateAutoLockPin -> {
                migrationResultWrapper("legacy auto-lock pin code") {
                    migrateLegacyAutoLockPinCode().flatMap {
                        migrateLegacyAutoLockInterval()
                    }
                }.bind()
            }

            else -> {
                Timber.w("Legacy migration: Unexpected status - has lock but no PIN/Biometrics set")
                raise(MigrationError.Unknown)
            }
        }
    }

    private suspend fun migrateLegacyAutoLockInterval(): Either<MigrationError, Unit> =
        legacyAutoLockRepository.getAutoLockInterval().map {
            migrationResultWrapper("legacy auto-lock interval") {
                migrateAutoLockInterval(it)
            }
        }

    private suspend fun migrateAutoLockInterval(
        legacyInterval: LegacyAutoLockIntervalPreference
    ): Either<MigrationError, Unit> {

        val interval = AutoLockInterval.fromMinutes(legacyInterval.duration.inWholeMinutes)
        return autoLockRepository.updateAutoLockInterval(interval)
            .mapLeft { MigrationError.AutoLockFailure.FailedToSetAutoLockInterval }
    }

    private suspend fun migrateLegacyAutoLockPinCode(): Either<MigrationError, Unit> {
        val legacyPinResult = legacyAutoLockRepository.observeAutoLockPinCode().firstOrNull()
            ?: run {
                Timber.e("Legacy migration: Failed to read auto-lock pin code from legacy repository.")
                return MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left()
            }

        return legacyPinResult
            .mapLeft { MigrationError.AutoLockFailure.FailedToReadAutoLockPin }
            .flatMap { localPin ->
                userSessionRepository.setAutoLockPinCode(localPin.toAutoLockPin())
                    .mapLeft {
                        Timber.e("Legacy migration: Failed to set auto-lock pin code: $it")
                        MigrationError.AutoLockFailure.FailedToSetAutoLockPin
                    }
            }
    }

    private suspend fun migrateLegacyAutoLockBiometricPreference(): Either<MigrationError, Unit> {
        return userSessionRepository.setBiometricAppProtection()
            .mapLeft {
                Timber.e("Legacy migration: Failed to set auto-lock biometric preference: $it")
                MigrationError.AutoLockFailure.FailedToSetBiometricPreference
            }
    }

    private suspend fun shouldMigrateAutoLockPin() = legacyAutoLockRepository.hasAutoLockPinCode()

    private suspend fun shouldMigrateBiometricPreference() = legacyAutoLockRepository.getAutoLockBiometricsPreference()
        .mapLeft {
            Timber.e("Legacy migration: Failed to read auto lock preference from legacy repository: $it")
        }
        .getOrElse { LegacyAutoLockBiometricsPreference(false) }
        .enabled

    private suspend fun <T> migrationResultWrapper(
        operation: String,
        migrate: suspend () -> Either<MigrationError, T>
    ): Either<MigrationError, T> = migrate()
        .onLeft { error ->
            Timber.e("Legacy migration: Failed to migrate $operation: $error")
        }
        .onRight {
            Timber.d("Legacy migration: Successfully migrated $operation")
        }
}
