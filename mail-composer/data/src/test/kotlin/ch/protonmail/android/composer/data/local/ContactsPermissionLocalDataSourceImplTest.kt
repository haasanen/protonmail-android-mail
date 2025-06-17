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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.local.ContactsPermissionDataStoreProvider.Companion.V7_PERMISSION_INTERACTION_KEY
import ch.protonmail.android.mailcommon.domain.model.DataError
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class ContactsPermissionLocalDataSourceImplTest {
    private val preferences = mockk<Preferences>()
    private val contactsPermissionDataStoreSpy = spyk<DataStore<Preferences>> {
        every { data } returns flowOf(preferences)
    }
    private val dataStoreProviderMock = mockk<ContactsPermissionDataStoreProvider> {
        every { contactsPermissionsStore } returns contactsPermissionDataStoreSpy
    }

    private val dataSource = ContactsPermissionLocalDataSourceImpl(dataStoreProviderMock)

    @Test
    fun `should return data when present`() = runTest {
        // Given
        val expectedState = true
        every { preferences[longPreferencesKey(V7_PERMISSION_INTERACTION_KEY)] } returns 1234L

        // When
        val actual = dataSource.observePermissionInteraction().first()

        // Then
        assertEquals(expectedState.right(), actual)
    }

    @Test
    fun `should return an error when data is not present`() = runTest {
        every { preferences[longPreferencesKey(V7_PERMISSION_INTERACTION_KEY)] } returns null

        // When
        val actual = dataSource.observePermissionInteraction().first()

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }

    @Test
    fun `should save the timestamp when invoked`() = runTest {
        val mutablePreferences = mockk<MutablePreferences>()

        every { preferences.toMutablePreferences() } returns mutablePreferences
        every { mutablePreferences[any<Preferences.Key<Long>>()] = any() } returns Unit

        // When
        dataSource.trackPermissionInteraction()

        // Then
        coVerify {
            contactsPermissionDataStoreSpy.updateData(any())
        }
    }
}
