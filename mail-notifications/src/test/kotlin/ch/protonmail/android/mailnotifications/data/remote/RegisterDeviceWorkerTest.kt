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

package ch.protonmail.android.mailnotifications.data.remote

import java.util.UUID
import androidx.work.ListenableWorker.Result
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.protonmail.android.mailcommon.data.worker.Enqueuer
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailnotifications.data.usecase.RegisterAndSaveDeviceToken
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RegisterDeviceWorkerTest {

    private val token = UUID.randomUUID().toString()
    private val userId = UserIdSample.Primary
    private val workManager: WorkManager = mockk {
        coEvery { enqueue(ofType<OneTimeWorkRequest>()) } returns mockk()
    }
    private val registerAndSaveDeviceToken = mockk<RegisterAndSaveDeviceToken>()
    private val params: WorkerParameters = mockk {
        every { taskExecutor } returns mockk(relaxed = true)
        every { inputData.getString(RegisterDeviceWorker.RawUserIdKey) } returns userId.id
        every { inputData.getString(RegisterDeviceWorker.TokenKey) } returns token
    }

    private val worker = RegisterDeviceWorker(
        context = mockk(),
        workerParameters = params,
        registerAndSaveDeviceToken = registerAndSaveDeviceToken
    )

    @Test
    fun `should enqueue worker with the correct constraints`() {
        // Given
        val expectedNetworkType = NetworkType.CONNECTED

        // When
        Enqueuer(workManager).enqueue<RegisterDeviceWorker>(userId, RegisterDeviceWorker.params(userId, token))

        // Then
        val requestSlot = slot<OneTimeWorkRequest>()
        verify { workManager.enqueue(capture(requestSlot)) }
        assertEquals(expectedNetworkType, requestSlot.captured.workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun `should return error when there is no token stored`() = runTest {
        // Given
        every { params.inputData.getString(RegisterDeviceWorker.TokenKey) } returns ""

        // When
        val result = worker.doWork()

        // Then
        assertTrue { result is Result.Failure }
    }

    @Test
    fun `should return error when saving UC fails`() = runTest {
        // Given
        coEvery { registerAndSaveDeviceToken(userId, token) } returns Result.failure()

        // When
        val result = worker.doWork()

        // Then
        assertTrue { result is Result.Failure }
    }

    @Test
    fun `should return success when saving UC succeeds`() = runTest {
        // Given
        coEvery { registerAndSaveDeviceToken(userId, token) } returns Result.success()

        // When
        val result = worker.doWork()

        // Then
        assertTrue { result is Result.Success }
    }
}
