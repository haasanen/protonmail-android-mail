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

import app.cash.turbine.test
import ch.protonmail.android.mailupselling.domain.usecase.GetMailPlusUpgradePlans
import ch.protonmail.android.mailupselling.domain.usecase.GetPromotionStatus
import ch.protonmail.android.mailupselling.domain.usecase.PromoStatus
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibilityOverrideSignal
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.android.core.payment.domain.model.ProductDetail
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveMailboxOneClickUpsellingVisibilityTest {

    private val getPromotionStatus = mockk<GetPromotionStatus>()
    private val upsellingVisibilityOverrideSignal = mockk<UpsellingVisibilityOverrideSignal>()
    private val getMailPlusUpgradePlans = mockk<GetMailPlusUpgradePlans>()

    private lateinit var observeUpselling: ObserveMailboxOneClickUpsellingVisibility

    @BeforeTest
    fun setup() {
        observeUpselling = ObserveMailboxOneClickUpsellingVisibility(
            getPromotionStatus,
            upsellingVisibilityOverrideSignal,
            getMailPlusUpgradePlans
        )
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should hide when override signal is forced`() = runTest {
        // Given
        every { upsellingVisibilityOverrideSignal.shouldHideUpselling() } returns flowOf(true)

        // When
        observeUpselling().test {
            // Then
            assertEquals(UpsellingVisibility.HIDDEN, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should proxy getPromotionStatus result when hide signal returns false - hidden`() = runTest {
        // Given
        every { upsellingVisibilityOverrideSignal.shouldHideUpselling() } returns flowOf(false)
        coEvery { getMailPlusUpgradePlans() } returns listOf()
        coEvery { getPromotionStatus(any()) } returns PromoStatus.NO_PLANS

        // When
        observeUpselling().test {
            // Then
            assertEquals(UpsellingVisibility.HIDDEN, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should proxy getPromotionStatus result when hide signal returns false - normal`() = runTest {
        // Given
        every { upsellingVisibilityOverrideSignal.shouldHideUpselling() } returns flowOf(false)
        val expectedPlans = listOf(mockk<ProductDetail>())
        coEvery { getMailPlusUpgradePlans() } returns expectedPlans
        every { getPromotionStatus(expectedPlans) } returns PromoStatus.NORMAL

        // When
        observeUpselling().test {
            // Then
            assertEquals(UpsellingVisibility.NORMAL, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should proxy getPromotionStatus result when hide signal returns false - promo`() = runTest {
        // Given
        every { upsellingVisibilityOverrideSignal.shouldHideUpselling() } returns flowOf(false)
        val expectedPlans = listOf(mockk<ProductDetail>())
        coEvery { getMailPlusUpgradePlans() } returns expectedPlans
        every { getPromotionStatus(expectedPlans) } returns PromoStatus.PROMO

        // When
        observeUpselling().test {
            // Then
            assertEquals(UpsellingVisibility.PROMO, awaitItem())
            awaitComplete()
        }
    }
}
