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

package me.proton.android.core.payment.google.presentation

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import me.proton.android.core.payment.domain.LogTag
import me.proton.android.core.payment.domain.PaymentMetricsTracker
import me.proton.android.core.payment.domain.model.PaymentObservabilityMetric.IAP_SUBSCRIBE
import me.proton.android.core.payment.domain.model.ProductOfferToken
import me.proton.android.core.payment.google.data.PurchaseStoreListener
import me.proton.android.core.payment.google.data.extension.metrics
import me.proton.android.core.payment.google.data.extension.withConnection
import me.proton.android.core.payment.presentation.PurchaseOrchestrator
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrchestratorImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val paymentMetricsTracker: PaymentMetricsTracker,
    private val purchaseStoreListener: PurchaseStoreListener,
    private val scopeProvider: CoroutineScopeProvider
) : PurchaseOrchestrator {

    // Return a new instance (multiple simultaneous instances are supported).
    private fun newClient() = BillingClient
        .newBuilder(context)
        .setListener(purchaseStoreListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private suspend fun getProductDetails(id: String): ProductDetailsResult {
        val productList = listOf(
            Product.newBuilder().setProductId(id).setProductType(BillingClient.ProductType.SUBS).build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        return newClient().withConnection {
            queryProductDetails(params.build())
        }
    }

    private suspend fun launchBillingFlow(
        caller: Activity,
        productId: String,
        offerToken: ProductOfferToken,
        accountId: String
    ): BillingResult {
        val productDetailResult = getProductDetails(productId)
        val productDetail = requireNotNull(productDetailResult.productDetailsList).first()

        val offer = productDetail.subscriptionOfferDetails?.first { it.offerToken == offerToken.value }
        requireNotNull(offer) { "Offer not found for product id '$productId'" }

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetail)
                        .setOfferToken(offer.offerToken)
                        .build()
                )
            )
            .setObfuscatedAccountId(accountId)
            .build()

        return newClient().withConnection {
            launchBillingFlow(caller, params)
        }
    }

    override fun startPurchaseWorkflow(
        caller: Activity,
        productId: String,
        offerToken: ProductOfferToken,
        accountId: String
    ) {
        scopeProvider.GlobalIOSupervisedScope.launch {
            val result = runCatching { launchBillingFlow(caller, productId, offerToken, accountId) }
            CoreLogger.d(LogTag.STORE, "launchBillingFlow result: $result")

            result.getOrNull()?.let { billingResult ->
                billingResult.metrics(
                    onResponse = { paymentMetricsTracker.track(IAP_SUBSCRIBE, it) },
                    onError = { CoreLogger.e(LogTag.IN_APP_PURCHASE, it) }
                )
            }
        }
    }
}
