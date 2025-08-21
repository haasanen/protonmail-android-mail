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
import me.proton.android.core.payment.domain.LogTag
import me.proton.android.core.payment.domain.PaymentException
import me.proton.android.core.payment.domain.PaymentErrorCode
import me.proton.android.core.payment.domain.model.SubscriptionDetail
import me.proton.android.core.payment.domain.usecase.GetCurrentSubscriptions
import me.proton.android.core.payment.presentation.R
import me.proton.android.core.payment.presentation.model.Subscription
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.util.android.datetime.DateTimeFormat
import me.proton.core.util.android.datetime.DateTimeFormat.DateTimeForm.MEDIUM_DATE
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class SubscriptionListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCurrentSubscriptions: GetCurrentSubscriptions,
    private val dateTimeFormat: DateTimeFormat
) : BaseViewModel<SubscriptionListAction, SubscriptionListState>(
    initialAction = SubscriptionListAction.Load(),
    initialState = SubscriptionListState.Loading
) {

    private val res = context.resources

    override fun onAction(action: SubscriptionListAction): Flow<SubscriptionListState> = flow {
        when (action) {
            is SubscriptionListAction.Load -> emitAll(onLoad())
        }
    }

    private fun onLoad() = flow {
        emit(SubscriptionListState.Loading)
        val subscriptions = getCurrentSubscriptions()
        val list = subscriptions.map {
            Subscription(
                header = it.header,
                entitlements = it.entitlements,
                additionalText = listOfNotNull(it.getRenewalText(), it.getManagedByText())
            )
        }
        emit(SubscriptionListState.Data(list))
    }.catch { exception ->
        if ((exception as? PaymentException)?.errorCode == PaymentErrorCode.FORBIDDEN.value) {
            emit(SubscriptionListState.Failure.Forbidden)
        } else {
            val exceptionMessage = exception.message ?: "Error occurred whilst fetching current subscriptions."
            CoreLogger.e(LogTag.GET_SUBSCRIPTIONS, exceptionMessage)
            emit(SubscriptionListState.Failure.Error(exceptionMessage))
        }
    }

    override suspend fun FlowCollector<SubscriptionListState>.onError(throwable: Throwable) {
        emit(SubscriptionListState.Failure.Error(throwable.message ?: "Unknown error"))
    }

    private fun SubscriptionDetail.getRenewalText() = when {
        renew == null -> null
        periodEnd == null -> null
        else -> {
            val formattedDate = dateTimeFormat.format(periodEnd!!.toLong(), MEDIUM_DATE)
            res.getString(R.string.payment_subscription_renew_auto_renew_on, formattedDate)
        }
    }

    private fun SubscriptionDetail.getManagedByText() = when {
        name == "free" -> null
        managedBy == 1 -> res.getString(R.string.payment_subscription_managed_by_apple)
        managedBy == 2 -> res.getString(R.string.payment_subscription_managed_by_google)
        else -> res.getString(R.string.payment_subscription_managed_by_other)
    }
}
