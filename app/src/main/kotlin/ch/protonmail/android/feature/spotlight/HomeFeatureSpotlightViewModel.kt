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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailspotlight.domain.usecase.ObserveFeatureSpotlightDisplay
import ch.protonmail.android.mailspotlight.presentation.model.FeatureSpotlightState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeFeatureSpotlightViewModel @Inject constructor(
    observeFeatureSpotlightDisplay: ObserveFeatureSpotlightDisplay
) : ViewModel() {

    val state: StateFlow<FeatureSpotlightState> = observeFeatureSpotlightDisplay().map { preferenceEither ->
        preferenceEither.fold(
            ifLeft = { FeatureSpotlightState.Hide },
            ifRight = { if (it.show) FeatureSpotlightState.Show else FeatureSpotlightState.Hide }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = FeatureSpotlightState.Loading
    )
}
