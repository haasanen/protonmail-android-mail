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

package ch.protonmail.android.initializer.background

import arrow.core.right
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import ch.protonmail.android.mailsession.data.background.BackgroundExecutionWorkScheduler
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.background.PendingSendTracker
import ch.protonmail.android.mailsession.domain.background.SendCompletionScheduler
import ch.protonmail.android.mailsettings.domain.model.BackgroundSyncInterval
import ch.protonmail.android.mailsettings.domain.usecase.privacy.ObserveBackgroundSyncInterval
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Rule
import kotlin.test.Test

internal class RustWorkLifecycleObserverTest {

    val dispatcher = TestDispatcherProvider().Main

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val scheduler = mockk<BackgroundExecutionWorkScheduler>()
    private val mailSessionRepository = mockk<MailSessionRepository>()
    private val observeBackgroundSyncInterval = mockk<ObserveBackgroundSyncInterval>()
    private val appScope = CoroutineScope(dispatcher + SupervisorJob())
    private val sendCompletionScheduler = mockk<SendCompletionScheduler>(relaxUnitFun = true)
    private val pendingSendTracker = mockk<PendingSendTracker>(relaxUnitFun = true)

    // Constructed AFTER stubbing inside each test: the observer's init block
    // starts collecting observeBackgroundSyncInterval() immediately, so the mock
    // must already have an answer or strict-mockk throws.
    private fun buildObserver(
        interval: BackgroundSyncInterval,
        pendingSends: Boolean
    ): RustWorkLifecycleObserver {
        every { observeBackgroundSyncInterval.invoke() } returns
            flowOf(interval.right())
        every { pendingSendTracker.hasPendingSends() } returns pendingSends
        return RustWorkLifecycleObserver(
            mailSessionRepository,
            scheduler,
            observeBackgroundSyncInterval,
            appScope,
            sendCompletionScheduler,
            pendingSendTracker
        )
    }

    @Test
    fun `should cancel background execution and resume work when onStart is triggered`() = runTest {
        // Given
        coEvery { scheduler.cancelPendingWork() } just runs
        every { scheduler.scheduleWork(any()) } just runs
        every { mailSessionRepository.getMailSession().onEnterForeground() } just runs
        val observer = buildObserver(BackgroundSyncInterval.EVERY_15_MINUTES, pendingSends = false)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED, dispatcher)

        // When
        observer.onStart(lifecycleOwner)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { scheduler.cancelPendingWork() }
        coVerify(exactly = 1) { mailSessionRepository.getMailSession().onEnterForeground() }
        verify(exactly = 1) { pendingSendTracker.reset() }
    }

    @Test
    fun `should apply background sync interval when onStop is triggered`() = runTest {
        // Given
        coEvery { scheduler.cancelPendingWork() } just runs
        every { scheduler.scheduleWork(any()) } just runs
        every { mailSessionRepository.getMailSession().onExitForeground() } just runs
        val observer = buildObserver(BackgroundSyncInterval.EVERY_15_MINUTES, pendingSends = false)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED, dispatcher)

        // When
        observer.onStop(lifecycleOwner)
        advanceUntilIdle()

        // Then: the interval collector (init) schedules once at 15 min, onStop again
        coVerify(exactly = 1) { mailSessionRepository.getMailSession().onExitForeground() }
        verify(exactly = 2) { scheduler.scheduleWork(15L) }
        verify(exactly = 0) { sendCompletionScheduler.scheduleSendCompletion() }
    }

    @Test
    fun `should schedule send completion on onStop when a send is pending`() = runTest {
        // Given
        coEvery { scheduler.cancelPendingWork() } just runs
        every { scheduler.scheduleWork(any()) } just runs
        every { mailSessionRepository.getMailSession().onExitForeground() } just runs
        val observer = buildObserver(BackgroundSyncInterval.EVERY_15_MINUTES, pendingSends = true)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED, dispatcher)

        // When
        observer.onStop(lifecycleOwner)
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { sendCompletionScheduler.scheduleSendCompletion() }
    }
}
