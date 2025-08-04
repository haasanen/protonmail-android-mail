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

import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailupselling.domain.cache.AvailableUpgradesCache
import ch.protonmail.android.mailupselling.domain.usecase.ObserveMailPlusPlanUpgrades
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveMailPlusPlanUpgradesTest {

    private val availableUpgradesCache = mockk<AvailableUpgradesCache>()
    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()

    private val userId = UserId("user-id")
    private lateinit var observeMailPlusPlanUpgrades: ObserveMailPlusPlanUpgrades

    @BeforeTest
    fun setup() {
        every { observePrimaryUserId() } returns flowOf(userId)
        observeMailPlusPlanUpgrades = ObserveMailPlusPlanUpgrades(availableUpgradesCache, observePrimaryUserId)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should return filtered plans with mail plus only ids when invoked`() = runTest {
        // Given
        val expectedPlans = listOf(
            UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            UpsellingTestData.MailPlusProducts.YearlyProductDetail
        )

        coEvery { availableUpgradesCache.observe(userId) } returns flowOf(
            buildList {
                addAll(expectedPlans)
                add(UpsellingTestData.UnlimitedMailProduct.MonthlyProductDetail)
                add(UpsellingTestData.UnlimitedMailProduct.YearlyProductDetail)
            }
        )

        // When
        val actualPlans = observeMailPlusPlanUpgrades().first()

        // Then
        assertEquals(expectedPlans, actualPlans)
    }
}
