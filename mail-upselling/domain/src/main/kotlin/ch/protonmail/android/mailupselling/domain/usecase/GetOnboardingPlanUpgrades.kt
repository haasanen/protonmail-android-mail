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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailupselling.domain.cache.AvailableUpgradesCache
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeIds
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetOnboardingPlanUpgrades @Inject constructor(
    private val cache: AvailableUpgradesCache
) {

    suspend operator fun invoke(userId: UserId): Either<GetOnboardingPlansError, List<ProductDetail>> {
        val upgrades = cache.get(userId)

        return if (!upgrades.containsExpectedInstances()) {
            GetOnboardingPlansError.MismatchingPlans.left()
        } else {
            upgrades.right()
        }
    }

    private fun List<ProductDetail>.containsExpectedInstances(): Boolean {
        val mailPlusPlans = this.filter { it.planName == PlanUpgradeIds.PlusPlanId }
        val unlimitedPlans = this.filter { it.planName == PlanUpgradeIds.UnlimitedPlanId }

        return mailPlusPlans.assertExpectedPlans() && unlimitedPlans.assertExpectedPlans()
    }

    private fun List<ProductDetail>.assertExpectedPlans() = size == 2 &&
        this.any { it.renew.cycle in listOf(PlanUpgradeCycle.Monthly.months, PlanUpgradeCycle.Yearly.months) }
}

sealed interface GetOnboardingPlansError {
    data object MismatchingPlans : GetOnboardingPlansError
}
