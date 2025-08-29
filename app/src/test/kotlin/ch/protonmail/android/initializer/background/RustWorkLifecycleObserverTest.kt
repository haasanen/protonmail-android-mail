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

import androidx.lifecycle.LifecycleOwner
import ch.protonmail.android.mailsession.data.background.BackgroundExecutionWorkScheduler
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlin.test.Test

internal class RustWorkLifecycleObserverTest {

    private val scheduler = mockk<BackgroundExecutionWorkScheduler>()
    private val mailSessionRepository = mockk<MailSessionRepository>()
    private val observer = RustWorkLifecycleObserver(mailSessionRepository, scheduler)

    private val lifecycleOwner = mockk<LifecycleOwner>()

    @Test
    fun `should cancel background execution and resume work when onResume is triggered`() {
        // Given
        every { scheduler.cancelPendingWork() } just runs
        every { mailSessionRepository.getMailSession().resumeWork() } just runs

        // When
        observer.onResume(lifecycleOwner)

        // Then
        verify(exactly = 1) { scheduler.cancelPendingWork() }
        coVerify(exactly = 1) { mailSessionRepository.getMailSession().resumeWork() }
        confirmVerified(mailSessionRepository, scheduler)
    }

    @Test
    fun `should schedule background execution and pause work when onStop is triggered`() {
        // Given
        coEvery { scheduler.scheduleWork() } just runs
        every { mailSessionRepository.getMailSession().pauseWork() } just runs

        // When
        observer.onStop(lifecycleOwner)

        // Then
        coVerify(exactly = 1) { scheduler.scheduleWork() }
        coVerify(exactly = 1) { mailSessionRepository.getMailSession().pauseWork() }
        confirmVerified(mailSessionRepository, scheduler)
    }
}
