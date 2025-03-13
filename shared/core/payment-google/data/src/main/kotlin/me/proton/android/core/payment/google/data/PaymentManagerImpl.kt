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

package me.proton.android.core.payment.google.data

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import me.proton.android.core.payment.domain.PaymentManager
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.model.Purchase
import me.proton.android.core.payment.google.data.extension.getOrThrow
import me.proton.android.core.payment.google.data.extension.getProductDetails
import me.proton.android.core.payment.google.data.extension.getProductPurchasesAsync
import me.proton.android.core.payment.google.data.extension.withConnection
import me.proton.android.core.payment.google.data.model.toProductHeader
import me.proton.android.core.payment.google.data.model.toProductPrice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentManagerImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val purchaseStoreListener: PurchaseStoreListener
) : PaymentManager {

    // Return a new instance (multiple simultaneous instances are supported).
    private fun newClient() = BillingClient
        .newBuilder(context)
        .setListener(purchaseStoreListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    override suspend fun getStoreProducts(ids: List<String>): List<ProductDetail> = newClient().withConnection {
        getProductDetails(ids).getOrThrow()?.map { detail ->
            val phases = detail.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList ?: emptyList()
            val current = phases.getOrNull(0)
            val renew = phases.getOrNull(1)
            ProductDetail(
                productId = detail.productId,
                planName = detail.name, // Comes from Proton BE.
                header = detail.toProductHeader(requireNotNull(current)),
                price = current.toProductPrice(detail.productId),
                renew = renew?.toProductPrice(detail.productId) ?: current.toProductPrice(detail.productId),
                entitlements = emptyList()
            )
        }.orEmpty()
    }

    override suspend fun getStorePurchases(): List<Purchase> = newClient().withConnection {
        purchaseStoreListener.purchases
            .onSubscription { getProductPurchasesAsync(purchaseStoreListener) }
            .first()
    }

    override fun observeStorePurchases(): Flow<List<Purchase>> = callbackFlow {
        val job = launch { purchaseStoreListener.purchases.collect { send(it) } }
        newClient().withConnection {
            getProductPurchasesAsync(purchaseStoreListener)
            job.join()
        }
        awaitClose { job.cancel() }
    }
}
