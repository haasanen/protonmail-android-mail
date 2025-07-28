/*
 * Copyright (c) 2022 Proton Technologies AG
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

package ch.protonmail.android.mailupselling.presentation.model.planupgrades

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import me.proton.android.core.payment.presentation.model.Product

sealed class PlanUpgradeInstanceUiModel(
    open val name: String,
    open val currency: String,
    open val discountRate: Int?,
    open val cycle: PlanUpgradeCycle,
    open val product: Product
) {

    abstract val primaryPrice: PlanUpgradePriceDisplayUiModel

    data class Standard(
        override val name: String,
        private val pricePerCycle: TextUiModel,
        private val totalPrice: TextUiModel,
        override val discountRate: Int?,
        override val currency: String,
        override val cycle: PlanUpgradeCycle,
        override val product: Product
    ) : PlanUpgradeInstanceUiModel(
        name,
        currency,
        discountRate,
        cycle,
        product
    ) {

        override val primaryPrice: PlanUpgradePriceDisplayUiModel
            get() {
                val (displayedPrice, standardPrice) = when (cycle) {
                    PlanUpgradeCycle.Monthly -> Pair(pricePerCycle, null)
                    PlanUpgradeCycle.Yearly -> Pair(totalPrice, pricePerCycle)
                }

                return PlanUpgradePriceDisplayUiModel(
                    pricePerCycle = pricePerCycle,
                    highlightedPrice = displayedPrice,
                    secondaryPrice = standardPrice
                )
            }
    }

    data class Promotional(
        override val name: String,
        private val pricePerCycle: TextUiModel,
        private val promotionalPrice: TextUiModel,
        private val renewalPrice: TextUiModel,
        override val discountRate: Int?,
        override val currency: String,
        override val cycle: PlanUpgradeCycle,
        override val product: Product
    ) : PlanUpgradeInstanceUiModel(
        name,
        currency,
        discountRate,
        cycle,
        product
    ) {

        override val primaryPrice: PlanUpgradePriceDisplayUiModel
            get() = PlanUpgradePriceDisplayUiModel(
                pricePerCycle = pricePerCycle,
                highlightedPrice = promotionalPrice,
                secondaryPrice = renewalPrice
            )
    }
}
