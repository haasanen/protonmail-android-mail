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

package ch.protonmail.android.mailupselling.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.mailupselling.data.BlackFridayDataStoreProvider
import ch.protonmail.android.mailupselling.domain.model.BlackFridayPhase
import ch.protonmail.android.mailupselling.domain.model.BlackFridaySeenPreference
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BlackFridayLocalDataSourceImplTest {

    private val preferences = mockk<Preferences>()
    private val dataStore = spyk<DataStore<Preferences>> {
        every { data } returns flowOf(preferences)
    }
    private val dataStoreProviderMock = mockk<BlackFridayDataStoreProvider> {
        every { blackFridayDataStore } returns dataStore
    }

    private val dataSource = BlackFridayLocalDataSourceImpl(dataStoreProviderMock)

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return pref with 0L when key value does not exist`() = runTest {
        // Given
        val expected = BlackFridaySeenPreference(BlackFridayPhase.Active.Wave1, 0L)
        every { preferences[longPreferencesKey(BlackFridayDataStoreProvider.PHASE_1_SEEN_KEY)] } returns null

        // When + Then
        dataSource.observePhaseEligibility(BlackFridayPhase.Active.Wave1).test {
            assertEquals(expected.right(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should return pref with actual value when key value exists`() = runTest {
        // Given
        val expected = BlackFridaySeenPreference(BlackFridayPhase.Active.Wave1, 10L)
        every { preferences[longPreferencesKey(BlackFridayDataStoreProvider.PHASE_1_SEEN_KEY)] } returns 10L

        // When + Then
        dataSource.observePhaseEligibility(BlackFridayPhase.Active.Wave1).test {
            assertEquals(expected.right(), awaitItem())
            awaitComplete()
        }
    }
}
