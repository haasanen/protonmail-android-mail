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

data class Purchase(
    val productType: String,
    val productId: String,
    val orderId: String,
    val clientId: String, // packageName
    val customerId: String,
    val receipt: String,
    val autoRenewing: Boolean,
    val status: PurchaseStatus
)

enum class PurchaseStatus(val value: Int) {
    Unspecified(0),
    Purchased(1),
    Pending(2),
    Acknowledged(3);

    companion object {
        val map = entries.associateBy { it.value }
    }
}
