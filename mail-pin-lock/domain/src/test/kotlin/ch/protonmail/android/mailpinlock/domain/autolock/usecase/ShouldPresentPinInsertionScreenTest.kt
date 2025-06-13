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

package ch.protonmail.android.mailpinlock.domain.autolock.usecase

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

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.AppInBackgroundState
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPending
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.domain.usecase.ShouldPresentPinInsertionScreen
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.test.assertTrue

internal class ShouldPresentPinInsertionScreenTest {

    private val appInBackgroundState = mockk<AppInBackgroundState>()
    private val autoLockRepository = mockk<AutoLockRepository>()

    private val autoLockCheckPendingState = AutoLockCheckPendingState()

    private fun useCase() = ShouldPresentPinInsertionScreen(
        appInBackgroundState,
        autoLockRepository,
        autoLockCheckPendingState = autoLockCheckPendingState
    )

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should not indicate to display pin screen and do nothing when the app is in the background`() = runTest {
        // Given
        expectAppInBackground()

        // When
        val result = useCase().invoke().first()

        // Then
        assertFalse(result)
        coVerify {
            autoLockRepository wasNot called
        }
    }

    @Test
    fun `should indicate to display pin screen when the app is not background AND shouldShowPin is TRUE`() = runTest {
        // Given
        expectAppInForeground()
        coEvery { autoLockRepository.shouldAutolock() } returns true.right()
        // When
        val result = useCase().invoke().first()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should NOT indicate to display pin screen when the app is not background AND shouldShowPin is FALSE`() =
        runTest {
            // Given
            expectAppInForeground()
            coEvery { autoLockRepository.shouldAutolock() } returns false.right()

            // When
            val result = useCase().invoke().first()

            // Then
            assertFalse(result)
        }

    @Test
    fun `should not trigger pin request when app is in foreground and it is already completed`() = runTest {
        // Given
        expectAppInForeground()
        coEvery { autoLockRepository.shouldAutolock() } returns true.right()
        autoLockCheckPendingState.emitOperationSignal(AutoLockCheckPending(false))

        // When
        val result = useCase().invoke().first()

        // Then
        assertFalse(result)
    }

    private fun expectAppInForeground() {
        every { appInBackgroundState.observe() } returns flowOf(false)
    }

    private fun expectAppInBackground() {
        every { appInBackgroundState.observe() } returns flowOf(true)
    }
}
