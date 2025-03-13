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

package me.proton.android.core.payment.presentation.component

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.android.core.payment.domain.PaymentException
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.usecase.GetAvailableUpgrades
import me.proton.android.core.payment.presentation.R
import me.proton.android.core.payment.presentation.model.Product
import me.proton.core.compose.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAvailableUpgrades: GetAvailableUpgrades
) : BaseViewModel<ProductListAction, ProductListState>(
    initialAction = ProductListAction.Load(),
    initialState = ProductListState.Loading
) {

    private val res = context.resources

    override fun onAction(action: ProductListAction): Flow<ProductListState> = flow {
        when (action) {
            is ProductListAction.Load -> emitAll(onLoad())
        }
    }

    private fun onLoad() = flow {
        emit(ProductListState.Loading)
        val details = getAvailableUpgrades()
        val list = details.map {
            Product(
                planName = it.planName,
                productId = it.productId,
                accountId = requireNotNull(it.price.customerId),
                cycle = it.price.cycle,
                header = it.header,
                entitlements = it.entitlements,
                renewalText = it.getRenewalText()
            )
        }
        emit(ProductListState.Data(list))
    }.catch {
        when ((it as? PaymentException)?.errorCode) {
            PaymentException.Companion.ErrorCode.BILLING_UNAVAILABLE -> {
                emit(ProductListState.Data(emptyList()))
            }
            else -> {
                emit(ProductListState.Error(it.message ?: "Error in onLoad"))
            }
        }
    }

    override suspend fun FlowCollector<ProductListState>.onError(throwable: Throwable) {
        emit(ProductListState.Error(throwable.message ?: "Unknown error"))
    }

    @Suppress("MagicNumber")
    private fun ProductDetail.getRenewalText(): String? = when {
        price.amount == renew.amount -> null
        else -> when (price.cycle) {
            1 -> res.getString(R.string.payment_welcome_offer_renew_monthly, renew.formatted)
            12 -> res.getString(R.string.payment_welcome_offer_renew_annually, renew.formatted)
            24 -> res.getString(R.string.payment_welcome_offer_renew_biennially, renew.formatted)
            else -> res.getQuantityString(R.plurals.payment_welcome_offer_renew_other, price.cycle, renew.formatted)
        }
    }
}
