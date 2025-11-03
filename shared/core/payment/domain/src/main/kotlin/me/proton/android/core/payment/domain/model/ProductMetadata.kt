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

package me.proton.android.core.payment.domain.model

data class ProductMetadata(
    val productId: String,
    val customerId: String,
    val planName: String,
    val entitlements: List<ProductEntitlement>
)

data class PlayStoreProductMetadata(
    val productId: String,
    val planName: String
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
