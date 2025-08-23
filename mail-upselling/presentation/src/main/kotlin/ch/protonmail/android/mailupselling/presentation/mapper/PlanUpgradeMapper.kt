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

import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeVariant
import me.proton.android.core.payment.domain.model.ProductDetail

internal object PlanUpgradeMapper {
    fun resolveVariant(
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

    fun resolveListUiModel(
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

    private fun UpsellingEntryPoint.supportsHeaderVariants() = when (this) {
        UpsellingEntryPoint.PostOnboarding,
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
