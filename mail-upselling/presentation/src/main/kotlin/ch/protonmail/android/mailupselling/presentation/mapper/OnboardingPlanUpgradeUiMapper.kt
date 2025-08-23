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

package ch.protonmail.android.mailupselling.presentation.mapper

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradePlanType
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingPlanUpgradeUiModel
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingPlanUpgradesListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementListUiModel
import me.proton.android.core.payment.domain.model.ProductDetail
import javax.inject.Inject

internal class OnboardingPlanUpgradeUiMapper @Inject constructor(
    private val planInstanceUiMapper: PlanUpgradeInstanceUiModelMapper,
    private val entitlementsUiMapper: PlanUpgradeEntitlementsUiMapper
) {

    fun toUiModel(products: List<ProductDetail>): Either<PlanMappingError, OnboardingPlanUpgradesListUiModel> = either {

        if (products.isEmpty()) raise(PlanMappingError.EmptyList)
        val mailPlans = products.filter { it.planName == PlanUpgradePlanType.MailPlusId }
        val unlimitedPlans = products.filter { it.planName == PlanUpgradePlanType.UnlimitedId }

        val (mailMonthlyPlan, mailYearlyPlan) = createPlanPair(mailPlans).bind()
        val (unlimitedMonthlyPlan, unlimitedYearlyPlan) = createPlanPair(unlimitedPlans).bind()

        val freePlan = OnboardingPlanUpgradeUiModel.Free(
            planName = TextUiModel("Proton Free"),
            entitlements = listOf(
                PlanUpgradeEntitlementListUiModel.Local(
                    TextUiModel.TextRes(R.string.upselling_onboarding_free_entitlement_storage),
                    R.drawable.ic_storage
                ),
                PlanUpgradeEntitlementListUiModel.Local(
                    TextUiModel.TextRes(R.string.upselling_onboarding_free_entitlement_mail),
                    R.drawable.ic_proton_envelope
                )
            ),
            currency = mailPlans.first().price.currency
        )

        OnboardingPlanUpgradesListUiModel(
            listOf(unlimitedMonthlyPlan, mailMonthlyPlan, freePlan),
            listOf(unlimitedYearlyPlan, mailYearlyPlan, freePlan)
        )
    }

    private fun createPlanPair(
        plans: List<ProductDetail>
    ): Either<PlanMappingError, Pair<OnboardingPlanUpgradeUiModel.Paid, OnboardingPlanUpgradeUiModel.Paid>> = either {
        val monthly = toPlanModel(plans, PlanUpgradeCycle.Monthly).bind()
        val yearly = toPlanModel(plans, PlanUpgradeCycle.Yearly).bind()
        monthly to yearly
    }

    private fun toPlanModel(
        filteredPlans: List<ProductDetail>,
        targetCycle: PlanUpgradeCycle
    ): Either<PlanMappingError, OnboardingPlanUpgradeUiModel.Paid> = either {
        val monthlyPlan = filteredPlans.minBy { it.price.cycle }
        val yearlyPlan = filteredPlans.maxBy { it.price.cycle }
        val planType = PlanUpgradePlanType(monthlyPlan.planName) ?: raise(PlanMappingError.InvalidList)
        if (monthlyPlan == yearlyPlan) raise(PlanMappingError.InvalidList)

        val variant = PlanUpgradeMapper.resolveVariant(
            monthlyPlan,
            yearlyPlan,
            UpsellingEntryPoint.PostOnboarding
        )

        val (monthlyUiModel, yearlyUiModel) = planInstanceUiMapper.toUiModel(
            monthlyPlan,
            yearlyPlan
        )

        val (selectedPlanInstance, selectedPlan) = when (targetCycle) {
            PlanUpgradeCycle.Monthly -> monthlyUiModel to monthlyPlan
            PlanUpgradeCycle.Yearly -> yearlyUiModel to yearlyPlan
        }

        return OnboardingPlanUpgradeUiModel.Paid(
            planType = planType,
            entitlements = entitlementsUiMapper.toOnboardingUiModel(selectedPlan),
            variant = variant,
            cycle = targetCycle,
            planInstance = selectedPlanInstance
        ).right()
    }
}
