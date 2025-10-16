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

package me.proton.android.core.payment.domain.usecase

import me.proton.android.core.payment.domain.PaymentManager
import me.proton.android.core.payment.domain.SubscriptionManager
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.model.PurchaseStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAvailableUpgrades @Inject constructor(
    private val paymentManager: PaymentManager,
    private val subscriptionManager: SubscriptionManager
) {

    suspend operator fun invoke(): List<ProductDetail> {
        if (!subscriptionManager.areInAppPurchasesEnabled()) {
            return emptyList()
        }

        val upgrades = subscriptionManager.getAvailable()
        if (upgrades.isEmpty()) return upgrades

        val ids = upgrades.map { it.productId }
        val storePurchases = paymentManager.getStorePurchases().associateBy { it.productId }
        val storeProducts = paymentManager.getStoreProducts(ids).associateBy { it.productId }
        val acknowledged = storePurchases.filterValues { it.status == PurchaseStatus.Acknowledged }
        val available = upgrades
            // Remove any acknowledged Purchase.
            .filter { !acknowledged.containsKey(it.productId) }
            // Remove any unavailable product in Store.
            .filter { storeProducts.containsKey(it.productId) }

        return available.map {
            val store = storeProducts.getValue(it.productId)
            it.copy(
                header = it.header.copy(
                    priceText = store.header.priceText
                ),
                price = it.price.copy(
                    amount = store.price.amount,
                    currency = store.price.currency,
                    formatted = store.price.formatted
                ),
                renew = it.price.copy(
                    amount = store.renew.amount,
                    currency = store.renew.currency,
                    formatted = store.renew.formatted
                )
            )
        }
    }
}
