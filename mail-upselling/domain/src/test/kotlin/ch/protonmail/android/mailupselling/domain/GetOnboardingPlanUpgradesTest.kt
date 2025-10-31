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

import arrow.core.left
import ch.protonmail.android.mailupselling.domain.cache.AvailableUpgradesCache
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlanUpgrades
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlansError
import ch.protonmail.android.mailupselling.domain.usecase.IsEligibleForBlackFridayPromotion
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetOnboardingPlanUpgradesTest {

    private val availableUpgradesCache = mockk<AvailableUpgradesCache>()
    private val userId = UserId("user-id")

    private val isEligibleForBlackFriday = mockk<IsEligibleForBlackFridayPromotion> {
        coEvery { this@mockk(userId) } returns false
    }

    private lateinit var getOnboardingPlanUpgrades: GetOnboardingPlanUpgrades

    @BeforeTest
    fun setup() {
        getOnboardingPlanUpgrades = GetOnboardingPlanUpgrades(availableUpgradesCache, isEligibleForBlackFriday)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return plans when the expected instances are present`() = runTest {
        val expectedInstances = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductOfferDetail,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductDetail
        )

        val availablePlans = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductOfferList,
            UpsellingTestData.MailPlusProducts.YearlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductOfferList
        )

        coEvery { availableUpgradesCache.get(userId) } returns availablePlans

        // When
        val actual = getOnboardingPlanUpgrades(userId).getOrNull()

        // Then
        assertEquals(expectedInstances, actual)
    }

    @Test
    fun `should return an error when no instances are present`() = runTest {
        // Given
        coEvery { availableUpgradesCache.get(userId) } returns listOf()

        // When
        val actual = getOnboardingPlanUpgrades(userId)

        // Then
        assertEquals(GetOnboardingPlansError.MismatchingPlans.left(), actual)
    }

    @Test
    fun `should return non BF instances (offer available, not eligible)`() = runTest {
        val expectedInstances = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductOfferDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductOfferDetail,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductOfferDetail,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductDetail
        )

        val availablePlans = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyBFProductOfferList,
            UpsellingTestData.MailPlusProducts.YearlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductOfferList
        )

        coEvery { availableUpgradesCache.get(userId) } returns availablePlans

        // When
        val actual = getOnboardingPlanUpgrades(userId).getOrNull()

        // Then
        assertEquals(expectedInstances, actual)
    }

    @Test
    fun `should return no plans (BF offer available, eligible)`() = runTest {
        val availablePlans = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyBFProductOfferList,
            UpsellingTestData.MailPlusProducts.YearlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductOfferList
        )

        coEvery { availableUpgradesCache.get(userId) } returns availablePlans
        coEvery { isEligibleForBlackFriday(userId) } returns true

        // When
        val actual = getOnboardingPlanUpgrades(userId).getOrNull()

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return an error when expected instances are not present`() = runTest {
        // Given
        val availablePlans = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductOfferList.copy(offers = emptyList()),
            UpsellingTestData.MailPlusProducts.YearlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductOfferList,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductOfferList
        )

        coEvery { availableUpgradesCache.get(userId) } returns availablePlans

        // When
        val actual = getOnboardingPlanUpgrades(userId)

        // Then
        assertEquals(GetOnboardingPlansError.MismatchingPlans.left(), actual)
    }
}
