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

package ch.protonmail.android.mailupselling.presentation.mapper

import android.content.Context
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.domain.model.YearlySaving
import ch.protonmail.android.mailupselling.domain.usecase.GetDiscountRate
import ch.protonmail.android.mailupselling.domain.usecase.GetYearlySaving
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradePriceUiModel
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
internal class PlanUpgradeInstanceUiModelMapperTest {

    private val getDiscountRate = GetDiscountRate()
    private val getYearlySaving = GetYearlySaving()
    private lateinit var mapper: PlanUpgradeInstanceUiModelMapper
    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        context = RuntimeEnvironment.getApplication().applicationContext
        mapper = PlanUpgradeInstanceUiModelMapper(context, getDiscountRate, getYearlySaving)
    }

    @Test
    fun `should map standard instances correctly`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        val yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail

        val monthlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = monthlyPlan.header.title,
            pricePerCycle = PlanUpgradePriceUiModel(amount = 12f, currencyCode = "EUR"),
            totalPrice = PlanUpgradePriceUiModel(amount = 12f, currencyCode = "EUR"),
            discountRate = null,
            cycle = PlanUpgradeCycle.Monthly,
            product = monthlyPlan.toProduct(context),
            yearlySaving = null
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = yearlyPlan.header.title,
            pricePerCycle = PlanUpgradePriceUiModel(amount = 9f, currencyCode = "EUR"),
            totalPrice = PlanUpgradePriceUiModel(amount = 108f, currencyCode = "EUR"),
            discountRate = 25,
            cycle = PlanUpgradeCycle.Yearly,
            product = yearlyPlan.toProduct(context),
            yearlySaving = YearlySaving("EUR", 36.0f)
        )

        // When
        val actual = mapper.toUiModel(
            monthlyPlanInstance = monthlyPlan,
            yearlyPlanInstance = yearlyPlan
        )

        // Then
        assertEquals(Pair(monthlyExpected, yearlyExpected), actual)
    }

    @Test
    fun `should map promo instances correctly (monthly promo)`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyPromoProductDetail
        val yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail

        val monthlyExpected = PlanUpgradeInstanceUiModel.Promotional(
            name = monthlyPlan.header.title,
            pricePerCycle = PlanUpgradePriceUiModel(amount = 9f, currencyCode = "EUR"),
            promotionalPrice = PlanUpgradePriceUiModel(amount = 9f, currencyCode = "EUR"),
            renewalPrice = PlanUpgradePriceUiModel(amount = 12f, currencyCode = "EUR"),
            discountRate = 25,
            cycle = PlanUpgradeCycle.Monthly,
            product = monthlyPlan.toProduct(context),
            yearlySaving = null
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = yearlyPlan.header.title,
            pricePerCycle = PlanUpgradePriceUiModel(amount = 9f, currencyCode = "EUR"),
            totalPrice = PlanUpgradePriceUiModel(amount = 108f, currencyCode = "EUR"),
            discountRate = null,
            cycle = PlanUpgradeCycle.Yearly,
            product = yearlyPlan.toProduct(context),
            yearlySaving = YearlySaving("EUR", 36.0f)
        )

        // When
        val actual = mapper.toUiModel(
            monthlyPlanInstance = monthlyPlan,
            yearlyPlanInstance = yearlyPlan
        )

        // Then
        assertEquals(actual, Pair(monthlyExpected, yearlyExpected))
    }

    @Test
    fun `should map promo instances correctly (yearly promo)`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        val yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyPromoProductDetail

        val monthlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = monthlyPlan.header.title,
            pricePerCycle = PlanUpgradePriceUiModel(amount = 12f, currencyCode = "EUR"),
            totalPrice = PlanUpgradePriceUiModel(amount = 12f, currencyCode = "EUR"),
            discountRate = null,
            cycle = PlanUpgradeCycle.Monthly,
            product = monthlyPlan.toProduct(context),
            yearlySaving = null
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Promotional(
            name = yearlyPlan.header.title,
            pricePerCycle = PlanUpgradePriceUiModel(amount = 4.5f, currencyCode = "EUR"),
            promotionalPrice = PlanUpgradePriceUiModel(amount = 54f, currencyCode = "EUR"),
            renewalPrice = PlanUpgradePriceUiModel(amount = 108f, currencyCode = "EUR"),
            discountRate = 50,
            cycle = PlanUpgradeCycle.Yearly,
            product = yearlyPlan.toProduct(context),
            yearlySaving = YearlySaving("EUR", 90.0f)
        )

        // When
        val actual = mapper.toUiModel(
            monthlyPlanInstance = monthlyPlan,
            yearlyPlanInstance = yearlyPlan
        )

        // Then
        assertEquals(actual, Pair(monthlyExpected, yearlyExpected))
    }
}
