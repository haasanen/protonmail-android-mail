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

package ch.protonmail.android.mailupselling.presentation

import ch.protonmail.android.mailupselling.presentation.mapper.OnboardingPlanUpgradeUiMapper
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellOperation
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellOperation.OnboardingUpsellEvent
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellState
import timber.log.Timber
import javax.inject.Inject

internal class OnboardingUpsellingReducer @Inject constructor(
    private val mapper: OnboardingPlanUpgradeUiMapper
) {

    suspend fun newStateFrom(operation: OnboardingUpsellOperation): OnboardingUpsellState {
        return when (operation) {
            is OnboardingUpsellEvent.DataLoaded -> reducePlansListToNewState(operation)
            is OnboardingUpsellEvent.LoadingError -> reduceErrorEvent(operation)
            is OnboardingUpsellEvent.UnsupportedFlow -> reduceUnsupportedFlow(operation)
        }
    }

    private suspend fun reducePlansListToNewState(operation: OnboardingUpsellEvent.DataLoaded): OnboardingUpsellState {
        val uiModel = mapper.toUiModel(operation.productDetails)

        return uiModel.fold(
            ifLeft = {
                OnboardingUpsellState.Error
            },
            ifRight = {
                OnboardingUpsellState.Data(it)
            }
        )
    }

    private fun reduceErrorEvent(event: OnboardingUpsellEvent.LoadingError) = when (event) {
        OnboardingUpsellEvent.LoadingError.NoUserId -> OnboardingUpsellState.Error
    }

    private fun reduceUnsupportedFlow(event: OnboardingUpsellEvent.UnsupportedFlow): OnboardingUpsellState {
        Timber.d("Unsupported upselling flow - $event")
        return OnboardingUpsellState.UnsupportedFlow
    }
}
