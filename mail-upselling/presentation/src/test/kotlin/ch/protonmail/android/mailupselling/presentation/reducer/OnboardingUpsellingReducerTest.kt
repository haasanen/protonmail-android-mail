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

package ch.protonmail.android.mailupselling.presentation.reducer

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailupselling.presentation.OnboardingUpsellingReducer
import ch.protonmail.android.mailupselling.presentation.mapper.OnboardingPlanUpgradeUiMapper
import ch.protonmail.android.mailupselling.presentation.mapper.PlanMappingError
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingPlanUpgradesListUiModel
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellOperation
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellOperation.OnboardingUpsellEvent
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellState
import io.mockk.every
import io.mockk.mockk
import me.proton.core.domain.entity.UserId
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class OnboardingUpsellingReducerTest(
    @Suppress("unused") private val testName: String,
    private val operation: OnboardingUpsellOperation,
    private val mapperResult: Either<PlanMappingError, OnboardingPlanUpgradesListUiModel>,
    private val expectedState: OnboardingUpsellState
) {

    private val mapper = mockk<OnboardingPlanUpgradeUiMapper>()
    private val reducer = OnboardingUpsellingReducer(mapper)

    @Test
    fun `should reduce the state correctly`() {
        // Given
        every { mapper.toUiModel(any()) } returns mapperResult

        // When
        val updatedState = reducer.newStateFrom(operation)

        // Then
        assertEquals(expectedState, updatedState)
    }

    companion object {
        private val expectedUiModel = mockk<OnboardingPlanUpgradesListUiModel>()

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "returns data state when mapping succeeds",
                OnboardingUpsellEvent.DataLoaded(
                    UserId("user-id"),
                    listOf(mockk(), mockk())
                ),
                expectedUiModel.right(),
                OnboardingUpsellState.Data(expectedUiModel)
            ),
            arrayOf(
                "returns unsupported flow state when paid user",
                OnboardingUpsellEvent.UnsupportedFlow.PaidUser,
                mockk<Either<PlanMappingError, OnboardingPlanUpgradesListUiModel>>(),
                OnboardingUpsellState.UnsupportedFlow
            ),
            arrayOf(
                "returns unsupported flow state when not enabled",
                OnboardingUpsellEvent.UnsupportedFlow.NotEnabled,
                mockk<Either<PlanMappingError, OnboardingPlanUpgradesListUiModel>>(),
                OnboardingUpsellState.UnsupportedFlow
            ),
            arrayOf(
                "returns unsupported flow state when plans mismatch",
                OnboardingUpsellEvent.UnsupportedFlow.PlansMismatch,
                mockk<Either<PlanMappingError, OnboardingPlanUpgradesListUiModel>>(),
                OnboardingUpsellState.UnsupportedFlow
            ),
            arrayOf(
                "returns error state when loading errors",
                OnboardingUpsellEvent.LoadingError.NoUserId,
                mockk<Either<PlanMappingError, OnboardingPlanUpgradesListUiModel>>(),
                OnboardingUpsellState.Error
            ),
            arrayOf(
                "returns error state when mapping fails (invalid)",
                OnboardingUpsellEvent.DataLoaded(
                    UserId("user-id"),
                    listOf(mockk(), mockk())
                ),
                PlanMappingError.InvalidList.left(),
                OnboardingUpsellState.Error
            ),
            arrayOf(
                "returns error state when mapping fails (empty list)",
                OnboardingUpsellEvent.DataLoaded(
                    UserId("user-id"),
                    listOf(mockk(), mockk())
                ),
                PlanMappingError.EmptyList.left(),
                OnboardingUpsellState.Error
            )
        )
    }
}
