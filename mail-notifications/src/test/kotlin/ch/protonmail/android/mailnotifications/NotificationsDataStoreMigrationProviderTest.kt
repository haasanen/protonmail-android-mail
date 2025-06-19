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

package ch.protonmail.android.mailnotifications

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import ch.protonmail.android.mailnotifications.data.local.NotificationDataStoreProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NotificationsDataStoreMigrationProviderTest {

    private val migration = NotificationDataStoreProvider.NotificationsV6DataStoreMigration()

    @Test
    fun `shouldMigrate returns true when old timestamp property exist`() = runTest {
        // Given
        val preferences = preferencesOf(
            longPreferencesKey(V6_KEY_NOTIFICATIONS_TIMESTAMP) to 123L
        )

        // When
        val shouldMigrate = migration.shouldMigrate(preferences)

        // Then
        assertTrue(shouldMigrate)
    }

    @Test
    fun `shouldMigrate returns true when old do not show property exist`() = runTest {
        // Given
        val preferences = preferencesOf(
            booleanPreferencesKey(V6_KEY_NOTIFICATIONS_STOP_SHOWING) to true
        )

        // When
        val shouldMigrate = migration.shouldMigrate(preferences)

        // Then
        assertTrue(shouldMigrate)
    }

    @Test
    fun `shouldMigrate returns false when old properties do not exist`() = runTest {
        // Given
        val preferences = preferencesOf()

        // When
        val shouldMigrate = migration.shouldMigrate(preferences)

        // Then
        assertFalse(shouldMigrate)
    }

    @Test
    fun `migrate removes the old properties but keeps the v7 one if present`() = runTest {
        // Given
        val originalPreferences = preferencesOf(
            longPreferencesKey(V6_KEY_NOTIFICATIONS_TIMESTAMP) to 123L,
            booleanPreferencesKey(V6_KEY_NOTIFICATIONS_STOP_SHOWING) to true,
            intPreferencesKey(V7_NOTIFICATIONS_REQUESTS_ATTEMPT) to 1
        )

        // When
        val migratedPreferences = migration.migrate(originalPreferences)

        // Then
        assertFalse(migratedPreferences.contains(booleanPreferencesKey(V6_KEY_NOTIFICATIONS_TIMESTAMP)))
        assertFalse(migratedPreferences.contains(booleanPreferencesKey(V6_KEY_NOTIFICATIONS_STOP_SHOWING)))
        assertEquals(1, migratedPreferences[intPreferencesKey(V7_NOTIFICATIONS_REQUESTS_ATTEMPT)])
    }

    private companion object {

        const val V6_KEY_NOTIFICATIONS_TIMESTAMP = "NotificationPermissionTimestampKey"
        const val V6_KEY_NOTIFICATIONS_STOP_SHOWING = "ShouldStopShowingPermissionDialog"
        const val V7_NOTIFICATIONS_REQUESTS_ATTEMPT = "NotificationsPermissionAttemptsNumber"
    }
}
