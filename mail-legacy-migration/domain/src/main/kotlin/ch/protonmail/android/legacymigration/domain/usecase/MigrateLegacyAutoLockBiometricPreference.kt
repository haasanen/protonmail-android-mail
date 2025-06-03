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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.repository.LegacyAutoLockRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

class MigrateLegacyAutoLockBiometricPreference @Inject constructor(
    private val legacyAutoLockRepository: LegacyAutoLockRepository,
    private val userSessionRepository: UserSessionRepository
) {
    suspend operator fun invoke(): Either<MigrationError, Unit> {

        val biometricPreference = legacyAutoLockRepository.observeAutoLockBiometricsPreference().firstOrNull()
            ?: run {
                Timber.e("Legacy migration: Failed to read auto-lock biometric preference from legacy repository.")
                return MigrationError.AutoLockFailure.FailedToReadBiometricPreference.left()
            }

        return biometricPreference
            .mapLeft { MigrationError.AutoLockFailure.FailedToReadBiometricPreference }
            .flatMap { pref ->
                if (pref.enabled) {
                    Timber.d("Legacy migration: Migrating Auto-lock biometric preference as enabled.")
                    userSessionRepository.setBiometricAppProtection()
                        .mapLeft {
                            Timber.e("Legacy migration: Failed to set auto-lock biometric preference: $it")
                            MigrationError.AutoLockFailure.FailedToSetBiometricPreference
                        }
                } else {
                    Timber.d("Legacy migration: Auto-lock biometric preference is disabled, skipping migration.")
                    return@flatMap Unit.right()
                }

            }
    }
}
