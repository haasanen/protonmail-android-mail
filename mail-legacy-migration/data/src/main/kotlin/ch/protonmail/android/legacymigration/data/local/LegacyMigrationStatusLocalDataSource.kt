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

package ch.protonmail.android.legacymigration.data.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import ch.protonmail.android.legacymigration.domain.model.LegacyMigrationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface LegacyMigrationStatusLocalDataSource {
    fun observeMigrationStatus(): Flow<LegacyMigrationStatus>
    suspend fun setMigrationStatus(status: LegacyMigrationStatus)
}

class LegacyMigrationStatusLocalDataSourceImpl @Inject constructor(
    private val dataStoreProvider: LegacyMigrationStatusDataStoreProvider
) : LegacyMigrationStatusLocalDataSource {

    private val dataStore = dataStoreProvider.legacyMigrationStatusStore
    private val migrationStatusKey = stringPreferencesKey(LEGACY_MIGRATION_STATUS_PREF_KEY)

    override fun observeMigrationStatus(): Flow<LegacyMigrationStatus> = dataStore.data.map { prefs ->
        LegacyMigrationStatus.fromString(prefs[migrationStatusKey])
    }

    override suspend fun setMigrationStatus(status: LegacyMigrationStatus) {
        dataStore.edit { prefs ->
            prefs[migrationStatusKey] = status.name
        }
    }
}

internal const val LEGACY_MIGRATION_STATUS_PREF_KEY = "LegacyMigrationStatus"
