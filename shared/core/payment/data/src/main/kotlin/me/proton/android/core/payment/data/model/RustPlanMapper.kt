/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("MagicNumber")

package me.proton.android.core.payment.data.model

import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.model.ProductEntitlement
import me.proton.android.core.payment.domain.model.ProductHeader
import me.proton.android.core.payment.domain.model.ProductPrice
import me.proton.android.core.payment.domain.model.SubscriptionDetail
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import uniffi.proton_mail_uniffi.Plan
import uniffi.proton_mail_uniffi.PlanDecoration
import uniffi.proton_mail_uniffi.PlanEntitlement
import uniffi.proton_mail_uniffi.PlanInstance
import uniffi.proton_mail_uniffi.PlanVendorName
import uniffi.proton_mail_uniffi.Subscription

private fun getFormattedPrice(amount: ULong?, currency: String?) = when {
    amount == null -> ""
    currency == null -> ""
    else -> amount.toDouble().formatCentsPriceDefaultLocale(currency)
}

fun Subscription.toSubscriptionDetail() = SubscriptionDetail(
    name = name ?: "free",
    header = ProductHeader(
        title = title,
        description = description,
        priceText = getFormattedPrice(amount, currency),
        cycleText = cycleDescription ?: "",
        starred = false
    ),
    entitlements = entitlements.map { it.toProductEntitlement() },
    price = amount?.let {
        ProductPrice(
            productId = "unknown",
            customerId = "unknown",
            cycle = requireNotNull(cycle).toInt(),
            amount = requireNotNull(amount).toInt(),
            currency = requireNotNull(currency),
            formatted = getFormattedPrice(amount, currency)
        )
    },
    renew = renewAmount?.let {
        ProductPrice(
            productId = "unknown",
            customerId = "unknown",
            cycle = requireNotNull(cycle).toInt(),
            amount = requireNotNull(renewAmount).toInt(),
            currency = requireNotNull(currency),
            formatted = getFormattedPrice(renewAmount, currency)
        )
    },
    periodEnd = periodEnd?.toLong(),
    managedBy = external?.toInt()
)

fun Plan.toProductDetail(instance: PlanInstance) = ProductDetail(
    productId = requireNotNull(instance.vendors[PlanVendorName.GOOGLE]?.productId),
    planName = requireNotNull(name),
    header = toProductHeader(instance),
    price = instance.toProductPrice(),
    renew = instance.toProductPrice(),
    entitlements = entitlements.map { it.toProductEntitlement() }
)

fun Plan.toProductHeader(instance: PlanInstance) = ProductHeader(
    title = title,
    description = description,
    priceText = instance.price.first().let { getFormattedPrice(it.current, it.currency) },
    cycleText = instance.description,
    starred = decorations.any { it as? PlanDecoration.Starred != null }
)

fun PlanInstance.toProductPrice() = ProductPrice(
    productId = requireNotNull(vendors[PlanVendorName.GOOGLE]?.productId),
    customerId = requireNotNull(vendors[PlanVendorName.GOOGLE]?.customerId),
    cycle = cycle.toInt(),
    amount = price.first().current.toInt(),
    currency = price.first().currency,
    formatted = price.first().let { getFormattedPrice(it.current, it.currency) }
)

fun PlanEntitlement.toProductEntitlement() = when (this) {
    is PlanEntitlement.Description -> toProductEntitlement()
    is PlanEntitlement.Progress -> toProductEntitlement()
}

fun PlanEntitlement.Description.toProductEntitlement() = ProductEntitlement.Description(
    iconName = iconName,
    text = text,
    hint = hint
)

fun PlanEntitlement.Progress.toProductEntitlement() = ProductEntitlement.Progress(
    startText = title ?: "",
    iconName = iconName,
    endText = text,
    min = min.toInt(),
    max = max.toInt(),
    current = current.toInt()
)
