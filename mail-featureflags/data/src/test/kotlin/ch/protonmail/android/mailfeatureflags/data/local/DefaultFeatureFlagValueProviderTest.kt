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

import app.cash.turbine.test
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagCategory
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DefaultFeatureFlagValueProviderTest {

    @Test
    fun `should return null when the definition is not provided`() = runTest {
        // Given
        val definition = TestFeatureFlagDefinition

        val provider = DefaultFeatureFlagValueProvider(emptySet())

        // When
        assertNull(provider.observeFeatureFlagValue(definition.key))
    }

    @Test
    fun `should return the default value of the given definition`() = runTest {
        // Given
        val definition = TestFeatureFlagDefinition

        val provider = DefaultFeatureFlagValueProvider(setOf(definition))

        // When
        provider.observeFeatureFlagValue(definition.key)!!.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    private object TestFeatureFlagDefinition : FeatureFlagDefinition(
        key = "key",
        name = "name",
        category = FeatureFlagCategory.System,
        description = "description",
        defaultValue = true
    )
}
