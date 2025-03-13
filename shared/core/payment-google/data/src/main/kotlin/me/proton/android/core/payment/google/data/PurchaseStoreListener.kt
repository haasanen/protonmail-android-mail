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

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import me.proton.android.core.payment.domain.LogTag
import me.proton.android.core.payment.domain.PurchaseListener
import me.proton.android.core.payment.domain.model.Purchase
import me.proton.android.core.payment.google.data.model.toPurchase
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton
import com.android.billingclient.api.Purchase as BillingClientPurchase

@Singleton
class PurchaseStoreListener @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider
) :
    PurchaseListener,
    PurchasesUpdatedListener,
    PurchasesResponseListener {

    private val mutablePurchases = MutableSharedFlow<List<Purchase>>()

    val purchases: SharedFlow<List<Purchase>> = mutablePurchases.asSharedFlow()

    override fun onPurchasesUpdated(result: BillingResult, list: List<BillingClientPurchase>?) {
        CoreLogger.d(LogTag.DEFAULT, "onPurchasesUpdated -> $list")
        list?.let { onPurchaseListUpdated(list.map { it.toPurchase() }) }
    }

    override fun onQueryPurchasesResponse(result: BillingResult, list: List<BillingClientPurchase>) {
        CoreLogger.d(LogTag.DEFAULT, "onQueryPurchasesResponse -> $list")
        onPurchaseListUpdated(list.map { it.toPurchase() })
    }

    override fun onPurchaseListUpdated(list: List<Purchase>) {
        CoreLogger.d(LogTag.DEFAULT, "onPurchaseListUpdated -> $list")
        scopeProvider.GlobalIOSupervisedScope.launch {
            mutablePurchases.emit(list)
        }
    }
}
