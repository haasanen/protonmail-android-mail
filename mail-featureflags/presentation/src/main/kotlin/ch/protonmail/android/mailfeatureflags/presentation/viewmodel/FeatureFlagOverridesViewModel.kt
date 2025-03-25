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

package ch.protonmail.android.mailfeatureflags.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailfeatureflags.data.local.DataStoreFeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition
import ch.protonmail.android.mailfeatureflags.presentation.mapper.FeatureFlagsDefinitionsMapper
import ch.protonmail.android.mailfeatureflags.presentation.model.FeatureFlagOverridesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeatureFlagOverridesViewModel @Inject constructor(
    private val definitions: Set<@JvmSuppressWildcards FeatureFlagDefinition>,
    private val dataStoreProvider: DataStoreFeatureFlagValueProvider,
    private val mapper: FeatureFlagsDefinitionsMapper
) : ViewModel() {

    val state: StateFlow<FeatureFlagOverridesState> =
        dataStoreProvider.observeAllOverrides().flatMapLatest { overrides ->
            val groupedDefinitions = definitions.groupBy { it.category }
            val listItems = mapper.toFlattenedListUiModel(groupedDefinitions, overrides)
            flowOf(FeatureFlagOverridesState.Loaded(listItems))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeatureFlagOverridesState.Loading
        )

    fun toggleKey(key: String) {
        val definition = definitions.firstOrNull { it.key == key } ?: return
        viewModelScope.launch { dataStoreProvider.toggle(definition, definition.defaultValue) }
    }

    fun resetAll() {
        viewModelScope.launch { dataStoreProvider.resetAll() }
    }
}
