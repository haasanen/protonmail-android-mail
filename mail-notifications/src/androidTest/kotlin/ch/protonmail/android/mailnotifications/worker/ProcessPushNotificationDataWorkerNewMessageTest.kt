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

package ch.protonmail.android.mailnotifications.worker

import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import arrow.core.right
import ch.protonmail.android.mailnotifications.PushNotificationSample.getSampleNewMessageNotification
import ch.protonmail.android.mailnotifications.data.local.ProcessPushNotificationDataWorker
import ch.protonmail.android.mailnotifications.data.usecase.DecryptPushNotificationContent
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotification
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData.MessagePushData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationSenderData
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessMessageReadPushNotification
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessNewLoginPushNotification
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessNewMessagePushNotification
import ch.protonmail.android.test.annotations.suite.SmokeTest
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals

@SmokeTest
@SdkSuppress(maxSdkVersion = 33)
internal class ProcessPushNotificationDataWorkerNewMessageTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val decryptNotificationContent = mockk<DecryptPushNotificationContent>()
    private val processNewMessagePushNotification = mockk<ProcessNewMessagePushNotification>(relaxUnitFun = true)
    private val processNewLoginPushNotification = mockk<ProcessNewLoginPushNotification>(relaxUnitFun = true)
    private val processMessageReadPushNotification = mockk<ProcessMessageReadPushNotification>(relaxUnitFun = true)

    private val params: WorkerParameters = mockk {
        every { taskExecutor } returns mockk(relaxed = true)
        every { inputData.getString(ProcessPushNotificationDataWorker.KeyPushNotificationUid) } returns RawSessionId
        every {
            inputData.getString(ProcessPushNotificationDataWorker.KeyPushNotificationEncryptedMessage)
        } returns RawNotification
    }

    private val worker = ProcessPushNotificationDataWorker(
        context,
        params,
        decryptNotificationContent,
        processNewMessagePushNotification,
        processNewLoginPushNotification,
        processMessageReadPushNotification
    )

    private val baseNewMessageNotification = getSampleNewMessageNotification().right()

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun newMessageNotificationIsShownWhenAppIsInBackground() = runTest {
        // Given
        prepareSharedMocks()

        val userData = LocalPushNotificationData.UserPushData(UserId("primary"), "primary-email@pm.me")
        val sender = PushNotificationSenderData(
            senderName = "Proton Mail",
            senderAddress = "",
            senderGroup = ""
        )
        val pushData = MessagePushData.NewMessagePushData(sender, "aMessageId", "Notification")
        val newMessageNotificationData = LocalPushNotification.Message.NewMessage(userData, pushData)

        // When
        val result = worker.doWork()

        // Then
        coVerify(exactly = 1) {
            processNewMessagePushNotification(newMessageNotificationData)
        }
        verify {
            processNewLoginPushNotification wasNot called
            processMessageReadPushNotification wasNot called
        }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    private fun prepareSharedMocks() {
        coEvery { decryptNotificationContent(any(), any(), any()) } returns baseNewMessageNotification
        every { processMessageReadPushNotification.invoke(any()) } returns ListenableWorker.Result.success()
        coEvery { processNewMessagePushNotification.invoke(any()) } returns ListenableWorker.Result.success()
    }

    private companion object {

        const val RawNotification = "notification"
        const val RawSessionId = "sessionId"
    }
}
