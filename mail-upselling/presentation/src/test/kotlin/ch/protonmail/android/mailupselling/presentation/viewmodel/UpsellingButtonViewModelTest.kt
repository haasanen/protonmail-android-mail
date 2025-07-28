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
import ch.protonmail.android.mailupselling.presentation.model.UpsellingButtonState
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.mailupselling.presentation.usecase.ObserveMailboxOneClickUpsellingVisibility
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UpsellingButtonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeVisibilityUseCase = mockk<ObserveMailboxOneClickUpsellingVisibility>()

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should propagate the use case visibility (normal)`() = runTest {
        // Given
        every { observeVisibilityUseCase() } returns flowOf(UpsellingVisibility.NORMAL)
        val viewModel = UpsellingButtonViewModel(observeVisibilityUseCase)

        // When
        viewModel.state.test {
            // Then
            assertEquals(UpsellingButtonState(UpsellingVisibility.NORMAL), awaitItem())
        }
    }

    @Test
    fun `should propagate the use case visibility (promo)`() = runTest {
        // Given
        every { observeVisibilityUseCase() } returns flowOf(UpsellingVisibility.PROMO)
        val viewModel = UpsellingButtonViewModel(observeVisibilityUseCase)

        // When
        viewModel.state.test {
            // Then
            assertEquals(UpsellingButtonState(UpsellingVisibility.PROMO), awaitItem())
        }
    }

    @Test
    fun `should propagate the use case visibility (hidden)`() = runTest {
        // Given
        every { observeVisibilityUseCase() } returns flowOf(UpsellingVisibility.HIDDEN)
        val viewModel = UpsellingButtonViewModel(observeVisibilityUseCase)

        // When
        viewModel.state.test {
            // Then
            assertEquals(UpsellingButtonState(UpsellingVisibility.HIDDEN), awaitItem())
        }
    }
}
