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

package ch.protonmail.android.featureflags.presentation.mapper

import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagResolver
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagCategory
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import ch.protonmail.android.mailfeatureflags.presentation.mapper.FeatureFlagsDefinitionsMapper
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagListItem
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagUiModel
import ch.protonmail.android.testdata.featureflags.FeatureFlagDefinitionsTestData.buildFeatureFlagDefinition
import ch.protonmail.android.testdata.featureflags.FeatureFlagDefinitionsTestData.buildSystemFeatureFlagDefinition
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FeatureFlagsDefinitionsMapperTest {

    private val resolver = mockk<FeatureFlagResolver> {
        every { this@mockk.observeFeatureFlag(any()) } returns flowOf(false)
    }

    @Test
    fun `should flatten the grouped definitions into feature flags list items`() = runTest {
        // Given
        val mapper = FeatureFlagsDefinitionsMapper(resolver)

        val systemFlagDefinition = buildSystemFeatureFlagDefinition(key = "1", defaultValue = false)
        val systemFlagDefinition2 = buildSystemFeatureFlagDefinition(key = "2", defaultValue = true)
        val testFlagDefinition = buildFeatureFlagDefinition(
            key = "3",
            category = FeatureFlagCategory.Test,
            defaultValue = false
        )
        val testFlagDefinition2 = buildFeatureFlagDefinition(
            key = "4",
            category = FeatureFlagCategory.Test,
            defaultValue = true
        )

        val groupedDefinitions = mapOf(
            FeatureFlagCategory.Global to listOf(systemFlagDefinition, systemFlagDefinition2),
            FeatureFlagCategory.Test to listOf(testFlagDefinition, testFlagDefinition2)
        )

        val overrides = mapOf(
            systemFlagDefinition to true,
            testFlagDefinition2 to false
        )

        val expectedListItems = listOf(
            FeatureFlagListItem.Header("Global"),
            FeatureFlagListItem.FeatureFlag(
                systemFlagDefinition.toExpectedUiModel(enabled = true, overridden = true)
            ),
            FeatureFlagListItem.FeatureFlag(
                systemFlagDefinition2.toExpectedUiModel(
                    enabled = false,
                    overridden = false
                )
            ),
            FeatureFlagListItem.Header("Test"),
            FeatureFlagListItem.FeatureFlag(
                testFlagDefinition.toExpectedUiModel(enabled = false, overridden = false)
            ),
            FeatureFlagListItem.FeatureFlag(
                testFlagDefinition2.toExpectedUiModel(enabled = false, overridden = true)
            )
        )


        // When
        val actual = mapper.toFlattenedListUiModel(groupedDefinitions, overrides)

        // Then
        assertEquals(expectedListItems, actual)
    }

    private fun FeatureFlagDefinition.toExpectedUiModel(enabled: Boolean, overridden: Boolean) = FeatureFlagUiModel(
        key = key,
        name = name,
        description = description,
        enabled = enabled,
        overridden = overridden
    )
}
