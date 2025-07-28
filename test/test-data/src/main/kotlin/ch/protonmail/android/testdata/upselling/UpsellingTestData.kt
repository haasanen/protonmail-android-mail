/*
 * Copyright (c) 2025 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.testdata.upselling

import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.model.ProductHeader
import me.proton.android.core.payment.domain.model.ProductPrice

object UpsellingTestData {

    object MailPlusProducts {

        val MonthlyProductDetail = ProductDetail(
            productId = "productId",
            planName = "mail2022",
            header = ProductHeader(
                title = "Mail Plus",
                description = "Description",
                priceText = "12.00 EUR",
                cycleText = "/month",
                starred = false
            ),
            price = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 12,
                currency = "EUR",
                formatted = "EUR 12.00"
            ),
            renew = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 12,
                currency = "EUR",
                formatted = "EUR 12.00"
            ),
            entitlements = emptyList()
        )

        val MonthlyPromoProductDetail = ProductDetail(
            productId = "productId",
            planName = "mail2022",
            header = ProductHeader(
                title = "Mail Plus",
                description = "Description",
                priceText = "12.00 EUR",
                cycleText = "/month",
                starred = false
            ),
            price = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 9,
                currency = "EUR",
                formatted = "EUR 9.00"
            ),
            renew = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 12,
                currency = "EUR",
                formatted = "EUR 12.00"
            ),
            entitlements = emptyList()
        )

        val YearlyProductDetail = ProductDetail(
            productId = "productId",
            planName = "mail2022",
            header = ProductHeader(
                title = "Mail Plus",
                description = "Description",
                priceText = "108.00 EUR",
                cycleText = "/year",
                starred = false
            ),
            price = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 12,
                amount = 108,
                currency = "EUR",
                formatted = "EUR 108.00"
            ),
            renew = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 12,
                amount = 108,
                currency = "EUR",
                formatted = "EUR 108.00"
            ),
            entitlements = emptyList()
        )
    }

    object UnlimitedMailProduct {

        val MonthlyProductDetail = ProductDetail(
            productId = "productId",
            planName = "bundle2022",
            header = ProductHeader(
                title = "Proton Unlimited",
                description = "Description",
                priceText = "12.00 EUR",
                cycleText = "/month",
                starred = false
            ),
            price = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 12,
                currency = "EUR",
                formatted = "EUR 12.00"
            ),
            renew = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 12,
                currency = "EUR",
                formatted = "EUR 12.00"
            ),
            entitlements = emptyList()
        )

        val MonthlyPromoProductDetail = ProductDetail(
            productId = "productId",
            planName = "bundle2022",
            header = ProductHeader(
                title = "Proton Unlimited",
                description = "Description",
                priceText = "12.00 EUR",
                cycleText = "/month",
                starred = false
            ),
            price = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 9,
                currency = "EUR",
                formatted = "EUR 9.00"
            ),
            renew = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 1,
                amount = 12,
                currency = "EUR",
                formatted = "EUR 12.00"
            ),
            entitlements = emptyList()
        )

        val YearlyProductDetail = ProductDetail(
            productId = "productId",
            planName = "bundle2022",
            header = ProductHeader(
                title = "Proton Unlimited",
                description = "Description",
                priceText = "108.00 EUR",
                cycleText = "/year",
                starred = false
            ),
            price = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 12,
                amount = 108,
                currency = "EUR",
                formatted = "EUR 108.00"
            ),
            renew = ProductPrice(
                productId = "productId",
                customerId = "customerId",
                cycle = 12,
                amount = 108,
                currency = "EUR",
                formatted = "EUR 108.00"
            ),
            entitlements = emptyList()
        )
    }
}
