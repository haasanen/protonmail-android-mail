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

import java.time.Instant
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import arrow.core.right
import ch.protonmail.android.mailnotifications.PushNotificationSample.getSampleLoginAlertNotification
import ch.protonmail.android.mailnotifications.data.local.ProcessPushNotificationDataWorker
import ch.protonmail.android.mailnotifications.data.usecase.DecryptPushNotificationContent
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotification
import ch.protonmail.android.mailnotifications.domain.model.LocalPushNotificationData
import ch.protonmail.android.mailnotifications.domain.model.PushNotificationSenderData
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessMessageReadPushNotification
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessNewLoginPushNotification
import ch.protonmail.android.mailnotifications.domain.usecase.ProcessNewMessagePushNotification
import ch.protonmail.android.test.annotations.suite.SmokeTest
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@SmokeTest
@SdkSuppress(maxSdkVersion = 33)
class ProcessPushNotificationDataWorkerLoginTest {

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

    private val baseLoginNotification = getSampleLoginAlertNotification().right()

    @Before
    fun setup() {
        mockkStatic(Instant::class)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun newLoginNotificationDataCreatesNewPushNotification() = runTest {
        // Given
        prepareSharedMocks()

        val userData = LocalPushNotificationData.UserPushData(UserId("primary"), "primary-email@pm.me")
        val sender = PushNotificationSenderData(
            senderName = "Proton Mail",
            senderAddress = "",
            senderGroup = ""
        )
        val pushData = LocalPushNotificationData.NewLoginPushData(sender, "New login attempt", "")
        val loginNotification = LocalPushNotification.Login(userData, pushData)

        // When
        val result = worker.doWork()

        // Then
        verify(exactly = 1) {
            processNewLoginPushNotification.invoke(loginNotification)
        }

        confirmVerified(processNewLoginPushNotification)
        assertEquals(ListenableWorker.Result.success(), result)
    }

    private fun prepareSharedMocks() {
        coEvery { decryptNotificationContent(any(), any(), any()) } returns baseLoginNotification
        every { processNewLoginPushNotification.invoke(any()) } returns ListenableWorker.Result.success()
        every { Instant.now() } returns mockk { every { epochSecond } returns 123 }
    }

    private companion object {

        const val RawNotification = "notification"
        const val RawSessionId = "sessionId"
    }
}
