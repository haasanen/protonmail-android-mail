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

package me.proton.android.core.payment.google.data.model

import me.proton.android.core.payment.domain.model.PurchaseStatus
import com.android.billingclient.api.Purchase as BillingClientPurchase

fun BillingClientPurchase.toPurchase() = me.proton.android.core.payment.domain.model.Purchase(
    productType = "google",
    productId = products.first(),
    orderId = orderId ?: "unknown",
    clientId = packageName,
    customerId = accountIdentifiers?.obfuscatedAccountId ?: "unknown",
    receipt = purchaseToken,
    autoRenewing = isAutoRenewing,
    status = when {
        isAcknowledged -> PurchaseStatus.Acknowledged
        else -> PurchaseStatus.map[purchaseState] ?: PurchaseStatus.Unspecified
    }
)
