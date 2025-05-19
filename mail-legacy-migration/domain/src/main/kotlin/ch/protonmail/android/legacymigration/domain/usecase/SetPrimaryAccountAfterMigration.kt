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

import ch.protonmail.android.legacymigration.domain.model.AccountMigrationInfo
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import javax.inject.Inject
import timber.log.Timber

class SetPrimaryAccountAfterMigration @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) {
    suspend operator fun invoke(migratedAccountList: List<AccountMigrationInfo>) {

        val primaryAccount = migratedAccountList.firstOrNull { it.isPrimaryUser }

        primaryAccount?.let {
            userSessionRepository.getUserId(primaryAccount.sessionId)?.let { primaryUserId ->
                Timber.d("Legacy migration: Setting primary user after migration")
                userSessionRepository.setPrimaryAccount(primaryUserId)
            } ?: {
                Timber.e("Legacy migration: Failed to set primary user, user not found!")
            }
        } ?: {
            Timber.e("Legacy migration: No primary account found after migration")
        }
    }
}
