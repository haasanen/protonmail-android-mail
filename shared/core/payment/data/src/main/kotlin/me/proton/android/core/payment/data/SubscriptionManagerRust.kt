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

package me.proton.android.core.payment.data

import android.content.Context
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.repository.getPrimarySession
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.android.core.payment.data.extension.getErrorMessage
import me.proton.android.core.payment.data.model.toProductDetail
import me.proton.android.core.payment.data.model.toSubscriptionDetail
import me.proton.android.core.payment.domain.PaymentException
import me.proton.android.core.payment.domain.PaymentException.Companion.ErrorCode.DEVELOPER_ERROR
import me.proton.android.core.payment.domain.SubscriptionManager
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.model.Purchase
import me.proton.android.core.payment.domain.model.SubscriptionDetail
import uniffi.proton_mail_uniffi.GetPaymentsPlansOptions
import uniffi.proton_mail_uniffi.GoogleRecurringReceiptDetails
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionGetPaymentsPlansResult
import uniffi.proton_mail_uniffi.MailUserSessionGetPaymentsSubscriptionResult
import uniffi.proton_mail_uniffi.MailUserSessionPostPaymentsSubscriptionResult
import uniffi.proton_mail_uniffi.MailUserSessionPostPaymentsTokensResult
import uniffi.proton_mail_uniffi.NewSubscription
import uniffi.proton_mail_uniffi.NewSubscriptionValues
import uniffi.proton_mail_uniffi.PaymentReceipt
import uniffi.proton_mail_uniffi.PaymentToken
import uniffi.proton_mail_uniffi.UserSessionError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("MagicNumber")
class SubscriptionManagerRust @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: UserSessionRepository
) : SubscriptionManager {

    private fun UserSessionError.throwException(): Nothing {
        throw PaymentException(DEVELOPER_ERROR, getErrorMessage(context))
    }

    private suspend fun getProtonToken(
        product: ProductDetail,
        purchase: Purchase,
        session: MailUserSession
    ): PaymentToken = when (
        val result = session.postPaymentsTokens(
            amount = product.price.amount.toULong(),
            currency = product.price.currency,
            payment = PaymentReceipt.Google(
                details = GoogleRecurringReceiptDetails(
                    orderId = purchase.orderId,
                    productId = purchase.productId,
                    customerId = purchase.customerId,
                    packageName = purchase.clientId,
                    token = purchase.receipt
                )
            )
        )
    ) {
        is MailUserSessionPostPaymentsTokensResult.Error -> result.v1.throwException()
        is MailUserSessionPostPaymentsTokensResult.Ok -> result.v1
    }

    private suspend fun createOrUpdateSubscription(
        session: MailUserSession,
        product: ProductDetail,
        paymentToken: PaymentToken
    ) = when (
        val result = session.postPaymentsSubscription(
            subscription = NewSubscription(
                cycle = product.price.cycle.toUByte(),
                currency = product.price.currency,
                currencyId = null,
                plans = mapOf(product.planName to 1),
                planIds = null,
                codes = null,
                couponCode = null,
                giftCode = null
            ),
            newValues = NewSubscriptionValues(
                amount = product.price.amount.toULong(),
                payments = null,
                paymentToken = paymentToken.token
            )
        )
    ) {
        is MailUserSessionPostPaymentsSubscriptionResult.Error -> result.v1.throwException()
        is MailUserSessionPostPaymentsSubscriptionResult.Ok -> Unit
    }

    override suspend fun getCurrent(): List<SubscriptionDetail> {
        val session = sessionRepository.getPrimarySession() ?: return emptyList()
        when (val result = session.getPaymentsSubscription()) {
            is MailUserSessionGetPaymentsSubscriptionResult.Error -> {
                result.v1.throwException()
            }

            is MailUserSessionGetPaymentsSubscriptionResult.Ok -> {
                val subscriptions = result.v1.current
                return subscriptions.map { it.toSubscriptionDetail() }
            }
        }
    }

    override suspend fun getAvailable(): List<ProductDetail> {
        val session = sessionRepository.getPrimarySession() ?: return emptyList()
        val options = GetPaymentsPlansOptions(
            vendor = "google",
            currency = null,
            state = null,
            timestamp = null,
            fallback = null
        )
        when (val result = session.getPaymentsPlans(options)) {
            is MailUserSessionGetPaymentsPlansResult.Error -> {
                result.v1.throwException()
            }

            is MailUserSessionGetPaymentsPlansResult.Ok -> {
                val plans = result.v1.plans
                return plans.map { plan -> plan.instances.map { instance -> plan.toProductDetail(instance) } }.flatten()
            }
        }
    }

    override suspend fun subscribe(product: ProductDetail, purchase: Purchase) {
        val session = checkNotNull(sessionRepository.getPrimarySession())
        val protonToken = getProtonToken(product, purchase, session)
        createOrUpdateSubscription(session, product, protonToken)
    }
}
