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

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContactsPermissionDataStoreProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val Context.contactsPermissionsStore: DataStore<Preferences> by preferencesDataStore(
        name = "ContactsPermissionStateStore",
        produceMigrations = { _ ->
            listOf(ContactsPermissionMigration())
        }
    )

    val contactsPermissionsStore = context.contactsPermissionsStore

    internal class ContactsPermissionMigration : DataMigration<Preferences> {

        private val v6Preference = booleanPreferencesKey(V6_PERMISSION_INTERACTION_KEY)
        private val v7Preference = longPreferencesKey(V7_PERMISSION_INTERACTION_KEY)

        override suspend fun shouldMigrate(currentData: Preferences) =
            currentData.contains(v6Preference) && !currentData.contains(v7Preference)

        override suspend fun migrate(currentData: Preferences): Preferences {
            val mutablePrefs = currentData.toMutablePreferences()

            val hasV6PreferenceKey = currentData[v6Preference] == true

            if (hasV6PreferenceKey) {
                mutablePrefs[v7Preference] = System.currentTimeMillis()
                mutablePrefs.remove(v6Preference)
            }

            return mutablePrefs.toPreferences()
        }

        override suspend fun cleanUp() = Unit
    }

    internal companion object {

        const val V6_PERMISSION_INTERACTION_KEY = "HasDeniedContactsPermission"
        const val V7_PERMISSION_INTERACTION_KEY = "HasInteractedWithContactPermission"
    }
}
