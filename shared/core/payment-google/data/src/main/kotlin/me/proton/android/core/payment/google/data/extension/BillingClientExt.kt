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

package me.proton.android.core.payment.google.data.extension

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.android.core.payment.domain.LogTag
import me.proton.android.core.payment.domain.PaymentException
import me.proton.android.core.payment.domain.PaymentException.Companion.ErrorCode.SERVICE_UNAVAILABLE
import me.proton.android.core.payment.domain.model.PaymentObservabilityValue
import me.proton.android.core.payment.google.data.PurchaseStoreListener
import me.proton.core.util.kotlin.CoreLogger

/**
 * Calls the specified suspending [block] receiving a connected or closed [BillingClient], suspending while connection
 * is establishing, and return the result [R].
 *
 * Note: This implementation take care of calling [BillingClient.startConnection] and [BillingClient.endConnection].
 *
 * Example of usage:
 *
 * ```
 * newBillingClient().withConnection { connectedClient ->
 *     connectedClient.queryPurchasesAsync(...)
 * }
 * ```
 * @throws PaymentException on any billing setup error.
 * @see [BillingClient]
 */
suspend fun <R> BillingClient.withConnection(retry: Int = Int.MAX_VALUE, block: suspend BillingClient.() -> R): R {
    var remainingRetry = retry
    val mutableConnectionState = MutableStateFlow<Int?>(null)
    val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            CoreLogger.d(LogTag.STORE, "BillingClient: onBillingServiceDisconnected: remainingRetry: $remainingRetry")
            if (remainingRetry > 0) {
                remainingRetry -= 1
                startConnection(this)
                mutableConnectionState.tryEmit(ConnectionState.CONNECTING)
            } else {
                endConnection()
                mutableConnectionState.tryEmit(ConnectionState.CLOSED)
            }
        }

        override fun onBillingSetupFinished(result: BillingResult) {
            CoreLogger.d(LogTag.STORE, "BillingClient: onBillingSetupFinished: $result")
            if (result.responseCode == BillingResponseCode.OK) {
                mutableConnectionState.tryEmit(ConnectionState.CONNECTED)
            } else {
                mutableConnectionState.tryEmit(ConnectionState.CLOSED)
            }
        }
    }

    try {
        CoreLogger.d(LogTag.STORE, "BillingClient: startConnection...")
        startConnection(billingClientStateListener)
        val state = mutableConnectionState.firstOrNull {
            when (it) {
                ConnectionState.DISCONNECTED -> false
                ConnectionState.CONNECTING -> false
                ConnectionState.CONNECTED -> true
                ConnectionState.CLOSED -> true
                else -> false
            }
        }

        if (state != ConnectionState.CONNECTED) {
            throw PaymentException(SERVICE_UNAVAILABLE, "Failed to connect to billing service")
        }

        CoreLogger.d(LogTag.STORE, "BillingClient: invoking block...")
        return withContext(Dispatchers.IO) { block.invoke(this@withConnection) }
    } finally {
        endConnection()
        CoreLogger.d(LogTag.STORE, "BillingClient: endConnection.")
    }
}

suspend fun BillingClient.getProductDetails(ids: List<String>): ProductDetailsResult {
    val productList = ids.map { id ->
        Product.newBuilder().setProductId(id).setProductType(BillingClient.ProductType.SUBS).build()
    }
    val params = QueryProductDetailsParams.newBuilder()
    params.setProductList(productList)
    return queryProductDetails(params.build())
}

fun BillingClient.getProductPurchasesAsync(listener: PurchaseStoreListener) {
    val params = QueryPurchasesParams.newBuilder()
    params.setProductType(BillingClient.ProductType.SUBS)
    return queryPurchasesAsync(params.build(), listener)
}

fun BillingResult.checkOrThrow() = getExceptionOrNull()?.let { throw it }

fun BillingResult.getExceptionOrNull() = when {
    responseCode == BillingResponseCode.OK -> null
    else -> PaymentException(responseCode, debugMessage)
}

/**
 * Provides a primary callback passing back [BillingResult] as a [PaymentObservabilityValue].
 *
 * Optionally provides an error callback, notifying consumers specifically if the result is not successful.
 */
fun BillingResult.metrics(onResponse: (PaymentObservabilityValue) -> Unit, onError: ((String) -> Unit)? = null) {
    val observabilityResponse = this.toObservabilityResponse()
    onResponse(observabilityResponse)

    if (observabilityResponse != PaymentObservabilityValue.SUCCESS) {
        onError?.invoke(debugMessage)
    }
}

/**
 * Maps a [BillingResult.responseCode] to a [PaymentObservabilityValue].
 */
private fun BillingResult.toObservabilityResponse(): PaymentObservabilityValue {
    return when (responseCode) {
        BillingResponseCode.OK -> PaymentObservabilityValue.SUCCESS
        BillingResponseCode.USER_CANCELED,
        BillingResponseCode.DEVELOPER_ERROR,
        BillingResponseCode.BILLING_UNAVAILABLE,
        BillingResponseCode.ITEM_NOT_OWNED,
        BillingResponseCode.ITEM_UNAVAILABLE,
        BillingResponseCode.ITEM_ALREADY_OWNED -> PaymentObservabilityValue.HTTP4XX
        BillingResponseCode.FEATURE_NOT_SUPPORTED,
        BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingResponseCode.SERVICE_DISCONNECTED -> PaymentObservabilityValue.HTTP5XX

        else -> PaymentObservabilityValue.UNKNOWN
    }
}
