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
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.comparisontable.ComparisonTableEntitlement
import ch.protonmail.android.mailupselling.presentation.model.comparisontable.ComparisonTableEntitlementItemUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeDescriptionUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementsListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeIconUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeTitleUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeVariant
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.ProductInstances
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import me.proton.android.core.payment.domain.model.ProductDetail
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
internal class PlanUpgradeUiMapperTest {

    private val iconUiMapper = mockk<PlanUpgradeIconUiMapper>()
    private val titleUiMapper = mockk<PlanUpgradeTitleUiMapper>()
    private val descriptionUiMapper = mockk<PlanUpgradeDescriptionUiMapper>()
    private val instanceUiMapper = mockk<PlanUpgradeInstanceUiModelMapper>()
    private val entitlementsUiMapper = mockk<PlanUpgradeEntitlementsUiMapper>()

    private lateinit var planUpgradeUiMapper: PlanUpgradeUiMapper
    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        planUpgradeUiMapper = PlanUpgradeUiMapper(
            iconUiMapper = iconUiMapper,
            titleUiMapper = titleUiMapper,
            descriptionUiMapper = descriptionUiMapper,
            planInstanceUiMapper = instanceUiMapper,
            entitlementsUiMapper = entitlementsUiMapper
        )

        context = RuntimeEnvironment.getApplication().applicationContext
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should return a plan with no options if the instances are not present`() {
        // Given
        val products = ProductInstances(emptyList())

        // When
        val actual = planUpgradeUiMapper.toUiModel(products, UpsellingEntryPoint.Feature.Mailbox)

        // Then
        assertEquals(PlanMappingError.EmptyList.left(), actual)
    }

    @Test
    fun `should return an invalid list if the instances are the same`() {
        // Given
        val products = ProductInstances(
            listOf(
                UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
                UpsellingTestData.MailPlusProducts.MonthlyProductDetail
            )
        )

        // When
        val actual = planUpgradeUiMapper.toUiModel(products, UpsellingEntryPoint.Feature.Mailbox)

        // Then
        assertEquals(PlanMappingError.InvalidList.left(), actual)
    }

    @Test
    fun `should return a plan with two options if the instances are different`() {
        // Given
        expectIconUiModel()
        expectTitleUiModel()
        expectDescriptionUiModel()
        expectEntitlementsUiModel()

        val shorterInstance = UpsellingTestData.MailPlusProducts.MonthlyProductDetail
        val longerInstance = UpsellingTestData.MailPlusProducts.YearlyProductDetail

        val monthlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = shorterInstance.header.title,
            pricePerCycle = TextUiModel.Text("12"),
            totalPrice = TextUiModel.Text("12"),
            discountRate = null,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Monthly,
            product = shorterInstance.toProduct(context)
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = longerInstance.header.title,
            pricePerCycle = TextUiModel.Text("12"),
            totalPrice = TextUiModel.Text("12"),
            discountRate = null,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Yearly,
            product = longerInstance.toProduct(context)
        )

        expectInstanceUiModel(
            monthlyInstance = shorterInstance,
            yearlyInstance = longerInstance,
            expectedInstance = Pair(monthlyExpected, yearlyExpected)
        )

        val expectedPlansUiModel = PlanUpgradeUiModel(
            icon = ExpectedIconUiModel,
            title = ExpectedTitleUiModel,
            description = ExpectedDescriptionUiModel,
            entitlements = ExpectedEntitlementsUiModel,
            list = PlanUpgradeInstanceListUiModel.Data.Standard(monthlyExpected, yearlyExpected),
            variant = PlanUpgradeVariant.Normal
        )

        // When
        val actual = planUpgradeUiMapper.toUiModel(
            ProductInstances(listOf(shorterInstance, longerInstance)),
            UpsellingEntryPoint.Feature.Mailbox
        )

        // Then
        assertEquals(expectedPlansUiModel.right(), actual)
    }

    @Test
    fun `should return a plan with promo if the variant FF is on`() {
        // Given
        expectIconUiModel()
        expectTitleUiModelPromo()
        expectDescriptionUiModelPromo()
        expectEntitlementsUiModel()

        val shorterInstance = UpsellingTestData.MailPlusProducts.MonthlyPromoProductDetail
        val longerInstance = UpsellingTestData.MailPlusProducts.YearlyProductDetail


        val monthlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = shorterInstance.header.title,
            pricePerCycle = TextUiModel.Text("12"),
            totalPrice = TextUiModel.Text("12"),
            discountRate = null,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Monthly,
            product = shorterInstance.toProduct(context)
        )

        val yearlyExpected = PlanUpgradeInstanceUiModel.Standard(
            name = longerInstance.header.title,
            pricePerCycle = TextUiModel.Text("12"),
            totalPrice = TextUiModel.Text("12"),
            discountRate = null,
            currency = "EUR",
            cycle = PlanUpgradeCycle.Yearly,
            product = longerInstance.toProduct(context)
        )

        expectInstanceUiModel(
            monthlyInstance = shorterInstance,
            yearlyInstance = longerInstance,
            expectedInstance = Pair(monthlyExpected, yearlyExpected)
        )

        val expectedPriceFormatted = TextUiModel.TextResWithArgs(
            R.string.upselling_get_button_promotional,
            listOf("EUR", "0.1")
        )

        val expectedPlansUiModel = PlanUpgradeUiModel(
            icon = ExpectedIconUiModel,
            title = ExpectedTitleUiModelPromo,
            description = ExpectedDescriptionUiModelPromo,
            entitlements = ExpectedEntitlementsUiModel,
            list = PlanUpgradeInstanceListUiModel.Data.Standard(monthlyExpected, yearlyExpected),
            variant = PlanUpgradeVariant.IntroductoryPrice
        )

        // When
        val actual = planUpgradeUiMapper.toUiModel(
            ProductInstances(listOf(shorterInstance, longerInstance)),
            UpsellingEntryPoint.Feature.MailboxPromo
        )

        // Then
        assertEquals(expectedPlansUiModel.right(), actual)
    }

    private fun expectIconUiModel() {
        every { iconUiMapper.toUiModel(any(), any()) } returns ExpectedIconUiModel
    }

    private fun expectTitleUiModel() {
        every { titleUiMapper.toUiModel(any(), any()) } returns ExpectedTitleUiModel
    }

    private fun expectTitleUiModelPromo() {
        every { titleUiMapper.toUiModel(UpsellingEntryPoint.Feature.MailboxPromo, any()) } returns
            ExpectedTitleUiModelPromo
    }

    private fun expectDescriptionUiModel() {
        every { descriptionUiMapper.toUiModel(any(), any(), any()) } returns ExpectedDescriptionUiModel
    }

    private fun expectDescriptionUiModelPromo() {
        every { descriptionUiMapper.toUiModel(any(), any(), any()) } returns ExpectedDescriptionUiModelPromo
    }

    private fun expectEntitlementsUiModel() {
        every { entitlementsUiMapper.toUiModel(any(), any()) } returns
            PlanUpgradeEntitlementsListUiModel.ComparisonTableList(ExpectedEntitlementsUiModel.items)
    }

    @Suppress("LongParameterList")
    private fun expectInstanceUiModel(
        monthlyInstance: ProductDetail,
        yearlyInstance: ProductDetail,
        expectedInstance: Pair<PlanUpgradeInstanceUiModel, PlanUpgradeInstanceUiModel>
    ) {
        every { instanceUiMapper.toUiModel(monthlyInstance, yearlyInstance) } returns expectedInstance
    }

    private companion object {

        val ExpectedIconUiModel = PlanUpgradeIconUiModel(R.drawable.illustration_upselling_mailbox)
        val ExpectedTitleUiModel = PlanUpgradeTitleUiModel(TextUiModel.Text("title"))
        val ExpectedTitleUiModelPromo = PlanUpgradeTitleUiModel(TextUiModel.Text("title-promo"))

        val ExpectedDescriptionUiModel = PlanUpgradeDescriptionUiModel.Simple(TextUiModel.Text("description"))
        val ExpectedDescriptionUiModelPromo = PlanUpgradeDescriptionUiModel.Simple(
            TextUiModel.Text("description-promo")
        )
        val ExpectedEntitlementsUiModel = PlanUpgradeEntitlementsListUiModel.ComparisonTableList(
            listOf(
                ComparisonTableEntitlementItemUiModel(
                    title = TextUiModel.Text("item"),
                    freeValue = ComparisonTableEntitlement.Free.Value(TextUiModel.Text("")),
                    paidValue = ComparisonTableEntitlement.Plus.Value(TextUiModel.Text("12"))
                )
            )
        )
    }
}
