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

package me.proton.android.core.payment.domain.model

data class ProductDetail(
    val productId: String,
    val planName: String,
    val header: ProductHeader,
    val price: ProductPrice,
    val renew: ProductPrice,
    val entitlements: List<ProductEntitlement>
)

data class ProductHeader(
    val title: String,
    val description: String,
    val priceText: String,
    val cycleText: String,
    val starred: Boolean
)

data class ProductPrice(
    val productId: String,
    val customerId: String?,
    val cycle: Int,
    val amount: Long,
    val currency: String,
    val formatted: String?
)

sealed interface ProductEntitlement {

    data class Description(
        val iconName: String?,
        val text: String,
        val hint: String?
    ) : ProductEntitlement

    data class Progress(
        val startText: String,
        val iconName: String?,
        val endText: String,
        val min: Long,
        val max: Long,
        val current: Long
    ) : ProductEntitlement {

        /** Returns a value between 0.0 and 1.0. */
        val normalizedProgress: Double = (current.toDouble() - min) / (max.toDouble() - min)
    }
}
