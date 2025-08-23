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

package ch.protonmail.android.mailupselling.domain.extensions

import ch.protonmail.android.mailupselling.domain.model.ProductPriceWithCurrency
import me.proton.android.core.payment.domain.model.ProductPrice

fun ProductPrice.normalizedPrice(cycle: Int): Float = this.amount.normalized(cycle)
fun ProductPrice.totalPrice(): Float = this.amount.toActualPrice()

fun ProductPrice.normalizedPriceWithCurrency(currency: String, cycle: Int): ProductPriceWithCurrency {
    val actualPrice = this.amount.normalized(cycle)
    return ProductPriceWithCurrency(actualPrice, currency)
}

fun ProductPrice.totalPriceWithCurrency(currency: String): ProductPriceWithCurrency {
    val actualPrice = this.amount.toActualPrice()
    return ProductPriceWithCurrency(actualPrice, currency)
}

@Suppress("MagicNumber")
internal fun Int.toActualPrice() = (this / (1000 * 1000f)).takeIf {
    it != Float.POSITIVE_INFINITY && it != Float.NEGATIVE_INFINITY
} ?: 0f

@Suppress("MagicNumber")
internal fun Int.normalized(cycle: Int) = (this / (1000 * 1000f) / cycle).takeIf {
    it != Float.POSITIVE_INFINITY && it != Float.NEGATIVE_INFINITY
} ?: 0f
