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
import me.proton.android.core.payment.domain.model.SubscriptionDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCurrentSubscriptions @Inject constructor(
    private val paymentManager: PaymentManager,
    private val subscriptionManager: SubscriptionManager
) {

    suspend operator fun invoke(): List<SubscriptionDetail> {
        val subscriptions = subscriptionManager.getCurrent()
        val ids = subscriptions.mapNotNull { null } // Use it.productId. Currently not existing.
        if (ids.isEmpty()) return subscriptions

        val storeProducts = paymentManager.getStoreProducts(ids).associateBy { it.productId }
        return subscriptions.map {
            val store = storeProducts[it.name]
            it.copy(
                header = it.header.copy(
                    priceText = store?.price?.formatted ?: store?.header?.priceText ?: it.header.priceText,
                    cycleText = store?.header?.cycleText ?: it.header.cycleText
                )
            )
        }
    }
}
