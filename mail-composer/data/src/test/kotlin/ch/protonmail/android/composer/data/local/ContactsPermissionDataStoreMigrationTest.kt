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

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ContactsPermissionDataStoreMigrationTest {

    private val migration = ContactsPermissionDataStoreProvider.ContactsPermissionMigration()

    @Test
    fun `shouldMigrate returns true when old property exists and new property does not exist`() = runTest {
        // Given
        val preferences = preferencesOf(
            booleanPreferencesKey(OLD_PERMISSION_KEY) to true
        )

        // When
        val shouldMigrate = migration.shouldMigrate(preferences)

        // Then
        assertTrue(shouldMigrate)
    }

    @Test
    fun `shouldMigrate returns false when new property already exists`() = runTest {
        // Given
        val preferences = preferencesOf(
            booleanPreferencesKey(OLD_PERMISSION_KEY) to true,
            longPreferencesKey(NEW_PERMISSION_KEY) to 1234L
        )

        // When
        val shouldMigrate = migration.shouldMigrate(preferences)

        // Then
        assertFalse(shouldMigrate)
    }

    @Test
    fun `shouldMigrate returns false when old property does not exist`() = runTest {
        // Given
        val preferences = preferencesOf()

        // When
        val shouldMigrate = migration.shouldMigrate(preferences)

        // Then
        assertFalse(shouldMigrate)
    }

    @Test
    fun `migrate converts old boolean true to timestamp and removes old property`() = runTest {
        // Given
        val originalPreferences = preferencesOf(
            booleanPreferencesKey(OLD_PERMISSION_KEY) to true
        )

        // When
        val migratedPreferences = migration.migrate(originalPreferences)

        // Then
        assertFalse(migratedPreferences.contains(booleanPreferencesKey(OLD_PERMISSION_KEY)))

        // Check for timestamp > 0 upon migration
        val timestamp = migratedPreferences[longPreferencesKey(NEW_PERMISSION_KEY)]
        assertTrue(timestamp != null && timestamp > 0L)
    }

    @Test
    fun `migrate preserves other preferences during migration`() = runTest {
        // Given
        val otherKey = "SomeOtherPreference"
        val otherValue = "SomeValue"
        val originalPreferences = preferencesOf(
            booleanPreferencesKey(OLD_PERMISSION_KEY) to true,
            stringPreferencesKey(otherKey) to otherValue
        )

        // When
        val migratedPreferences = migration.migrate(originalPreferences)

        // Then
        assertEquals(otherValue, migratedPreferences[stringPreferencesKey(otherKey)])
        assertFalse(migratedPreferences.contains(booleanPreferencesKey(OLD_PERMISSION_KEY)))
        assertTrue(migratedPreferences.contains(longPreferencesKey(NEW_PERMISSION_KEY)))
    }

    private companion object {

        const val OLD_PERMISSION_KEY = "HasDeniedContactsPermission"
        const val NEW_PERMISSION_KEY = "HasInteractedWithContactPermission"
    }
}
