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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import ch.protonmail.android.legacymigration.domain.model.LegacyMigrationStatus
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class LegacyMigrationStatusLocalDataSourceImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val preferences = mockk<Preferences>()
    private val dataStoreSpy = spyk<DataStore<Preferences>> {
        every { data } returns flowOf(preferences)
    }
    private val dataStoreProvider = mockk<LegacyMigrationStatusDataStoreProvider> {
        every { legacyMigrationStatusStore } returns dataStoreSpy
    }

    private val dataSource = LegacyMigrationStatusLocalDataSourceImpl(dataStoreProvider)
    private val key = stringPreferencesKey("LegacyMigrationStatus")

    @Test
    fun `observeMigrationStatus returns NotDone when value is missing`() = runTest {
        // Given
        every { preferences[key] } returns null

        // When
        val result = dataSource.observeMigrationStatus().first()

        // Then
        assertEquals(LegacyMigrationStatus.NotDone, result)
    }

    @Test
    fun `observeMigrationStatus returns correct value`() = runTest {
        // Given
        every { preferences[key] } returns LegacyMigrationStatus.Done.name

        // When
        val result = dataSource.observeMigrationStatus().first()

        // Then
        assertEquals(LegacyMigrationStatus.Done, result)
    }

    @Test
    fun `setMigrationStatus writes correct value`() = runTest {
        // Given
        val transformSlot = slot<suspend (Preferences) -> Preferences>()
        val mutablePreferences = mockk<MutablePreferences>(relaxed = true)

        every { preferences.toMutablePreferences() } returns mutablePreferences
        every { mutablePreferences[stringPreferencesKey("LegacyMigrationStatus")] = any() } returns Unit

        coEvery {
            dataStoreSpy.updateData(capture(transformSlot))
        } coAnswers {
            transformSlot.captured.invoke(preferences)
            preferences
        }

        // When
        dataSource.setMigrationStatus(LegacyMigrationStatus.Done)

        // Then
        coVerify {
            dataStoreSpy.updateData(any())
        }

        verify {
            mutablePreferences[stringPreferencesKey("LegacyMigrationStatus")] = LegacyMigrationStatus.Done.name
        }
    }
}
