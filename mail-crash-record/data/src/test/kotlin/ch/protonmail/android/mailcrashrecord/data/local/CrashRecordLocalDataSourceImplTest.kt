/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailcrashrecord.data.local

import java.io.IOException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.PreferencesError
import ch.protonmail.android.mailcrashrecord.data.CrashRecordDataStoreProvider
import ch.protonmail.android.mailcrashrecord.domain.model.MessageBodyWebViewCrash
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import kotlin.test.Test
import kotlin.test.assertEquals

class CrashRecordLocalDataSourceImplTest {

    private val preferences = mockk<Preferences>()
    private val crashRecordDataStoreSpy = spyk<DataStore<Preferences>>()
    private val dataStoreProvider = mockk<CrashRecordDataStoreProvider> {
        every { this@mockk.crashRecordDataStore } returns crashRecordDataStoreSpy
    }

    private val crashRecordLocalDataSource: CrashRecordLocalDataSource =
        CrashRecordLocalDataSourceImpl(dataStoreProvider)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `returns false when no preference is stored locally`() = runTest {
        // Given
        coEvery { preferences.get<Boolean>(any()) } returns null
        every { crashRecordDataStoreSpy.data } returns flowOf(preferences)

        // When
        val actual = crashRecordLocalDataSource.get()

        // Then
        assertEquals(MessageBodyWebViewCrash(false).right(), actual)
    }

    @Test
    fun `returns locally stored preference from data store when available`() = runTest {
        // Given
        coEvery {
            preferences[booleanPreferencesKey(CrashRecordDataStoreProvider.MESSAGE_BODY_WEB_VIEW_CRASH_KEY)]
        } returns true
        every { crashRecordDataStoreSpy.data } returns flowOf(preferences)

        // When
        val actual = crashRecordLocalDataSource.get()

        // Then
        assertEquals(MessageBodyWebViewCrash(true).right(), actual)
    }

    @Test
    fun `should return error when an exception is thrown when getting preference`() = runTest {
        // Given
        every { crashRecordDataStoreSpy.data } returns flow { throw IOException() }

        // When
        val actual = crashRecordLocalDataSource.get()

        // Then
        assertEquals(PreferencesError.left(), actual)
    }

    @Test
    fun `should return success when preference is saved`() = runTest {
        // Given
        val expectedResult = Unit.right()
        val messageBodyWebViewCrash = MessageBodyWebViewCrash(hasCrashed = true)

        // When
        val result = crashRecordLocalDataSource.save(messageBodyWebViewCrash)

        // Then
        coVerify { crashRecordDataStoreSpy.updateData(any()) }
        assertEquals(expectedResult, result)
    }

    @Test
    fun `should return failure when an exception is thrown when saving preference`() = runTest {
        // Given
        val expectedResult = PreferencesError.left()
        val messageBodyWebViewCrash = MessageBodyWebViewCrash(hasCrashed = true)
        coEvery { crashRecordDataStoreSpy.updateData(any()) } throws IOException()

        // When
        val actual = crashRecordLocalDataSource.save(messageBodyWebViewCrash)

        // Then
        assertEquals(expectedResult, actual)
    }
}
