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

import ch.protonmail.android.mailupselling.domain.usecase.GetMailPlusUpgradePlans
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

internal class GetMailPlusPlanUpgrades {

    private val getAvailableUpgrades = mockk<GetAvailableUpgrades>()
    private lateinit var getMailPlusUpgradePlans: GetMailPlusUpgradePlans

    @BeforeTest
    fun setup() {
        getMailPlusUpgradePlans = GetMailPlusUpgradePlans(getAvailableUpgrades)
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

        coEvery { getAvailableUpgrades() } returns buildList {
            addAll(expectedPlans)
            add(UpsellingTestData.UnlimitedMailProduct.MonthlyProductDetail)
            add(UpsellingTestData.UnlimitedMailProduct.YearlyProductDetail)
        }

        // When
        val actualPlans = getMailPlusUpgradePlans()

        // Then
        assertEquals(expectedPlans, actualPlans)
    }
}
