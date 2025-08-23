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
import arrow.core.right
import ch.protonmail.android.mailupselling.domain.cache.AvailableUpgradesCache
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlanUpgrades
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlansError
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

internal class GetOnboardingPlanUpgradesTest {

    private val availableUpgradesCache = mockk<AvailableUpgradesCache>()
    private val userId = UserId("user-id")

    private lateinit var getOnboardingPlanUpgrades: GetOnboardingPlanUpgrades

    @BeforeTest
    fun setup() {
        getOnboardingPlanUpgrades = GetOnboardingPlanUpgrades(availableUpgradesCache)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return plans when the expected instances are present`() = runTest {
        val expectedList = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductDetail,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductDetail,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductDetail
        )

        coEvery { availableUpgradesCache.get(userId) } returns expectedList

        // When
        val actual = getOnboardingPlanUpgrades(userId)

        // Then
        assertEquals(expectedList.right(), actual)
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
    fun `should return an error when expected instances are not present`() = runTest {
        // Given
        coEvery { availableUpgradesCache.get(userId) } returns listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductDetail.copy(planName = "anotherPlanName"),
            UpsellingTestData.MailPlusProducts.YearlyProductDetail,
            UpsellingTestData.UnlimitedMailProduct.YearlyProductDetail,
            UpsellingTestData.UnlimitedMailProduct.MonthlyProductDetail
        )

        // When
        val actual = getOnboardingPlanUpgrades(userId)

        // Then
        assertEquals(GetOnboardingPlansError.MismatchingPlans.left(), actual)
    }
}
