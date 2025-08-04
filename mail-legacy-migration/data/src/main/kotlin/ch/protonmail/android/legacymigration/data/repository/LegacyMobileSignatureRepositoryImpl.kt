/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.legacymigration.data.repository

import arrow.core.Either
import ch.protonmail.android.legacymigration.data.local.signature.mobile.LegacyMobileSignatureLocalDataSource
import ch.protonmail.android.legacymigration.domain.model.LegacyMobileSignaturePreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.repository.LegacyMobileSignatureRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class LegacyMobileSignatureRepositoryImpl @Inject constructor(
    private val localDataSource: LegacyMobileSignatureLocalDataSource
) : LegacyMobileSignatureRepository {

    override suspend fun getMobileSignaturePreference(
        userId: UserId
    ): Either<MigrationError, LegacyMobileSignaturePreference> = localDataSource.getMobileSignaturePreference(userId)

    override fun clearPreferences() = localDataSource.clearPreferences()
}

