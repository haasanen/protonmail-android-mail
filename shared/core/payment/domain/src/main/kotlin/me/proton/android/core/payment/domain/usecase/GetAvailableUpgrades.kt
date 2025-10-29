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
import me.proton.android.core.payment.domain.model.ProductOffer
import me.proton.android.core.payment.domain.model.ProductOfferList
import me.proton.android.core.payment.domain.model.ProductSelectionHeader
import me.proton.android.core.payment.domain.model.PurchaseStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAvailableUpgrades @Inject constructor(
    private val paymentManager: PaymentManager,
    private val subscriptionManager: SubscriptionManager
) {

    suspend operator fun invoke(): List<ProductOfferList> {
        if (!subscriptionManager.areInAppPurchasesEnabled()) {
            return emptyList()
        }

        val upgrades = subscriptionManager.getAvailable()
        if (upgrades.isEmpty()) return emptyList()

        val ids = upgrades.map { it.metadata.productId }
        val storePurchases = paymentManager.getStorePurchases().associateBy { it.productId }
        val storeProducts = paymentManager.getStoreProducts(ids).associateBy { it.metadata.productId }
        val acknowledged = storePurchases.filterValues { it.status == PurchaseStatus.Acknowledged }
        val available = upgrades
            // Remove any acknowledged Purchase.
            .filter { !acknowledged.containsKey(it.metadata.productId) }
            // Remove any unavailable product in Store.
            .filter { storeProducts.containsKey(it.metadata.productId) }

        return available.map {
            val store = storeProducts.getValue(it.metadata.productId)
            ProductOfferList(
                metadata = it.metadata,
                header = ProductSelectionHeader(
                    it.header.title,
                    it.header.description,
                    it.header.cycleText,
                    it.header.starred
                ),
                offers = store.offers.injectCustomerId(it.metadata.customerId)
            )
        }
    }

    private fun List<ProductOffer>.injectCustomerId(customerId: String) = map { offer ->
        offer.copy(
            current = offer.current.copy(customerId = customerId),
            renew = offer.renew.copy(customerId = customerId)
        )
    }
}
