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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagResolver @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards FeatureFlagValueProvider>
) {

    /**
     * Resolves the value of a feature flag by its key, taking provider priorities into account.
     */
    fun observeFeatureFlag(key: String): Flow<Boolean> {
        return providers
            .filter { it.isEnabled() }
            .sortedByDescending { it.priority }
            .firstNotNullOfOrNull { it.observeFeatureFlagValue(key) }
            ?.distinctUntilChanged()
            ?: flowOf(false) // Default fallback value if the key isn't found anywhere.
    }
}
