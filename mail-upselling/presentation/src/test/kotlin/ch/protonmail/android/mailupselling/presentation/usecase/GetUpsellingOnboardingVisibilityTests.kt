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

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlanUpgrades
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlansError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.core.domain.entity.UserId
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GetUpsellingOnboardingVisibilityTests {

    private val getOnboardingPlanUpgrades = mockk<GetOnboardingPlanUpgrades>()

    private val userId = UserId("user-id")

    @Test
    fun `should return false when FF is disabled`() = runTest {
        // Given
        val isUpsellEnabled = flowOf(false)

        // When
        val actual = GetUpsellingOnboardingVisibility(
            getOnboardingPlanUpgrades,
            isUpsellEnabled
        ).invoke(userId)

        // Then
        assertFalse(actual)
    }

    @Test
    fun `should return false when FF is enabled but get plans error`() = runTest {
        // Given
        val isUpsellEnabled = flowOf(false)
        coEvery { getOnboardingPlanUpgrades.invoke(userId) } returns GetOnboardingPlansError.MismatchingPlans.left()

        // When
        val actual = GetUpsellingOnboardingVisibility(
            getOnboardingPlanUpgrades,
            isUpsellEnabled
        ).invoke(userId)

        // Then
        assertFalse(actual)
    }

    @Test
    fun `should return true when FF is enabled but and get plans returns plans`() = runTest {
        // Given
        val isUpsellEnabled = flowOf(true)
        coEvery { getOnboardingPlanUpgrades.invoke(userId) } returns listOf<ProductDetail>(mockk(), mockk()).right()

        // When
        val actual = GetUpsellingOnboardingVisibility(
            getOnboardingPlanUpgrades,
            isUpsellEnabled
        ).invoke(userId)

        // Then
        assertTrue(actual)
    }
}
