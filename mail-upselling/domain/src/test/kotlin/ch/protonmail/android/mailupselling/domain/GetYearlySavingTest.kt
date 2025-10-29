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

package ch.protonmail.android.mailupselling.domain

import java.math.BigDecimal
import ch.protonmail.android.mailupselling.domain.model.YearlySaving
import ch.protonmail.android.mailupselling.domain.usecase.GetYearlySaving
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import me.proton.android.core.payment.domain.model.ProductOffer
import me.proton.android.core.payment.domain.model.ProductOfferPrice
import me.proton.android.core.payment.domain.model.ProductOfferTags
import me.proton.android.core.payment.domain.model.ProductOfferToken
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetYearlySavingTest {

    private val getYearlySaving = GetYearlySaving()

    @Test
    fun `should return the expected yearly saving (standard)`() {
        // Given
        val expected = YearlySaving("EUR", BigDecimal("36.00"))

        // When
        val actual = getYearlySaving(
            monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail
        )

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return the expected yearly saving (fractional)`() {
        // Given
        val expected = YearlySaving("EUR", BigDecimal("37.20"))

        val monthlyPrice = ProductOfferPrice(
            productId = "productId",
            customerId = "customerId",
            cycle = 1,
            amount = (15.49f * 1000 * 1000).toLong(),
            currency = "EUR",
            formatted = "EUR 15.49"
        )

        val yearlyPrice = ProductOfferPrice(
            productId = "productId",
            customerId = "customerId",
            cycle = 12,
            amount = (148.68f * 1000 * 1000).toLong(),
            currency = "EUR",
            formatted = "EUR 148.69"
        )

        val monthlyOffer = ProductOffer(
            isBaseOffer = true,
            tags = ProductOfferTags(emptySet()),
            current = monthlyPrice,
            renew = monthlyPrice,
            token = ProductOfferToken("monthly")
        )

        val yearlyOffer = ProductOffer(
            isBaseOffer = true,
            tags = ProductOfferTags(emptySet()),
            current = yearlyPrice,
            renew = yearlyPrice,
            token = ProductOfferToken("yearly")
        )

        // When
        val actual = getYearlySaving(
            monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail.copy(offer = monthlyOffer),
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail.copy(offer = yearlyOffer)
        )

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return the expected yearly saving (promo)`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail

        val monthlyPrice = ProductOfferPrice(
            productId = "productId",
            customerId = "customerId",
            cycle = 1,
            amount = (1f * 1000 * 1000).toLong(),
            currency = "EUR",
            formatted = "EUR 1"
        )

        val renewPrice = ProductOfferPrice(
            productId = "productId",
            customerId = "customerId",
            cycle = 1,
            amount = (12f * 1000 * 1000).toLong(),
            currency = "EUR",
            formatted = "EUR 12.00"
        )

        val monthlyOffer = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail.offer.copy(
            isBaseOffer = false,
            tags = ProductOfferTags(setOf("tag")),
            token = ProductOfferToken("monthly"),
            current = monthlyPrice,
            renew = renewPrice
        )

        val referenceMonthlyPlan = monthlyPlan.copy(offer = monthlyOffer)
        val expected = YearlySaving("EUR", BigDecimal("36.00"))

        // When
        val actual = getYearlySaving(
            monthlyPlan = referenceMonthlyPlan,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail
        )

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return null if the yearly saving is less than 0`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail

        val renewPrice = ProductOfferPrice(
            productId = "productId",
            customerId = "customerId",
            cycle = 1,
            amount = (0f * 1000 * 1000).toLong(),
            currency = "EUR",
            formatted = "EUR 0.00"
        )

        val referenceMonthlyPlan = monthlyPlan.copy(offer = monthlyPlan.offer.copy(renew = renewPrice))

        // When
        val actual = getYearlySaving(
            monthlyPlan = referenceMonthlyPlan,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail
        )

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return null if the cycles mismatch (monthly)`() {
        // When
        val actual = getYearlySaving(
            monthlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail
        )

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return null if the cycles mismatch (yearly)`() {
        // When
        val actual = getYearlySaving(
            monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail,
            yearlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail
        )

        // Then
        assertNull(actual)
    }
}
