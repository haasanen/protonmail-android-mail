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

package ch.protonmail.android.mailupselling.presentation.usecase

import ch.protonmail.android.mailupselling.domain.model.BlackFridayPhase
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeSupportedTags
import ch.protonmail.android.mailupselling.domain.usecase.GetCurrentBlackFridayPhase
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.android.core.payment.domain.model.ProductOfferDetail
import me.proton.android.core.payment.domain.model.ProductOfferTags
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResolveUpsellingVisibilityForPlansTest {

    private val getCurrentBlackFridayPhase = mockk<GetCurrentBlackFridayPhase>()
    private lateinit var resolveUpsellingVisibilityForPlans: ResolveUpsellingVisibilityForPlans

    @BeforeTest
    fun setup() {
        resolveUpsellingVisibilityForPlans = ResolveUpsellingVisibilityForPlans(getCurrentBlackFridayPhase)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return hidden when plans size is less than 2`() = runTest {
        // Given
        val plans = listOf<ProductOfferDetail>(mockk())

        // When
        val actual = resolveUpsellingVisibilityForPlans(plans)

        // Then
        assertEquals(UpsellingVisibility.Hidden, actual)
    }

    @Test
    fun `should return normal when offer is neither BF nor intro price`() = runTest {
        // Given
        val plans = listOf(mailMonthlyBase, mailYearlyBase)

        // When
        val actual = resolveUpsellingVisibilityForPlans(plans)

        // Then
        assertEquals(UpsellingVisibility.Normal, actual)
    }

    @Test
    fun `should return BF when at least one offer is tagged with BF`() = runTest {
        // Given
        coEvery { getCurrentBlackFridayPhase() } returns BlackFridayPhase.Active.Wave1

        val plans = listOf(mailMonthlyBF, mailYearlyBase)

        // When
        val actual = resolveUpsellingVisibilityForPlans(plans)

        // Then
        assertEquals(UpsellingVisibility.Promotional.BlackFriday.Wave1, actual)
    }

    @Test
    fun `should return BF when there are both BF and intro price offers`() = runTest {
        // Given
        coEvery { getCurrentBlackFridayPhase() } returns BlackFridayPhase.Active.Wave2

        val plans = listOf(mailMonthlyBF, mailYearlyIntroPrice)

        // When
        val actual = resolveUpsellingVisibilityForPlans(plans)

        // Then
        assertEquals(UpsellingVisibility.Promotional.BlackFriday.Wave2, actual)
    }

    @Test
    fun `should return intro pricing when at least one offer is tagged with intro pricing`() = runTest {
        // Given
        val plans = listOf(mailMonthlyIntroPrice, mailYearlyBase)

        // When
        val actual = resolveUpsellingVisibilityForPlans(plans)

        // Then
        assertEquals(UpsellingVisibility.Promotional.IntroductoryPrice, actual)
    }

    @Test
    fun `should fallback to intro price if tagged as BF but there is no active phase`() = runTest {
        // Given
        val plans = listOf(mailMonthlyBF, mailYearlyIntroPrice)
        coEvery { getCurrentBlackFridayPhase() } returns BlackFridayPhase.None

        // When
        val actual = resolveUpsellingVisibilityForPlans(plans)

        // Then
        assertEquals(UpsellingVisibility.Promotional.IntroductoryPrice, actual)
    }

    @Test
    fun `should fallback to normal offer if tagged as BF but there is no active phase nor intro offer`() = runTest {
        // Given
        val plans = listOf(mailMonthlyBF, mailYearlyBase)
        coEvery { getCurrentBlackFridayPhase() } returns BlackFridayPhase.None

        // When
        val actual = resolveUpsellingVisibilityForPlans(plans)

        // Then
        assertEquals(UpsellingVisibility.Normal, actual)
    }

    private companion object {

        val mailMonthlyBase = UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail
        val mailYearlyBase = UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail
        val mailMonthlyBF = mailMonthlyBase.copy(
            offer = mailMonthlyBase.offer.copy(
                tags = ProductOfferTags(setOf(PlanUpgradeSupportedTags.BlackFriday.value))
            )
        )
        val mailMonthlyIntroPrice = mailMonthlyBase.copy(
            offer = mailMonthlyBase.offer.copy(
                tags = ProductOfferTags(setOf(PlanUpgradeSupportedTags.IntroductoryPrice.value))
            )
        )

        val mailYearlyIntroPrice = mailYearlyBase.copy(
            offer = mailMonthlyBase.offer.copy(
                tags = ProductOfferTags(setOf(PlanUpgradeSupportedTags.IntroductoryPrice.value))
            )
        )
    }
}
