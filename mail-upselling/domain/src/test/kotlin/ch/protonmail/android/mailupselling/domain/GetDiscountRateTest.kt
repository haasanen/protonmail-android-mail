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
import ch.protonmail.android.mailupselling.domain.usecase.GetDiscountRate
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetDiscountRateTest {

    private val getDiscountRate = GetDiscountRate()

    @Test
    fun `should return the expected discount rate (2 instances)`() {
        // Given
        val expected = 25

        // When
        val actual = getDiscountRate(
            shorterInstance = UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            longerInstance = UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return null if the discount rate is 0 (2 instances)`() {
        // When
        val actual = getDiscountRate(
            shorterInstance = UpsellingTestData.MailPlusProducts.YearlyProductDetail,
            longerInstance = UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return null if the yearly price is higher than the monthly price (2 instances)`() {
        // When
        val actual = getDiscountRate(
            shorterInstance = UpsellingTestData.MailPlusProducts.YearlyProductDetail,
            longerInstance = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        )

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return the expected discount rate (promo prices)`() {
        // Given
        val expected = 90

        // When
        val actual = getDiscountRate(promotionalPrice = BigDecimal(10), renewalPrice = BigDecimal(100))

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return null if the discount rate is 0 (promo prices)`() {
        // When
        val actual = getDiscountRate(promotionalPrice = BigDecimal(100), renewalPrice = BigDecimal(100))

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return null if promotional price is higher than renewal price (promo prices)`() {
        // When
        val actual = getDiscountRate(promotionalPrice = BigDecimal(500), renewalPrice = BigDecimal(1))

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return null if the discount rate would exceed 100 percent`() {
        // When
        val actual = getDiscountRate(promotionalPrice = BigDecimal(-1), renewalPrice = BigDecimal(1))

        // Then
        assertNull(actual)
    }
}
