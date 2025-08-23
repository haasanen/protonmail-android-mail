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

package ch.protonmail.android.mailupselling.presentation.viewmodel

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUser
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlanUpgrades
import ch.protonmail.android.mailupselling.domain.usecase.GetOnboardingPlansError
import ch.protonmail.android.mailupselling.presentation.OnboardingUpsellingReducer
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellOperation.OnboardingUpsellEvent
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingUpsellState
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserTestData
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.android.core.payment.domain.model.ProductDetail
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OnboardingUpsellViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePrimaryUser = mockk<ObservePrimaryUser>()
    private val reducer = mockk<OnboardingUpsellingReducer>(relaxed = true)
    private val getOnboardingPlanUpgrades = mockk<GetOnboardingPlanUpgrades>()
    private val isUpsellEnabled = MutableSharedFlow<Boolean>()

    private fun viewModel() = OnboardingUpsellViewModel(
        observePrimaryUser,
        reducer,
        getOnboardingPlanUpgrades,
        isUpsellEnabled
    )

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should emit an error when no user is found`() = runTest {
        // Given
        every { observePrimaryUser() } returns flowOf(DataError.Local.NoUserSession.left())

        // When
        viewModel().state.test {
            awaitItem()
        }

        // Then
        verify { reducer.newStateFrom(OnboardingUpsellEvent.LoadingError.NoUserId) }
        confirmVerified(reducer)
    }

    @Test
    fun `should emit unsupported flow when FF is off`() = runTest {
        // Given
        every { observePrimaryUser() } returns flowOf(UserTestData.Primary.right())

        // When
        viewModel().state.test {
            isUpsellEnabled.emit(false)
            assertEquals(OnboardingUpsellState.Loading, awaitItem())
            awaitItem() // Ignore, it's a mocked state
        }

        // Then
        verify { reducer.newStateFrom(OnboardingUpsellEvent.UnsupportedFlow.NotEnabled) }
        confirmVerified(reducer)
    }

    @Test
    fun `should emit unsupported flow when user is paid`() = runTest {
        // Given
        every { observePrimaryUser() } returns flowOf(UserTestData.Primary.copy(subscribed = 1).right())

        // When
        viewModel().state.test {
            isUpsellEnabled.emit(true)
            assertEquals(OnboardingUpsellState.Loading, awaitItem())
            awaitItem() // Ignore, it's a mocked state
        }

        // Then
        verify { reducer.newStateFrom(OnboardingUpsellEvent.UnsupportedFlow.PaidUser) }
        confirmVerified(reducer)
    }

    @Test
    fun `should emit plans mismatched when product details are not fetched correctly`() = runTest {
        // Given
        val user = UserTestData.Primary.copy(subscribed = 0)
        val expectedList = listOf<ProductDetail>(mockk())
        every { observePrimaryUser() } returns flowOf(user.right())
        coEvery { getOnboardingPlanUpgrades(user.userId) } returns GetOnboardingPlansError.MismatchingPlans.left()

        // When
        viewModel().state.test {
            isUpsellEnabled.emit(true)
            assertEquals(OnboardingUpsellState.Loading, awaitItem())
            awaitItem() // Ignore, it's a mocked state
        }

        // Then
        verify { reducer.newStateFrom(OnboardingUpsellEvent.UnsupportedFlow.PlansMismatch) }
        confirmVerified(reducer)
    }

    @Test
    fun `should emit data loaded when product details are fetched correctly`() = runTest {
        // Given
        val user = UserTestData.Primary.copy(subscribed = 0)
        val expectedList = listOf<ProductDetail>(mockk())
        every { observePrimaryUser() } returns flowOf(user.right())
        coEvery { getOnboardingPlanUpgrades(user.userId) } returns expectedList.right()

        // When
        viewModel().state.test {
            isUpsellEnabled.emit(true)
            assertEquals(OnboardingUpsellState.Loading, awaitItem())
            awaitItem() // Ignore, it's a mocked state
        }

        // Then
        verify { reducer.newStateFrom(OnboardingUpsellEvent.DataLoaded(user.userId, expectedList)) }
        confirmVerified(reducer)
    }
}
