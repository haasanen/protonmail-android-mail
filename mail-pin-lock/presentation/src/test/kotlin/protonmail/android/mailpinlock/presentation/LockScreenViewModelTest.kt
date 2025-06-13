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

package protonmail.android.mailpinlock.presentation

import app.cash.turbine.test
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.model.AutoLock
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockOverlayState
import ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel.LockScreenViewModel
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

internal class LockScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val autoLockRepository = mockk<AutoLockRepository>()

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should emit interstitial on empty flow`() = runTest {
        // Given
        every { autoLockRepository.observeAppLock() } returns flowOf()

        val viewModel = LockScreenViewModel(
            autoLockRepository = autoLockRepository,
            autoLockCheckPendingState = AutoLockCheckPendingState()
        )

        // When + Then
        viewModel.state.test {
            assertEquals(AutoLockOverlayState.Loading, awaitItem())
        }
    }

    @Test
    fun `should emit interstitial on pin`() = runTest {
        // Given
        val lock = AutoLock.default().copy(protectionType = Protection.Pin)
        every { autoLockRepository.observeAppLock() } returns flowOf(lock)

        val viewModel = LockScreenViewModel(
            autoLockRepository = autoLockRepository,
            autoLockCheckPendingState = AutoLockCheckPendingState()
        )

        // When + Then
        viewModel.state.test {
            assertEquals(AutoLockOverlayState.Pin, awaitItem())
        }
    }

    @Test
    fun `should emit interstitial on biometrics`() = runTest {
        // Given
        val lock = AutoLock.default().copy(protectionType = Protection.Biometrics)
        every { autoLockRepository.observeAppLock() } returns flowOf(lock)

        val viewModel = LockScreenViewModel(
            autoLockRepository = autoLockRepository,
            autoLockCheckPendingState = AutoLockCheckPendingState()
        )

        // When + Then
        viewModel.state.test {
            assertEquals(AutoLockOverlayState.Biometrics, awaitItem())
        }
    }

    @Test
    fun `should emit error on no protection`() = runTest {
        // Given
        val lock = AutoLock.default().copy(protectionType = Protection.None)
        every { autoLockRepository.observeAppLock() } returns flowOf(lock)

        val viewModel = LockScreenViewModel(
            autoLockRepository = autoLockRepository,
            autoLockCheckPendingState = AutoLockCheckPendingState()
        )

        // When + Then
        viewModel.state.test {
            assertEquals(AutoLockOverlayState.Error, awaitItem())
        }
    }
}
