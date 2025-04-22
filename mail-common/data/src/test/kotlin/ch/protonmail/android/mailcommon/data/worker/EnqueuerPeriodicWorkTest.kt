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

package ch.protonmail.android.mailcommon.data.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.impl.model.WorkSpec
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toJavaDuration

internal class EnqueuerPeriodicWorkTest {

    private val workManager = mockk<WorkManager>()

    private val enqueuer = Enqueuer(workManager)

    @Test
    fun `should schedule periodic work with update existing policy and default request params`() = runTest {
        // Given
        val workerId = "worker-id"
        val tag = "tag"

        val expectedConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val expectedWorkSpec = WorkSpecValues(
            initialDelay = 30.seconds,
            intervalDuration = 30.minutes,
            backoffPolicy = BackoffPolicy.LINEAR,
            constraints = expectedConstraints
        )

        every {
            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = workerId,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = any()
            )
        } returns mockk()

        // When
        enqueuer.enqueueUniquePeriodicWork(
            workerId = workerId,
            worker = ListenableWorker::class.java,
            tag = tag,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            initialDelay = expectedWorkSpec.initialDelay.toJavaDuration()
        )

        // Then
        val workPolicySlot = slot<ExistingPeriodicWorkPolicy>()
        val request = slot<PeriodicWorkRequest>()

        coVerify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(workerId, capture(workPolicySlot), capture(request))
        }

        assertEquals(ExistingPeriodicWorkPolicy.UPDATE, workPolicySlot.captured)
        assertTrue(request.captured.tags.contains(tag))
        verifyWorkSpec(request.captured.workSpec, expectedWorkSpec)
        confirmVerified(workManager)
    }

    private fun verifyWorkSpec(workSpec: WorkSpec, expected: WorkSpecValues) {
        assertTrue(workSpec.isPeriodic)
        assertEquals(workSpec.initialDelay, expected.initialDelay.toLong(DurationUnit.MILLISECONDS))
        assertEquals(workSpec.intervalDuration, expected.intervalDuration.toLong(DurationUnit.MILLISECONDS))
        assertEquals(workSpec.backoffPolicy, BackoffPolicy.LINEAR)
        assertEquals(workSpec.constraints, expected.constraints)
    }

    private data class WorkSpecValues(
        val initialDelay: Duration,
        val intervalDuration: Duration,
        val backoffPolicy: BackoffPolicy,
        val constraints: Constraints
    )
}
