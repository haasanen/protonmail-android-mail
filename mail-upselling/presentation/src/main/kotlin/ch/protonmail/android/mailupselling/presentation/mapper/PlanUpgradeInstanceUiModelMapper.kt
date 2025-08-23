/*
 * Copyright (c) 2025 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later vers ion.
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

import android.content.Context
import ch.protonmail.android.mailupselling.domain.extensions.normalizedPrice
import ch.protonmail.android.mailupselling.domain.extensions.normalizedPriceWithCurrency
import ch.protonmail.android.mailupselling.domain.extensions.totalPriceWithCurrency
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.domain.usecase.GetDiscountRate
import ch.protonmail.android.mailupselling.domain.usecase.GetYearlySaving
import ch.protonmail.android.mailupselling.presentation.extension.toUiModel
import ch.protonmail.android.mailupselling.presentation.mapper.RenewalCycle.BiYearly
import ch.protonmail.android.mailupselling.presentation.mapper.RenewalCycle.Monthly
import ch.protonmail.android.mailupselling.presentation.mapper.RenewalCycle.Yearly
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.presentation.R
import me.proton.android.core.payment.presentation.model.Product
import javax.inject.Inject

class PlanUpgradeInstanceUiModelMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getDiscountRate: GetDiscountRate,
    private val getYearlySaving: GetYearlySaving
) {

    fun toUiModel(
        monthlyPlanInstance: ProductDetail,
        yearlyPlanInstance: ProductDetail
    ): Pair<PlanUpgradeInstanceUiModel, PlanUpgradeInstanceUiModel> {

        val monthlyUiModel = createPlanUiModel(
            cycle = PlanUpgradeCycle.Monthly,
            productDetail = monthlyPlanInstance
        )

        val yearlyUiModel = createPlanUiModel(
            cycle = PlanUpgradeCycle.Yearly,
            productDetail = yearlyPlanInstance,
            comparisonPriceInstance = monthlyPlanInstance
        )

        return Pair(monthlyUiModel, yearlyUiModel)
    }

    private fun createPlanUiModel(
        productDetail: ProductDetail,
        cycle: PlanUpgradeCycle,
        comparisonPriceInstance: ProductDetail? = null
    ): PlanUpgradeInstanceUiModel {
        val currentPrice = productDetail.price
        val defaultPrice = productDetail.renew

        val isPromotional = currentPrice.amount < defaultPrice.amount
        val currency = productDetail.price.currency

        return if (isPromotional) {
            val promotionalPrice = currentPrice.normalizedPrice(cycle.months)
            val renewalPrice = defaultPrice.normalizedPrice(cycle.months)
            PlanUpgradeInstanceUiModel.Promotional(
                name = productDetail.header.title,
                pricePerCycle = productDetail.price.normalizedPriceWithCurrency(currency, cycle.months).toUiModel(),
                promotionalPrice = productDetail.price.totalPriceWithCurrency(currency).toUiModel(),
                renewalPrice = productDetail.renew.totalPriceWithCurrency(currency).toUiModel(),
                yearlySaving = comparisonPriceInstance?.let { getYearlySaving(it, productDetail) },
                discountRate = getDiscountRate(promotionalPrice, renewalPrice),
                cycle = cycle,
                product = productDetail.toProduct(context)
            )
        } else {
            PlanUpgradeInstanceUiModel.Standard(
                name = productDetail.header.title,
                pricePerCycle = productDetail.price.normalizedPriceWithCurrency(currency, cycle.months).toUiModel(),
                totalPrice = productDetail.price.totalPriceWithCurrency(currency).toUiModel(),
                yearlySaving = comparisonPriceInstance?.let { getYearlySaving(it, productDetail) },
                discountRate = comparisonPriceInstance?.let { getDiscountRate(it, productDetail) },
                cycle = cycle,
                product = productDetail.toProduct(context)
            )
        }
    }
}

internal fun ProductDetail.toProduct(context: Context): Product {
    return Product(
        planName = planName,
        productId = productId,
        accountId = requireNotNull(price.customerId),
        cycle = price.cycle,
        header = header,
        entitlements = entitlements,
        renewalText = getRenewalText(context)
    )
}

private fun ProductDetail.getRenewalText(context: Context): String? {
    val res = context.resources

    return when {
        price.amount == renew.amount -> null
        else -> when (price.cycle) {
            Monthly -> res.getString(R.string.payment_welcome_offer_renew_monthly, renew.formatted)
            Yearly -> res.getString(R.string.payment_welcome_offer_renew_annually, renew.formatted)
            BiYearly -> res.getString(R.string.payment_welcome_offer_renew_biennially, renew.formatted)
            else -> res.getQuantityString(R.plurals.payment_welcome_offer_renew_other, price.cycle, renew.formatted)
        }
    }
}

private object RenewalCycle {

    const val Monthly = 1
    const val Yearly = 12
    const val BiYearly = 24
}
