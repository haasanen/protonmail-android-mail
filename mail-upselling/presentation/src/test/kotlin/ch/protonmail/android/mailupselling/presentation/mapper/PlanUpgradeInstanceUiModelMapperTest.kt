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
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.usecase.GetDiscountRate
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
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
    private lateinit var mapper: PlanUpgradeInstanceUiModelMapper
    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        context = RuntimeEnvironment.getApplication().applicationContext
        mapper = PlanUpgradeInstanceUiModelMapper(context, getDiscountRate)
    }

    @Test
    fun `should map standard instances correctly`() {
        // Given
        val monthlyPlan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        val yearlyPlan = UpsellingTestData.MailPlusProducts.YearlyProductDetail

        val monthlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = monthlyPlan.header.title,
            pricePerCycle = TextUiModel.Text("12"),
            totalPrice = TextUiModel.Text("12"),
            discountRate = null,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Monthly,
            product = monthlyPlan.toProduct(context)
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = yearlyPlan.header.title,
            pricePerCycle = TextUiModel.Text("9"),
            totalPrice = TextUiModel.Text("108"),
            discountRate = 25,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Yearly,
            product = yearlyPlan.toProduct(context)
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
            pricePerCycle = TextUiModel.Text("9"),
            promotionalPrice = TextUiModel.Text("9"),
            renewalPrice = TextUiModel.Text("12"),
            discountRate = 25,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Monthly,
            product = monthlyPlan.toProduct(context)
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = yearlyPlan.header.title,
            pricePerCycle = TextUiModel.Text("9"),
            totalPrice = TextUiModel.Text("108"),
            discountRate = null,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Yearly,
            product = yearlyPlan.toProduct(context)
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
            pricePerCycle = TextUiModel.Text("12"),
            totalPrice = TextUiModel.Text("12"),
            discountRate = null,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Monthly,
            product = monthlyPlan.toProduct(context)
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Promotional(
            name = yearlyPlan.header.title,
            pricePerCycle = TextUiModel.Text("4.5"),
            promotionalPrice = TextUiModel.Text("54"),
            renewalPrice = TextUiModel.Text("108"),
            discountRate = 50,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Yearly,
            product = yearlyPlan.toProduct(context)
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
