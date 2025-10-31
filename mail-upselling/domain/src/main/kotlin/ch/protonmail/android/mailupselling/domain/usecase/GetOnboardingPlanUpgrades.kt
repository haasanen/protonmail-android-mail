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

package ch.protonmail.android.mailupselling.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailupselling.domain.cache.AvailableUpgradesCache
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeIds
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeSupportedTags
import me.proton.android.core.payment.domain.model.ProductOfferDetail
import me.proton.android.core.payment.domain.model.filterForTags
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetOnboardingPlanUpgrades @Inject constructor(
    private val cache: AvailableUpgradesCache
) {

    suspend operator fun invoke(userId: UserId): Either<GetOnboardingPlansError, List<ProductOfferDetail>> = either {
        val upgrades = cache.get(userId)

        val containsBlackFridayPlans = upgrades.filterForTags(primaryTag = PlanUpgradeSupportedTags.BlackFriday.value)
            .containsTag(PlanUpgradeSupportedTags.BlackFriday)

        // If BF, we should not return plans in onboarding.
        if (containsBlackFridayPlans) raise(GetOnboardingPlansError.UnsupportedBlackFridayFlow)

        val offerDetailsList = upgrades.filterForTags(primaryTag = PlanUpgradeSupportedTags.IntroductoryPrice.value)
            .takeIf { it.containsExpectedInstances() }
            ?: raise(GetOnboardingPlansError.MismatchingPlans)

        offerDetailsList
    }

    private fun List<ProductOfferDetail>.containsExpectedInstances(): Boolean {
        val mailPlusPlans = this.filter { it.metadata.planName == PlanUpgradeIds.PlusPlanId }
        val unlimitedPlans = this.filter { it.metadata.planName == PlanUpgradeIds.UnlimitedPlanId }

        return mailPlusPlans.containsExpectedPlans() && unlimitedPlans.containsExpectedPlans()
    }

    private fun List<ProductOfferDetail>.containsTag(tag: PlanUpgradeSupportedTags) =
        any { it.offer.tags.value.contains(tag.value) }

    private fun List<ProductOfferDetail>.containsExpectedPlans() = size == 2 &&
        this.any { it.offer.renew.cycle in listOf(PlanUpgradeCycle.Monthly.months, PlanUpgradeCycle.Yearly.months) }
}

sealed interface GetOnboardingPlansError {
    data object MismatchingPlans : GetOnboardingPlansError
    data object UnsupportedBlackFridayFlow : GetOnboardingPlansError
}
