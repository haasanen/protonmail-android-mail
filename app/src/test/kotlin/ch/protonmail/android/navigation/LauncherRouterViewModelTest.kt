/*
 * Copyright (c) 2022 Proton Technologies AG
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

package ch.protonmail.android.navigation

import app.cash.turbine.test
import ch.protonmail.android.mailpinlock.domain.usecase.ShouldPresentPinInsertionScreen
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LauncherRouterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val shouldPresentPinInsertionScreen = mockk<ShouldPresentPinInsertionScreen>()

    private val viewModel by lazy {
        LauncherRouterViewModel(shouldPresentPinInsertionScreen)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should proxy the value from the UC (true)`() = runTest {
        // Given
        every { shouldPresentPinInsertionScreen() } returns flowOf(true)

        // When + Then
        viewModel.displayAutoLockState.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `should proxy the value from the UC (false)`() = runTest {
        // Given
        every { shouldPresentPinInsertionScreen() } returns flowOf(false)

        // When + Then
        viewModel.displayAutoLockState.test {
            assertFalse(awaitItem())
        }
    }
}
