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

package ch.protonmail.android.composer.data.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ContactsPermissionLocalDataSource {

    fun observePermissionInteraction(): Flow<Either<DataError, Boolean>>
    suspend fun trackPermissionInteraction()
}

class ContactsPermissionLocalDataSourceImpl @Inject constructor(
    private val dataStoreProvider: ContactsPermissionDataStoreProvider
) : ContactsPermissionLocalDataSource {

    override fun observePermissionInteraction() = dataStoreProvider.contactsPermissionsStore.data.map { prefs ->
        prefs[longPreferencesKey(HAS_INTERACTED_WITH_CONTACT_PERMISSION)]?.let { timestamp ->
            (timestamp > 0L).right()
        } ?: DataError.Local.NoDataCached.left()
    }

    override suspend fun trackPermissionInteraction() {
        dataStoreProvider.contactsPermissionsStore.edit { prefs ->
            prefs[longPreferencesKey(HAS_INTERACTED_WITH_CONTACT_PERMISSION)] = System.currentTimeMillis()
        }
    }
}

private const val HAS_INTERACTED_WITH_CONTACT_PERMISSION = "HasInteractedWithContactPermission"
