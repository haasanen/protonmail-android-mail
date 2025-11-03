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

package ch.protonmail.android.mailupselling.presentation.model.onboarding

import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import me.proton.android.core.payment.domain.model.ProductOfferDetail
import me.proton.core.domain.entity.UserId

internal interface OnboardingUpsellState {

    data object Loading : OnboardingUpsellState

    data object Error : OnboardingUpsellState
    data object UnsupportedFlow : OnboardingUpsellState

    data class Data(
        val planUiModels: OnboardingPlanUpgradesListUiModel
    ) : OnboardingUpsellState
}

sealed interface OnboardingUpsellOperation {

    sealed interface OnboardingUpsellEvent : OnboardingUpsellOperation {

        data class DataLoaded(
            val userId: UserId,
            val productDetails: List<ProductOfferDetail>,
            val upsellingEntryPoint: UpsellingEntryPoint = UpsellingEntryPoint.PostOnboarding
        ) : OnboardingUpsellEvent

        sealed interface LoadingError : OnboardingUpsellEvent {
            data object NoUserId : LoadingError
        }

        sealed interface UnsupportedFlow : OnboardingUpsellEvent {
            data object NotEnabled : UnsupportedFlow
            data object PaidUser : UnsupportedFlow
            data object PlansMismatch : UnsupportedFlow
        }
    }
}
