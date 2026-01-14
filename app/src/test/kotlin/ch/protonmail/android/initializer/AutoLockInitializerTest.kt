/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.initializer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import ch.protonmail.android.mailpinlock.data.StartAutoLockCountdown
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AutoLockInitializerTest {

    private lateinit var autoLockInitializer: AutoLockInitializer

    private val lifecycleOwner = mockk<LifecycleOwner> {
        every { this@mockk.lifecycle } returns mockk(relaxed = true)
    }
    private val pendingState = mockk<AutoLockCheckPendingState> {
        every { this@mockk.triggerAutoLockCheck() } just runs
        every { this@mockk.clearSkip() } just runs

    }
    private val startCountdown = mockk<StartAutoLockCountdown> {
        every { this@mockk.invoke() } just runs
    }

    @Before
    fun setUp() {
        autoLockInitializer = AutoLockInitializer()

        // Inject mocks into private fields
        autoLockInitializer.autoLockCheckPendingState = pendingState
        autoLockInitializer.startAutoLockCountdown = startCountdown
    }

    @Test
    fun `triggers auto lock check when the app is started (on first Resume)`() {

        // Given & When
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_RESUME)

        // Then
        verify(exactly = 1) { pendingState.triggerAutoLockCheck() }
        verify(exactly = 0) { pendingState.clearSkip() }
        verify(exactly = 0) { startCountdown.invoke() }
    }

    @Test
    fun `does not trigger auto lock check when the app is paused and resumed (not stopped)`() {

        // Given (app launched)
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_RESUME)

        // When
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_PAUSE)
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_RESUME)

        // Then
        verify(exactly = 1) { pendingState.triggerAutoLockCheck() } // still 1, no new call
        verify(exactly = 0) { pendingState.clearSkip() }
        verify(exactly = 0) { startCountdown.invoke() }
    }

    @Test
    fun `starts countdown and clears skip when the app is stopped`() {
        // Given (app launched)
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_RESUME)

        // When
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_STOP)

        // Then
        verify(exactly = 1) { pendingState.clearSkip() }
        verify(exactly = 1) { startCountdown.invoke() }
    }

    @Test
    fun `triggers auto lock check again when the app is stopped and resumed again`() {
        // Given (app launched)
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_RESUME)

        // When
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_STOP)
        autoLockInitializer.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_RESUME)

        // Then
        verify(exactly = 2) { pendingState.triggerAutoLockCheck() }
        verify(exactly = 1) { pendingState.clearSkip() }
        verify(exactly = 1) { startCountdown.invoke() }
    }

}
