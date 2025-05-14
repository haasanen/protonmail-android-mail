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

package ch.protonmail.android.mailcommon.presentation

import java.util.concurrent.atomic.AtomicBoolean
import ch.protonmail.android.mailcommon.presentation.extension.launchWithDelayedCallback
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LaunchWithDelayedCallbackTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    @Test
    fun `when block completes before threshold, threshold callback is not invoked`() = runTest {
        // Given
        val thresholdExceededCalled = AtomicBoolean(false)
        val completeCalled = AtomicBoolean(false)
        val result = "result"

        // When
        val job = testScope.launchWithDelayedCallback(
            durationThreshold = 1000,
            onThresholdExceeded = { thresholdExceededCalled.set(true) },
            onComplete = { completeCalled.set(true) }
        ) {
            result
        }

        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(500)
        job.join()

        // Then
        assertFalse(thresholdExceededCalled.get(), "Threshold should not have been exceeded")
        assertTrue(completeCalled.get(), "Complete callback should have been called")
    }

    @Test
    fun `when block takes longer than threshold, threshold callback is invoked`() = runTest {
        // Given
        val thresholdExceededCalled = AtomicBoolean(false)
        val completeCalled = AtomicBoolean(false)
        val blockExecuted = AtomicBoolean(false)

        // When
        val job = testScope.launchWithDelayedCallback(
            durationThreshold = 1000,
            onThresholdExceeded = { thresholdExceededCalled.set(true) },
            onComplete = { completeCalled.set(true) }
        ) {
            delay(2000)
            blockExecuted.set(true)
            "result"
        }

        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1500)

        assertTrue(thresholdExceededCalled.get(), "Threshold callback should have been called")
        assertFalse(completeCalled.get(), "Complete callback should not have been called yet")

        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1000)
        job.join()

        // Then
        assertTrue(blockExecuted.get(), "Block should have been executed")
        assertTrue(completeCalled.get(), "Complete callback should have been called")
    }

    @Test
    @Suppress("SwallowedException")
    fun `when block throws cancellation exception, complete callback is still invoked`() = runTest {
        // Given
        val thresholdExceededCalled = AtomicBoolean(false)
        val completeCalled = AtomicBoolean(false)
        val exceptionThrown = AtomicBoolean(false)

        // When + Then
        val job = testScope.launchWithDelayedCallback(
            durationThreshold = 1000,
            onThresholdExceeded = { thresholdExceededCalled.set(true) },
            onComplete = { completeCalled.set(true) }
        ) {
            throw CancellationException()
        }

        try {
            job.join()
        } catch (_: Exception) {
            exceptionThrown.set(true)
        }

        // Assert
        assertFalse(thresholdExceededCalled.get(), "Threshold callback should not have been called")
        assertTrue(completeCalled.get(), "Complete callback should have been called despite exception")
    }

    @Test
    fun `when job is cancelled, threshold callback is not invoked and complete callback is invoked`() = runTest {
        // Given
        val thresholdExceededCalled = AtomicBoolean(false)
        val completeCalled = AtomicBoolean(false)

        // When
        val job = testScope.launchWithDelayedCallback(
            durationThreshold = 1000,
            onThresholdExceeded = { thresholdExceededCalled.set(true) },
            onComplete = { completeCalled.set(true) }
        ) {
            delay(2000)
            "result"
        }

        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(500)
        job.cancel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(thresholdExceededCalled.get(), "Threshold callback should not have been called after cancellation")
        assertTrue(completeCalled.get(), "Complete callback should have been called after cancellation")
    }

    @Test
    fun `runs with custom threshold duration`() = runTest {
        // Given
        val thresholdExceededCalled = AtomicBoolean(false)
        val customThreshold = 500L

        // When
        val job = testScope.launchWithDelayedCallback(
            durationThreshold = customThreshold,
            onThresholdExceeded = { thresholdExceededCalled.set(true) }
        ) {
            delay(1000)
            "result"
        }

        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(customThreshold + 10)

        // Then
        assertTrue(thresholdExceededCalled.get(), "Threshold callback should have been called with custom threshold")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        job.join()
    }
}
