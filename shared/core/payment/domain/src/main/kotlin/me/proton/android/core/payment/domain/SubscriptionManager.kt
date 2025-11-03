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

package me.proton.android.core.payment.domain

import me.proton.android.core.payment.domain.model.ProductOfferDetail
import me.proton.android.core.payment.domain.model.Purchase
import me.proton.android.core.payment.domain.model.SubscriptionDetail

/**
 * Get [ProductOfferDetail] from Proton Payment BE.
 */
interface SubscriptionManager {

    /**
     * Get all current [SubscriptionDetail] from BE.
     */
    suspend fun getCurrent(): List<SubscriptionDetail>


    /**
     * Get all available [ProductOfferDetail] from BE.
     */
    suspend fun getAvailable(): List<ProductOfferDetail>

    /**
     * Subscribe to [product] using [purchase] on BE.
     *
     * Note: Create Proton Token and subscribe.
     *
     * @throws PaymentException
     */
    suspend fun subscribe(product: ProductOfferDetail, purchase: Purchase)

    /**
     * Check if In App Purchases are enabled for this user session.
     */
    suspend fun areInAppPurchasesEnabled(): Boolean
}
