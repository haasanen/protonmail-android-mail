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
import ch.protonmail.android.legacymigration.domain.model.toAutoLockPin
import ch.protonmail.android.legacymigration.domain.repository.LegacyAutoLockRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

class MigrateLegacyAutoLockPinCode @Inject constructor(
    private val legacyAutoLockRepository: LegacyAutoLockRepository,
    private val userSessionRepository: UserSessionRepository
) {
    suspend operator fun invoke(): Either<MigrationError, Unit> {
        if (!legacyAutoLockRepository.hasAutoLockPinCode()) {
            Timber.d("Legacy migration: No auto-lock pin code found, skipping migration.")
            return Unit.right()
        }

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
}
