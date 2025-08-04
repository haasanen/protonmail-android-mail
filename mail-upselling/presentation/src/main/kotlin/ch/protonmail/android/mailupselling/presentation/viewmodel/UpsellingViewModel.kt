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

package ch.protonmail.android.mailupselling.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.domain.usecase.ObserveMailPlusPlanUpgrades
import ch.protonmail.android.mailupselling.presentation.UpsellingContentReducer
import ch.protonmail.android.mailupselling.presentation.model.UpsellingScreenContentOperation
import ch.protonmail.android.mailupselling.presentation.model.UpsellingScreenContentOperation.UpsellingScreenContentEvent
import ch.protonmail.android.mailupselling.presentation.model.UpsellingScreenContentState
import ch.protonmail.android.mailupselling.presentation.model.UpsellingScreenContentState.Loading
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibilityOverrideSignal
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = UpsellingViewModel.Factory::class)
internal class UpsellingViewModel @AssistedInject constructor(
    @Assisted val upsellingEntryPoint: UpsellingEntryPoint.Feature,
    private val observeMailPlusPlanUpgrades: ObserveMailPlusPlanUpgrades,
    private val upsellingVisibilityOverrideSignal: UpsellingVisibilityOverrideSignal,
    private val upsellingContentReducer: UpsellingContentReducer
) : ViewModel() {

    @AssistedFactory
    interface Factory {

        fun create(upsellingEntryPoint: UpsellingEntryPoint.Feature): UpsellingViewModel
    }

    private val mutableState = MutableStateFlow<UpsellingScreenContentState>(Loading)
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val plans = observeMailPlusPlanUpgrades().first().takeIf { it.isNotEmpty() }
                ?: return@launch emitNewStateFrom(UpsellingScreenContentEvent.LoadingError.NoSubscriptions)

            emitNewStateFrom(UpsellingScreenContentEvent.DataLoaded(plans, upsellingEntryPoint))
        }
    }

    fun overrideUpsellingVisibility() = upsellingVisibilityOverrideSignal.hideUpselling()

    private fun emitNewStateFrom(operation: UpsellingScreenContentOperation) {
        mutableState.update { upsellingContentReducer.newStateFrom(operation) }
    }
}
