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

package ch.protonmail.android.feature.spotlight

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.PreferencesError
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailspotlight.domain.model.FeatureSpotlightDisplay
import ch.protonmail.android.mailspotlight.domain.usecase.ObserveFeatureSpotlightDisplay
import ch.protonmail.android.mailspotlight.presentation.model.FeatureSpotlightState
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HomeFeatureSpotlightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockFeatureFlag = mockk<FeatureFlag<Boolean>>()
    private val mockObserveFeatureSpotlightDisplay = mockk<ObserveFeatureSpotlightDisplay>()

    @Test
    fun `should emit Hide when feature flag is disabled`() = runTest {
        // Given
        coEvery { mockFeatureFlag.get() } returns false

        val viewModel = buildViewModel()

        // When/Then
        viewModel.state.test {
            assertEquals(FeatureSpotlightState.Hide, awaitItem())
        }
    }

    @Test
    fun `should emit Show when feature flag is enabled and preference is show`() = runTest {
        // Given
        coEvery { mockFeatureFlag.get() } returns true
        every { mockObserveFeatureSpotlightDisplay() } returns flowOf(FeatureSpotlightDisplay(show = true).right())

        val viewModel = buildViewModel()

        // When/Then
        viewModel.state.test {
            assertEquals(FeatureSpotlightState.Show, awaitItem())
        }
    }

    @Test
    fun `should emit Hide when feature flag is enabled and preference is hide`() = runTest {
        // Given
        coEvery { mockFeatureFlag.get() } returns true
        every { mockObserveFeatureSpotlightDisplay() } returns flowOf(FeatureSpotlightDisplay(show = false).right())

        val viewModel = buildViewModel()

        // When/Then
        viewModel.state.test {
            assertEquals(FeatureSpotlightState.Hide, awaitItem())
        }
    }

    @Test
    fun `should emit Hide when feature flag is enabled and preference returns error`() = runTest {
        // Given
        coEvery { mockFeatureFlag.get() } returns true
        every { mockObserveFeatureSpotlightDisplay() } returns flowOf(PreferencesError.left())

        val viewModel = buildViewModel()

        // When/Then
        viewModel.state.test {
            assertEquals(FeatureSpotlightState.Hide, awaitItem())
        }
    }

    private fun buildViewModel() = HomeFeatureSpotlightViewModel(
        observeFeatureSpotlightDisplay = mockObserveFeatureSpotlightDisplay,
        isEnabled = mockFeatureFlag
    )
}
