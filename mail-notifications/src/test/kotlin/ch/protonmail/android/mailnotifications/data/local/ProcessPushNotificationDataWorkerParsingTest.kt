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

package ch.protonmail.android.mailnotifications.data.local

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.protonmail.android.mailnotifications.data.local.ProcessPushNotificationDataWorker.Companion.KeyPushNotificationEncryptedMessage
import ch.protonmail.android.mailnotifications.data.local.ProcessPushNotificationDataWorker.Companion.KeyPushNotificationUid
import ch.protonmail.android.mailnotifications.data.local.ProcessPushNotificationDataWorker.Companion.KeyPushNotificationUserId
import ch.protonmail.android.mailnotifications.data.usecase.DecryptPushNotificationContent
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessMessageReadPushNotification
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessNewLoginPushNotification
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessNewMessagePushNotification
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

internal class ProcessPushNotificationDataWorkerParsingTest {

    private val context = mockk<Context>(relaxUnitFun = true)
    private val decryptNotificationContent = mockk<DecryptPushNotificationContent>()
    private val processNewMessagePushNotification = mockk<ProcessNewMessagePushNotification>()
    private val processNewLoginPushNotification = mockk<ProcessNewLoginPushNotification>()
    private val processMessageReadPushNotification = mockk<ProcessMessageReadPushNotification>()

    private val params: WorkerParameters = mockk {
        every { taskExecutor } returns mockk(relaxed = true)

        every { inputData.getString(KeyPushNotificationUserId) } returns RawUserId
        every { inputData.getString(KeyPushNotificationUid) } returns RawSessionId
        every { inputData.getString(KeyPushNotificationEncryptedMessage) } returns RawNotification
    }

    private val worker = ProcessPushNotificationDataWorker(
        context,
        params,
        decryptNotificationContent,
        processNewMessagePushNotification,
        processNewLoginPushNotification,
        processMessageReadPushNotification
    )

    @Before
    fun reset() {
        unmockkAll()
    }

    @Test
    fun `null user id makes the worker fail`() = runTest {
        // Given
        coEvery { params.inputData.getString(KeyPushNotificationUserId) } returns null

        // When
        val result = worker.doWork()

        // Then
        assertEquals(MissingInputData, result)
    }

    @Test
    fun `null session id makes the worker fail`() = runTest {
        // Given
        coEvery { params.inputData.getString(KeyPushNotificationUserId) } returns null

        // When
        val result = worker.doWork()

        // Then
        assertEquals(MissingInputData, result)
    }

    @Test
    fun `null notification data id makes the worker fail`() = runTest {
        // Given
        coEvery { params.inputData.getString(KeyPushNotificationUid) } returns null

        // When
        val result = worker.doWork()

        // Then
        assertEquals(MissingInputData, result)
    }

    @Test
    fun `null decrypted notification content makes the worker fail`() = runTest {
        // Given
        coEvery { params.inputData.getString(KeyPushNotificationEncryptedMessage) } returns null

        // When
        val result = worker.doWork()

        // Then
        assertEquals(MissingInputData, result)
    }

    private companion object {

        const val RawNotification = "notification"
        const val RawSessionId = "sessionId"
        const val RawUserId = "userId"

        val MissingInputData = ListenableWorker.Result.failure(
            workDataOf(
                ProcessPushNotificationDataWorker.KeyProcessPushNotificationDataError to
                    "Input data is missing"
            )
        )

        val FailureResultNullDecryptedContent = ListenableWorker.Result.failure(
            workDataOf(
                ProcessPushNotificationDataWorker.KeyProcessPushNotificationDataError to
                    "Unable to decrypt notification content."
            )
        )
    }
}
