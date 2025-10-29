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
import ch.protonmail.android.mailupselling.domain.model.BlackFridayPhase
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeSupportedTags
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.domain.usecase.GetCurrentBlackFridayPhase
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeVariant
import me.proton.android.core.payment.domain.model.ProductOfferDetail
import javax.inject.Inject

internal class PlanUpgradeMapper @Inject constructor(
    private val getCurrentBlackFridayPhase: GetCurrentBlackFridayPhase
) {

    suspend fun resolveVariant(
        monthlyInstance: ProductOfferDetail?,
        yearlyInstance: ProductOfferDetail?,
        entryPoint: UpsellingEntryPoint
    ): Either<PlanUpgradeMappingError, PlanUpgradeVariant> = either {
        val instances = listOfNotNull(monthlyInstance, yearlyInstance)

        when {
            instances.containsTag(PlanUpgradeSupportedTags.BlackFriday) -> when (getCurrentBlackFridayPhase()) {
                BlackFridayPhase.None -> raise(PlanUpgradeMappingError.InvalidBlackFridayState)
                BlackFridayPhase.Active.Wave2 -> PlanUpgradeVariant.BlackFriday.Wave2
                BlackFridayPhase.Active.Wave1 -> PlanUpgradeVariant.BlackFriday.Wave1
            }

            instances.containsTag(PlanUpgradeSupportedTags.IntroductoryPrice) -> PlanUpgradeVariant.IntroductoryPrice
            entryPoint.supportsHeaderVariants() -> PlanUpgradeVariant.SocialProof
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

            variant is PlanUpgradeVariant.BlackFriday -> {
                PlanUpgradeInstanceListUiModel.Data.BlackFriday(variant, shorterCycleUiModel, longerCycleUiModel)
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

    private fun List<ProductOfferDetail>.containsTag(tag: PlanUpgradeSupportedTags) =
        any { it.offer.tags.value.contains(tag.value) }

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

sealed interface PlanUpgradeMappingError {
    data object InvalidBlackFridayState : PlanUpgradeMappingError
}
