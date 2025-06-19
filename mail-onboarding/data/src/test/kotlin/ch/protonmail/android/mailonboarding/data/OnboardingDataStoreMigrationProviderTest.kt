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

package ch.protonmail.android.mailonboarding.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test

internal class OnboardingDataStoreMigrationProviderTest {

    private val migration = OnboardingDataStoreProvider.OnboardingV6DataStoreMigration()

    @Test
    fun `shouldMigrate returns true when old property exists`() = runTest {
        // Given
        val preferences = preferencesOf(
            booleanPreferencesKey(V6_SHOW_ONBOARDING_KEY) to true
        )

        // When
        val shouldMigrate = migration.shouldMigrate(preferences)

        // Then
        assertTrue(shouldMigrate)
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
    fun `migrate removes the old property but keeps the v7 one if present`() = runTest {
        // Given
        val originalPreferences = preferencesOf(
            booleanPreferencesKey(V6_SHOW_ONBOARDING_KEY) to true,
            booleanPreferencesKey(V7_SHOW_ONBOARDING_KEY) to true
        )

        assertTrue(originalPreferences.contains(booleanPreferencesKey(V6_SHOW_ONBOARDING_KEY)))

        // When
        val migratedPreferences = migration.migrate(originalPreferences)

        // Then
        assertFalse(migratedPreferences.contains(booleanPreferencesKey(V6_SHOW_ONBOARDING_KEY)))
        assertTrue(migratedPreferences.contains(booleanPreferencesKey(V7_SHOW_ONBOARDING_KEY)))
    }

    private companion object {

        const val V6_SHOW_ONBOARDING_KEY = "shouldDisplayOnboardingPrefKey"
        const val V7_SHOW_ONBOARDING_KEY = "ShouldDisplayV7BetaOnboardingPrefKey"
    }
}
