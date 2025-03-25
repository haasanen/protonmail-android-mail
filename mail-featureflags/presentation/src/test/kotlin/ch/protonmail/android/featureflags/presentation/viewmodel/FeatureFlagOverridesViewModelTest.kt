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

package ch.protonmail.android.featureflags.presentation.viewmodel

import app.cash.turbine.test
import ch.protonmail.android.mailfeatureflags.data.local.DataStoreFeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagCategory
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagListItem
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagOverridesState
import ch.protonmail.android.mailfeatureflags.presentation.viewmodel.FeatureFlagOverridesViewModel
import ch.protonmail.android.mailfeatureflags.presentation.mapper.FeatureFlagsDefinitionsMapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.featureflags.FeatureFlagDefinitionsTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FeatureFlagOverridesViewModelTest {

    private val mapper = mockk<FeatureFlagsDefinitionsMapper>()
    private val dataStoreProvider = mockk<DataStoreFeatureFlagValueProvider>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should expose a loaded state with the mapped list items`() = runTest {
        // Given
        val systemFlag = FeatureFlagDefinitionsTestData.buildSystemFeatureFlagDefinition(key = "1")
        val testFlag = FeatureFlagDefinitionsTestData.buildFeatureFlagDefinition(
            key = "2",
            category = FeatureFlagCategory.Test
        )

        val definitions = setOf(systemFlag, testFlag)
        val expectedOverrides = mapOf(systemFlag to false)
        val expectedUiModelsList = mockk<ImmutableList<FeatureFlagListItem>>()

        every { dataStoreProvider.observeAllOverrides() } returns flowOf(expectedOverrides)
        coEvery { mapper.toFlattenedListUiModel(any(), expectedOverrides) } returns expectedUiModelsList

        // When + Then
        viewModel(definitions).state.test {
            assertEquals(FeatureFlagOverridesState.Loaded(expectedUiModelsList), awaitItem())
        }
    }

    @Test
    fun `should do nothing when the key is not within the definitions`() = runTest {
        // Given
        val flag = FeatureFlagDefinitionsTestData.buildSystemFeatureFlagDefinition(key = "1")
        val definitions = setOf(flag)
        every { dataStoreProvider.observeAllOverrides() } returns flowOf()

        // When
        viewModel(definitions).toggleKey("unknownKey")

        // Then
        coVerify(exactly = 0) { dataStoreProvider.toggle(flag, flag.defaultValue) }
    }

    @Test
    fun `should call the datastore when toggling a key`() {
        // Given
        val flag = FeatureFlagDefinitionsTestData.buildSystemFeatureFlagDefinition(key = "1")
        val definitions = setOf(flag)
        every { dataStoreProvider.observeAllOverrides() } returns flowOf()
        coEvery { dataStoreProvider.toggle(flag, flag.defaultValue) } just runs

        // When
        viewModel(definitions).toggleKey(flag.key)

        // Then
        coVerify(exactly = 1) { dataStoreProvider.toggle(flag, flag.defaultValue) }
    }

    @Test
    fun `should call the datastore when resetting all overrides`() {
        // Given
        val flag = FeatureFlagDefinitionsTestData.buildSystemFeatureFlagDefinition(key = "1")
        val definitions = setOf(flag)

        every { dataStoreProvider.observeAllOverrides() } returns flowOf()
        coEvery { dataStoreProvider.resetAll() } just runs

        // When
        viewModel(definitions).resetAll()

        // Then
        coVerify(exactly = 1) { dataStoreProvider.resetAll() }
    }

    private fun viewModel(definitions: Set<FeatureFlagDefinition>) = FeatureFlagOverridesViewModel(
        definitions,
        dataStoreProvider,
        mapper
    )
}
