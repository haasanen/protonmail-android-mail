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

package ch.protonmail.android.mailpinlock.domain.autolock

import app.cash.turbine.test
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AutoLockCheckPendingStateTest {

    private lateinit var autoLockCheckPendingState: AutoLockCheckPendingState

    @BeforeTest
    fun setUp() {
        autoLockCheckPendingState = AutoLockCheckPendingState()
    }

    @Test
    fun `triggerAutoLockCheck emits event to flow`() = runTest {
        autoLockCheckPendingState.autoLockCheckEvents.test {
            autoLockCheckPendingState.triggerAutoLockCheck()
            awaitItem()
        }
    }

    @Test
    fun `triggerAutoLockCheck called multiple times emits multiple events`() = runTest {
        autoLockCheckPendingState.autoLockCheckEvents.test {
            autoLockCheckPendingState.triggerAutoLockCheck()
            autoLockCheckPendingState.triggerAutoLockCheck()
            autoLockCheckPendingState.triggerAutoLockCheck()

            awaitItem()
            awaitItem()
            awaitItem()
        }
    }

    @Test
    fun `skipNextAutoLockCheck sets skip flag to true`() {
        autoLockCheckPendingState.skipNextAutoLockCheck()

        assertTrue(autoLockCheckPendingState.shouldSkipAndClear())
    }

    @Test
    fun `skipNextAutoLockCheck called multiple times keeps flag true`() {
        autoLockCheckPendingState.skipNextAutoLockCheck()
        autoLockCheckPendingState.skipNextAutoLockCheck()

        assertTrue(autoLockCheckPendingState.shouldSkipAndClear())
    }

    @Test
    fun `shouldSkipAndClear returns false when skip was not set`() {
        val result = autoLockCheckPendingState.shouldSkipAndClear()

        assertFalse(result)
    }

    @Test
    fun `shouldSkipAndClear returns true and clears flag when skip was set`() {
        autoLockCheckPendingState.skipNextAutoLockCheck()

        val firstCall = autoLockCheckPendingState.shouldSkipAndClear()
        val secondCall = autoLockCheckPendingState.shouldSkipAndClear()

        assertTrue(firstCall)
        assertFalse(secondCall)
    }

    @Test
    fun `shouldSkipAndClear clears flag after returning true`() {
        autoLockCheckPendingState.skipNextAutoLockCheck()
        autoLockCheckPendingState.shouldSkipAndClear()

        assertFalse(autoLockCheckPendingState.shouldSkipAndClear())
    }

    @Test
    fun `clearSkip resets skip flag to false`() {
        autoLockCheckPendingState.skipNextAutoLockCheck()

        autoLockCheckPendingState.clearSkip()

        assertFalse(autoLockCheckPendingState.shouldSkipAndClear())
    }

    @Test
    fun `clearSkip does nothing when flag is already false`() {
        autoLockCheckPendingState.clearSkip()

        assertFalse(autoLockCheckPendingState.shouldSkipAndClear())
    }

    @Test
    fun `skip and clear cycle works correctly`() {
        assertFalse(autoLockCheckPendingState.shouldSkipAndClear())

        autoLockCheckPendingState.skipNextAutoLockCheck()
        assertTrue(autoLockCheckPendingState.shouldSkipAndClear())
        assertFalse(autoLockCheckPendingState.shouldSkipAndClear())

        autoLockCheckPendingState.skipNextAutoLockCheck()
        autoLockCheckPendingState.clearSkip()
        assertFalse(autoLockCheckPendingState.shouldSkipAndClear())
    }
}
