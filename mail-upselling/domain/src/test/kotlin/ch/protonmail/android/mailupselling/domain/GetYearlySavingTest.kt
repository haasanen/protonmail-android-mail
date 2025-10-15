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
            monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return the expected yearly saving (promo)`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        val referenceMonthlyPlan = monthlyPlan.copy(
            price = monthlyPlan.price.copy(amount = 1),
            renew = monthlyPlan.renew.copy(amount = 12 * 1000 * 1000)
        )
        val expected = YearlySaving("EUR", BigDecimal("36.00"))

        // When
        val actual = getYearlySaving(
            monthlyPlan = referenceMonthlyPlan,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should return null if the yearly saving is less than 0`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        val referenceMonthlyPlan = monthlyPlan.copy(
            renew = monthlyPlan.renew.copy(amount = 0)
        )

        // When
        val actual = getYearlySaving(
            monthlyPlan = referenceMonthlyPlan,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return null if the cycles mismatch (monthly)`() {
        // When
        val actual = getYearlySaving(
            monthlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail,
            yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return null if the cycles mismatch (yearly)`() {
        // When
        val actual = getYearlySaving(
            monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            yearlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        )

        // Then
        assertNull(actual)
    }
}
