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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NotificationsPermissionLocalDataSourceImplTest {

    private val dataStoreProvider = mockk<NotificationDataStoreProvider>()
    private val dataStore = mockk<DataStore<Preferences>>()
    private val attemptsKey = intPreferencesKey("NotificationsPermissionAttemptsNumber")

    private lateinit var dataSource: NotificationsPermissionLocalDataSourceImpl

    @Before
    fun setup() {
        every { dataStoreProvider.notificationPermissionStore } returns dataStore
        dataSource = NotificationsPermissionLocalDataSourceImpl(dataStoreProvider)
    }

    @Test
    fun `observePermissionRequestAttempts returns correct attempts from preferences`() = runTest {
        // Given
        val preferences = preferencesOf(attemptsKey to 3)
        every { dataStore.data } returns flowOf(preferences)

        // When
        val result = dataSource.observePermissionRequestAttempts().first()

        // Then
        assertEquals(3, result.value)
    }

    @Test
    fun `observePermissionRequestAttempts returns zero when no attempts stored`() = runTest {
        // Given
        val emptyPreferences = preferencesOf()
        every { dataStore.data } returns flowOf(emptyPreferences)

        // When
        val result = dataSource.observePermissionRequestAttempts().first()

        // Then
        assertEquals(0, result.value)
    }

    @Test
    fun `increasePermissionRequestAttempts calls updateData`() = runBlocking {
        // Given
        coEvery {
            dataStore.updateData(any<suspend (Preferences) -> Preferences>())
        } returns mockk()

        // When
        dataSource.increasePermissionRequestAttempts()

        // Then
        coVerify { dataStore.updateData(any()) }
    }
}
