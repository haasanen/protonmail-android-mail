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

package ch.protonmail.android.mailonboarding.presentation

import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.mailonboarding.presentation.model.OnboardingState
import ch.protonmail.android.mailonboarding.presentation.viewmodel.OnboardingViewModel
import ch.protonmail.android.mailsession.domain.model.UserAccountAge
import ch.protonmail.android.mailsession.domain.usecase.GetUserAccountCreationDays
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUser
import ch.protonmail.android.mailupselling.presentation.usecase.GetUpsellingOnboardingVisibility
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserTestData
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePrimaryUser = mockk<ObservePrimaryUser>()
    private val getUpsellingOnboardingVisibility = mockk<GetUpsellingOnboardingVisibility>()
    private val getUserAccountCreationDays = mockk<GetUserAccountCreationDays>()

    private fun viewModel() = OnboardingViewModel(
        observePrimaryUser,
        getUpsellingOnboardingVisibility,
        getUserAccountCreationDays
    )

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return no upsell when user has subscription`() = runTest {
        // Given
        every { observePrimaryUser() } returns flowOf(UserTestData.Primary.copy(subscribed = 1).right())

        // When + Then
        viewModel().state.test {
            assertEquals(OnboardingState.NoUpsell, awaitItem())
        }
    }

    @Test
    fun `should return no upsell when user has no subscription but newly created`() = runTest {
        // Given
        val user = UserTestData.Primary.copy(subscribed = 0)
        every { observePrimaryUser() } returns flowOf(user.right())
        every { getUserAccountCreationDays(user) } returns UserAccountAge(0)

        // When + Then
        viewModel().state.test {
            assertEquals(OnboardingState.NoUpsell, awaitItem())
        }
    }

    @Test
    fun `should return no upsell when user has no subscription, not newly created but visibility is off`() = runTest {
        // Given
        val user = UserTestData.Primary.copy(subscribed = 0)
        every { observePrimaryUser() } returns flowOf(user.right())
        every { getUserAccountCreationDays(user) } returns UserAccountAge(2)
        coEvery { getUpsellingOnboardingVisibility(user.userId) } returns false

        // When + Then
        viewModel().state.test {
            assertEquals(OnboardingState.NoUpsell, awaitItem())
        }
    }

    @Test
    fun `should return upsell when user has no subscription, not newly created and visibility is on`() = runTest {
        // Given
        val user = UserTestData.Primary.copy(subscribed = 0)
        every { observePrimaryUser() } returns flowOf(user.right())
        every { getUserAccountCreationDays(user) } returns UserAccountAge(2)
        coEvery { getUpsellingOnboardingVisibility(user.userId) } returns true

        // When + Then
        viewModel().state.test {
            assertEquals(OnboardingState.ToUpsell, awaitItem())
        }
    }
}
