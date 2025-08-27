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

package ch.protonmail.android.mailfeatureflags.presentation.mapper

import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagResolver
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagCategory
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagListItem
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

data class FeatureFlagsDefinitionsMapper @Inject constructor(
    private val resolver: FeatureFlagResolver
) {

    suspend fun toFlattenedListUiModel(
        groupedDefinitions: Map<FeatureFlagCategory, List<@JvmSuppressWildcards FeatureFlagDefinition>>,
        overrides: Map<FeatureFlagDefinition, Boolean>
    ): ImmutableList<FeatureFlagListItem> {
        return groupedDefinitions.flatMap { (category, flags) ->
            buildList {
                add(FeatureFlagListItem.Header(category.name))
                addAll(
                    flags.mapToUiModel(overrides).map { uiModel ->
                        FeatureFlagListItem.FeatureFlag(uiModel)
                    }
                )
            }
        }.toImmutableList()
    }

    private suspend fun List<@JvmSuppressWildcards FeatureFlagDefinition>.mapToUiModel(
        overrides: Map<FeatureFlagDefinition, Boolean>
    ) = this.map { definition ->
        val isOverridden = overrides.containsKey(definition)
        val isEnabled = if (isOverridden) {
            overrides[definition] ?: definition.defaultValue
        } else {
            resolver.getFeatureFlag(definition.key, definition.defaultValue)
        }

        FeatureFlagUiModel(
            key = definition.key,
            name = definition.name,
            description = definition.description,
            enabled = isEnabled,
            overridden = isOverridden
        )
    }
}
