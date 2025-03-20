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

package ch.protonmail.android.mailnotifications.data.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import ch.protonmail.android.mailnotifications.data.local.NotificationPermissionsKeys.NumberOfAttempts
import ch.protonmail.android.mailnotifications.data.model.NotificationsPermissionRequestAttempts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationsPermissionLocalDataSourceImpl @Inject constructor(
    private val dataStoreProvider: NotificationDataStoreProvider
) : NotificationsPermissionLocalDataSource {

    override fun observePermissionRequestAttempts(): Flow<NotificationsPermissionRequestAttempts> {
        return dataStoreProvider.notificationPermissionStore.data.map { preferences ->
            val attempts = preferences[intPreferencesKey(NumberOfAttempts)] ?: 0

            NotificationsPermissionRequestAttempts(attempts)
        }
    }

    override suspend fun increasePermissionRequestAttempts() {
        dataStoreProvider.notificationPermissionStore.edit { preferences ->
            val currentValue = preferences[intPreferencesKey(NumberOfAttempts)] ?: 0
            preferences[intPreferencesKey(NumberOfAttempts)] = currentValue + 1
        }
    }
}

private object NotificationPermissionsKeys {

    const val NumberOfAttempts = "NotificationsPermissionAttemptsNumber"
}
