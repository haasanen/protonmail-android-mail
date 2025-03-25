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

package ch.protonmail.android.mailfeatureflags.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import app.cash.turbine.test
import ch.protonmail.android.testdata.featureflags.FeatureFlagDefinitionsTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DataStoreFeatureFlagValueProviderTest {

    private val preferences = mockk<Preferences>()
    private val featureFlagsDataStore = mockk<DataStore<Preferences>>()
    private val dataStoreProvider = mockk<FeatureFlagOverridesDataStoreProvider> {
        every { this@mockk.featureFlagOverrides } returns featureFlagsDataStore
    }

    @Test
    fun `should return an empty map when no override is present for the provided definitions`() = runTest {
        // Given
        coEvery { preferences.contains<Preferences.Key<Boolean>>(any()) } returns false
        coEvery { preferences.get<Preferences.Key<Boolean>>(any()) } returns null

        every { featureFlagsDataStore.data } returns flowOf(preferences)

        val provider = DataStoreFeatureFlagValueProvider(knownFeatureFlagDefinitions, dataStoreProvider)

        // When + Then
        provider.observeAllOverrides().test {
            assertEquals(emptyMap(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should provide overrides when present for any provided definition`() = runTest {
        // Given
        coEvery { preferences.contains<Preferences.Key<Boolean>>(any()) } returns false
        coEvery { preferences.contains(getPreferenceKey(flagDefinition1.key)) } returns true

        coEvery { preferences.get<Preferences.Key<Boolean>>(any()) } returns null
        coEvery { preferences[getPreferenceKey(flagDefinition1.key)] } returns false

        every { featureFlagsDataStore.data } returns flowOf(preferences)

        val expectedMap = mapOf(flagDefinition1 to false)
        val provider = DataStoreFeatureFlagValueProvider(knownFeatureFlagDefinitions, dataStoreProvider)

        // When + Then
        provider.observeAllOverrides().test {
            assertEquals(expectedMap, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should return null when the feature flag key is unknown to the datastore`() = runTest {
        // Given
        coEvery { preferences.contains(getPreferenceKey(flagDefinition2.key)) } returns false
        every { featureFlagsDataStore.data } returns flowOf(preferences)

        val provider = DataStoreFeatureFlagValueProvider(knownFeatureFlagDefinitions, dataStoreProvider)

        // When
        val actual = provider.observeFeatureFlagValue(flagDefinition2.key)

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return the overridden value when the feature flag key is known to the datastore`() = runTest {
        // Given
        coEvery { preferences.contains(getPreferenceKey(flagDefinition2.key)) } returns true
        coEvery { preferences[getPreferenceKey(flagDefinition2.key)] } returns true
        every { featureFlagsDataStore.data } returns flowOf(preferences)

        val provider = DataStoreFeatureFlagValueProvider(knownFeatureFlagDefinitions, dataStoreProvider)

        // When + Then
        provider.observeFeatureFlagValue(flagDefinition2.key)!!.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should update the datastore when toggling is invoked`() = runTest {
        // Given
        coEvery { preferences[getPreferenceKey(flagDefinition2.key)] } returns true
        every { featureFlagsDataStore.data } returns flowOf(preferences)
        coEvery { featureFlagsDataStore.updateData(any()) } returns mockk()

        val provider = DataStoreFeatureFlagValueProvider(knownFeatureFlagDefinitions, dataStoreProvider)

        // When
        provider.toggle(flagDefinition2)

        // Then
        coVerify(exactly = 1) { featureFlagsDataStore.updateData(any()) }
    }

    @Test
    fun `should update the datastore data when resetAll is invoked`() = runTest {
        // Given
        every { featureFlagsDataStore.data } returns flowOf(preferences)
        coEvery { featureFlagsDataStore.updateData(any()) } returns mockk()

        val provider = DataStoreFeatureFlagValueProvider(knownFeatureFlagDefinitions, dataStoreProvider)

        // When
        provider.resetAll()

        // Then
        coVerify(exactly = 1) { featureFlagsDataStore.updateData(any()) }
    }

    private fun getPreferenceKey(key: String) = booleanPreferencesKey("feature_flag_$key")

    private companion object {

        val flagDefinition1 =
            FeatureFlagDefinitionsTestData.buildSystemFeatureFlagDefinition(key = "key", defaultValue = true)

        val flagDefinition2 =
            FeatureFlagDefinitionsTestData.buildSystemFeatureFlagDefinition(key = "key2", defaultValue = false)

        val knownFeatureFlagDefinitions = setOf(flagDefinition1, flagDefinition2)
    }
}
