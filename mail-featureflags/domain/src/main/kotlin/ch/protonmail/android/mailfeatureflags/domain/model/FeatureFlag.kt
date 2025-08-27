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

package ch.protonmail.android.mailfeatureflags.domain.model

import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagResolver

/**
 * The base FeatureFlag entity.
 *
 * There is some minor overlaps with [FeatureFlagDefinition] which needs to be addressed.
 */
class FeatureFlag<T>(
    private val key: String,
    private val defaultValue: T,
    private val resolver: FeatureFlagResolver
) {
    suspend fun get(): T {
        return when (defaultValue) {
            is Boolean -> resolver.getFeatureFlag(key, defaultValue) as T
            // Add other types as needed
            else -> defaultValue
        }
    }
}
