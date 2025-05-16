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

import android.content.Context
import ch.protonmail.android.legacymigration.domain.LegacyDBCoroutineScope
import ch.protonmail.android.legacymigration.domain.model.LegacyDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DestroyLegacyDatabases @Inject constructor(
    @ApplicationContext private val context: Context,
    @LegacyDBCoroutineScope private val coroutineScope: CoroutineScope
) {

    operator fun invoke() {
        coroutineScope.launch {
            val dbFile = context.getDatabasePath(LegacyDatabase.Name)

            if (dbFile.exists()) {
                val deleted = context.deleteDatabase(LegacyDatabase.Name)
                if (deleted) {
                    Timber.d("Legacy migration: Successfully deleted legacy database: ${LegacyDatabase.Name}")
                } else {
                    Timber.w("Legacy migration: Failed to delete legacy database: ${LegacyDatabase.Name}")
                }
            } else {
                Timber.w("Legacy migration: Legacy database not found: ${LegacyDatabase.Name}")
            }
        }
    }
}
