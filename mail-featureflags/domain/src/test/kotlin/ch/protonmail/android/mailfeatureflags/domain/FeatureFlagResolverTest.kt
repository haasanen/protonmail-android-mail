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

package ch.protonmail.android.mailfeatureflags.domain

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FeatureFlagResolverTest {

    @Test
    fun `should default to false when no providers are present`() = runTest {
        // Given
        val resolver = FeatureFlagResolver(emptySet())

        // When + Then
        resolver.observeFeatureFlag(FeatureFlagKey).test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should default to false when no provider is enabled`() = runTest {
        // Given
        val notEnabledProvider1 = mockk<FeatureFlagValueProvider> {
            every { this@mockk.observeFeatureFlagValue(FeatureFlagKey) } returns flowOf(true)
            every { priority } returns 0
            every { isEnabled() } returns false
        }

        val notEnabledProvider2 = mockk<FeatureFlagValueProvider> {
            every { this@mockk.observeFeatureFlagValue(FeatureFlagKey) } returns flowOf(true)
            every { priority } returns 10
            every { isEnabled() } returns false
        }
        val resolver = FeatureFlagResolver(setOf(notEnabledProvider1, notEnabledProvider2))

        // When + Then
        resolver.observeFeatureFlag("some-flag").test {
            assertFalse(awaitItem())
            awaitComplete()
        }

        verify(exactly = 0) { notEnabledProvider1.observeFeatureFlagValue(FeatureFlagKey) }
        verify(exactly = 0) { notEnabledProvider2.observeFeatureFlagValue(FeatureFlagKey) }
    }

    @Test
    fun `should ignore higher priority providers that are not enabled`() = runTest {
        // Given
        val enabledProvider = mockk<FeatureFlagValueProvider> {
            every { this@mockk.observeFeatureFlagValue(FeatureFlagKey) } returns flowOf(true)
            every { priority } returns 0
            every { isEnabled() } returns true
        }

        val notEnabledProvider = mockk<FeatureFlagValueProvider> {
            every { this@mockk.observeFeatureFlagValue(FeatureFlagKey) } returns flowOf(false)
            every { priority } returns 10
            every { isEnabled() } returns false
        }
        val resolver = FeatureFlagResolver(setOf(enabledProvider, notEnabledProvider))

        // When + Then
        resolver.observeFeatureFlag(FeatureFlagKey).test {
            assertTrue(awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { enabledProvider.observeFeatureFlagValue(FeatureFlagKey) }
        verify(exactly = 0) { notEnabledProvider.observeFeatureFlagValue(FeatureFlagKey) }
    }

    @Test
    fun `should return false when the feature key is unknown to the given providers`() = runTest {
        // Given
        val provider = mockk<FeatureFlagValueProvider> {
            every { this@mockk.observeFeatureFlagValue(FeatureFlagKey) } returns null
            every { priority } returns 0
            every { isEnabled() } returns true
        }
        val resolver = FeatureFlagResolver(setOf(provider))

        // When + Then
        resolver.observeFeatureFlag(FeatureFlagKey).test {
            assertFalse(awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { provider.observeFeatureFlagValue(FeatureFlagKey) }
    }

    @Test
    fun `should respect providers priority when resolving a feature flag`() = runTest {
        // Given
        val lowPriorityProvider = mockk<FeatureFlagValueProvider> {
            every { this@mockk.observeFeatureFlagValue(any<String>()) } returns flowOf(false)
            every { priority } returns 0
            every { isEnabled() } returns true
        }

        val topPriorityProvider = mockk<FeatureFlagValueProvider> {
            every { this@mockk.observeFeatureFlagValue(any<String>()) } returns flowOf(true)
            every { priority } returns 10
            every { isEnabled() } returns true
        }

        val resolver = FeatureFlagResolver(setOf(lowPriorityProvider, topPriorityProvider))

        // When + Then
        resolver.observeFeatureFlag(FeatureFlagKey).test {
            assertTrue(awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { topPriorityProvider.observeFeatureFlagValue(FeatureFlagKey) }
        verify(exactly = 0) { lowPriorityProvider.observeFeatureFlagValue(FeatureFlagKey) }
    }

    private companion object {

        const val FeatureFlagKey = "ff-key"
    }
}
