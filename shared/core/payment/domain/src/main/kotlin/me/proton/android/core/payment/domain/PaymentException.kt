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

data class PaymentException(
    val errorCode: Int?,
    override val message: String?
) : Exception("errorCode: $errorCode message: $message") {

    companion object {
        object ErrorCode {

            const val OK: Int = 0
            const val USER_CANCELED: Int = 1
            const val SERVICE_UNAVAILABLE: Int = 2
            const val BILLING_UNAVAILABLE: Int = 3
            const val ITEM_UNAVAILABLE: Int = 4
            const val DEVELOPER_ERROR: Int = 5
            const val NETWORK_ERROR: Int = 12
        }
    }
}
