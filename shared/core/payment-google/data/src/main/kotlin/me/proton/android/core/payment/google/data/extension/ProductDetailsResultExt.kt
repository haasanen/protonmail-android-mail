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

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult

fun ProductDetailsResult.getOrThrow(): List<ProductDetails>? {
    billingResult.checkOrThrow()
    return productDetailsList
}

inline fun <R> ProductDetailsResult.fold(
    onSuccess: (value: ProductDetailsResult) -> R,
    onFailure: (exception: Throwable) -> R
): R {
    return when (val exception = billingResult.getExceptionOrNull()) {
        null -> onSuccess(this)
        else -> onFailure(exception)
    }
}
