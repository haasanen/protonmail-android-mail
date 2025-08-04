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
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeVariant
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.ProductInstances
import me.proton.android.core.payment.domain.model.ProductDetail
import javax.inject.Inject

internal class PlanUpgradeUiMapper @Inject constructor(
    private val iconUiMapper: PlanUpgradeIconUiMapper,
    private val titleUiMapper: PlanUpgradeTitleUiMapper,
    private val descriptionUiMapper: PlanUpgradeDescriptionUiMapper,
    private val planInstanceUiMapper: PlanUpgradeInstanceUiModelMapper,
    private val entitlementsUiMapper: PlanUpgradeEntitlementsUiMapper
) {

    fun toUiModel(
        products: ProductInstances,
        upsellingEntryPoint: UpsellingEntryPoint.Feature
    ): Either<PlanMappingError, PlanUpgradeUiModel> = either {
        if (products.instances.isEmpty()) raise(PlanMappingError.EmptyList)

        val monthlyPlan = products.instances.minBy { it.price.cycle }
        val yearlyPlan = products.instances.maxBy { it.price.cycle }

        if (monthlyPlan == yearlyPlan) raise(PlanMappingError.InvalidList)

        val variant = resolveVariant(
            monthlyPlan,
            yearlyPlan,
            upsellingEntryPoint
        )

        val (shorterCycleUiModel, longerCycleUiModel) = planInstanceUiMapper.toUiModel(
            monthlyPlan,
            yearlyPlan
        )

        return PlanUpgradeUiModel(
            icon = iconUiMapper.toUiModel(upsellingEntryPoint, variant),
            title = titleUiMapper.toUiModel(shorterCycleUiModel.primaryPrice, upsellingEntryPoint, variant),
            description = descriptionUiMapper.toUiModel(monthlyPlan, upsellingEntryPoint, variant),
            entitlements = entitlementsUiMapper.toUiModel(monthlyPlan, upsellingEntryPoint),
            variant = variant,
            list = resolveListUiModel(shorterCycleUiModel, longerCycleUiModel, variant)
        ).right()
    }

    private fun resolveListUiModel(
        shorterCycleUiModel: PlanUpgradeInstanceUiModel,
        longerCycleUiModel: PlanUpgradeInstanceUiModel,
        variant: PlanUpgradeVariant
    ): PlanUpgradeInstanceListUiModel.Data {
        return when {
            variant == PlanUpgradeVariant.SocialProof -> {
                PlanUpgradeInstanceListUiModel.Data.SocialProof(shorterCycleUiModel, longerCycleUiModel)
            }

            shorterCycleUiModel is PlanUpgradeInstanceUiModel.Promotional ||
                longerCycleUiModel is PlanUpgradeInstanceUiModel.Promotional -> {
                PlanUpgradeInstanceListUiModel.Data.IntroPrice(shorterCycleUiModel, longerCycleUiModel)
            }

            else -> PlanUpgradeInstanceListUiModel.Data.Standard(
                shorterCycleUiModel as PlanUpgradeInstanceUiModel.Standard,
                longerCycleUiModel as PlanUpgradeInstanceUiModel.Standard
            )
        }
    }

    private fun resolveVariant(
        monthlyInstance: ProductDetail?,
        yearlyInstance: ProductDetail?,
        entryPoint: UpsellingEntryPoint
    ): PlanUpgradeVariant {
        val isPromotional = listOfNotNull(monthlyInstance, yearlyInstance).any { instance ->
            val currentPrice = instance.price.amount
            val defaultPrice = instance.renew.amount
            val isPromotional = currentPrice < defaultPrice
            isPromotional
        }

        val supportsHeaderVariants = entryPoint.supportsHeaderVariants()
        return when {
            isPromotional -> PlanUpgradeVariant.IntroductoryPrice
            supportsHeaderVariants -> PlanUpgradeVariant.SocialProof
            else -> PlanUpgradeVariant.Normal
        }
    }

    private fun UpsellingEntryPoint.supportsHeaderVariants() = when (this) {
        UpsellingEntryPoint.Feature.Sidebar,
        UpsellingEntryPoint.Feature.Navbar -> false // Keep social proof off for the time being

        UpsellingEntryPoint.Feature.AutoDelete,
        UpsellingEntryPoint.Feature.ContactGroups,
        UpsellingEntryPoint.Feature.Folders,
        UpsellingEntryPoint.Feature.Labels,
        UpsellingEntryPoint.Feature.MobileSignature,
        UpsellingEntryPoint.Feature.ScheduleSend,
        UpsellingEntryPoint.Feature.Snooze -> false
    }
}

internal sealed interface PlanMappingError {
    data object EmptyList : PlanMappingError
    data object InvalidList : PlanMappingError
}
