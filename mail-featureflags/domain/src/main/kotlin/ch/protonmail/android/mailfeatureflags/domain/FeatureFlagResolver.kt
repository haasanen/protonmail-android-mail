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

import ch.protonmail.android.mailfeatureflags.domain.annotation.FeatureFlagsCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagResolver @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards FeatureFlagValueProvider>,
    @FeatureFlagsCoroutineScope private val coroutineScope: CoroutineScope
) {

    /**
     * Gets the value of a feature flag by its key, taking provider priorities into account.
     */
    suspend fun getFeatureFlag(key: String, defaultValue: Boolean): Boolean {
        return withContext(coroutineScope.coroutineContext) {
            providers
                .filter { it.isEnabled() }
                .sortedByDescending { it.priority }
                .firstNotNullOfOrNull { provider ->
                    val value = runCatching {
                        provider.getFeatureFlagValue(key)
                    }.getOrNull()

                    Timber.d("'${provider.name}' - Resolved FF '$key': $value")
                    value
                }
                ?: defaultValue
        }
    }
}
