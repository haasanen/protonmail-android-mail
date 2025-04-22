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

package ch.protonmail.android.mailsession.data.background

import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import ch.protonmail.android.mailsession.data.usecase.StartBackgroundExecution
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uniffi.proton_mail_uniffi.BackgroundExecutionStatus
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class BackgroundExecutionWorkerTest(
    @Suppress("unused") private val testName: String,
    val status: BackgroundExecutionStatus,
    val expected: ListenableWorker.Result
) {

    private val startBackgroundExecution = mockk<StartBackgroundExecution>()
    private val params = mockk<WorkerParameters>()

    private val worker = BackgroundExecutionWorker(
        mockk(),
        params,
        startBackgroundExecution
    )

    @Test
    fun `should propagate the result correctly`() = runTest {
        // Given
        every { startBackgroundExecution() } returns flowOf(status)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(result, expected)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "failed status from background state",
                BackgroundExecutionStatus.Failed("failure"),
                ListenableWorker.Result.failure()
            ),
            arrayOf(
                "aborted in background status from background state",
                BackgroundExecutionStatus.AbortedInBackground,
                ListenableWorker.Result.success()
            ),
            arrayOf(
                "aborted in foreground status from background state",
                BackgroundExecutionStatus.AbortedInForeground,
                ListenableWorker.Result.success()
            ),
            arrayOf(
                "executed status from background state",
                BackgroundExecutionStatus.Executed,
                ListenableWorker.Result.success()
            ),
            arrayOf(
                "skipped no active status from background state",
                BackgroundExecutionStatus.SkippedNoActiveContexts,
                ListenableWorker.Result.success()
            ),
            arrayOf(
                "timed out from background state",
                BackgroundExecutionStatus.TimedOut,
                ListenableWorker.Result.success()
            )
        )
    }
}
