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

import ch.protonmail.android.mailupselling.domain.usecase.GetPromotionStatus
import ch.protonmail.android.mailupselling.domain.usecase.PromoStatus
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import me.proton.android.core.payment.domain.usecase.GetAvailableUpgrades
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetPromotionStatusTest {

    private val getAvailableUpgrades = mockk<GetAvailableUpgrades>()
    private lateinit var getPromotionStatus: GetPromotionStatus

    @BeforeTest
    fun setup() {
        getPromotionStatus = GetPromotionStatus(getAvailableUpgrades)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should return no plans if plans cannot be fetched for the given user`() = runTest {
        // Given
        coEvery { getAvailableUpgrades() } throws Exception()

        // When
        val actual = getPromotionStatus()

        // Then
        assertEquals(PromoStatus.NO_PLANS, actual)
    }

    @Test
    fun `should return no plans if no plans are available for the given user`() = runTest {
        // Given
        coEvery { getAvailableUpgrades() } returns emptyList()

        // When
        val actual = getPromotionStatus()

        // Then
        assertEquals(PromoStatus.NO_PLANS, actual)
    }

    @Test
    fun `should return normal if the plans list is not empty for the given user without promos`() = runTest {
        // Given
        coEvery { getAvailableUpgrades() } returns listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // When
        val actual = getPromotionStatus()

        // Then
        assertEquals(PromoStatus.NORMAL, actual)
    }

    @Test
    fun `should return normal if the provided plans do not have promotions`() = runTest {
        // Given
        val plans = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // When
        val actual = getPromotionStatus(plans)

        // Then
        assertEquals(PromoStatus.NORMAL, actual)
    }

    @Test
    fun `should return promo if the one of the provided plans do have promotions`() = runTest {
        // Given
        val plans = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyPromoProductDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // When
        val actual = getPromotionStatus(plans)

        // Then
        assertEquals(PromoStatus.PROMO, actual)
    }

    @Test
    fun `should return promo if the plans list is not empty for the given user with promos`() = runTest {
        // Given
        coEvery { getAvailableUpgrades() } returns listOf(
            UpsellingTestData.MailPlusProducts.MonthlyPromoProductDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        // When
        val actual = getPromotionStatus()

        // Then
        assertEquals(PromoStatus.PROMO, actual)
    }
}
