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

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagProviderPriority
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreFeatureFlagValueProvider @Inject constructor(
    private val definitions: Set<@JvmSuppressWildcards FeatureFlagDefinition>,
    private val dataStoreProvider: FeatureFlagOverridesDataStoreProvider
) : FeatureFlagValueProvider {

    override val priority: Int = FeatureFlagProviderPriority.DataStoreProvider

    override val name: String = "Override FF provider"

    fun observeAllOverrides(): Flow<Map<FeatureFlagDefinition, Boolean>> {
        return dataStoreProvider.featureFlagOverrides.data.map { preferences ->
            val definitionToOverridePairs = definitions.associateWith { definition ->
                val prefKey = getPreferenceKey(definition.key)
                val hasOverride = preferences.contains(prefKey)
                val value = preferences[prefKey] ?: false

                Pair(hasOverride, value)
            }

            val overriddenDefinitionPairs = definitionToOverridePairs.filterValues { (hasOverride, _) -> hasOverride }
            val overriddenFeatureFlags = overriddenDefinitionPairs.mapValues { (_, pair) -> pair.second }

            overriddenFeatureFlags
        }
    }

    override suspend fun getFeatureFlagValue(key: String): Boolean? {
        val prefKey = getPreferenceKey(key)

        val preferences = dataStoreProvider.featureFlagOverrides.data.first()

        // Check if the key exists
        if (!preferences.contains(prefKey)) {
            return null // Let the next provider handle it
        }

        return preferences[prefKey]
    }

    suspend fun toggle(definition: FeatureFlagDefinition, defaultValue: Boolean? = false) {
        val prefKey = getPreferenceKey(definition.key)
        dataStoreProvider.featureFlagOverrides.edit {
            val currentValue = it[prefKey] ?: defaultValue ?: false
            it[prefKey] = !currentValue
        }
    }

    suspend fun resetAll() {
        dataStoreProvider.featureFlagOverrides.edit { it.clear() }
    }

    private fun getPreferenceKey(key: String) = booleanPreferencesKey("feature_flag_$key")
}
