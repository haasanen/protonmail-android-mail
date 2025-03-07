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

package ch.protonmail.android.mailnotifications.data.usecase

import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailnotifications.data.remote.fcm.model.KEY_PM_REGISTRATION_WORKER_ERROR
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import uniffi.proton_mail_uniffi.DeviceEnvironment
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionUserResult
import uniffi.proton_mail_uniffi.RegisteredDevice
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RegisterAndSaveDeviceTokenTest {

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val registerAndSaveDevice = mockk<RegisterAndSaveDevice>()

    private lateinit var registerAndSaveDeviceToken: RegisterAndSaveDeviceToken

    @BeforeTest
    fun setup() {
        registerAndSaveDeviceToken = RegisterAndSaveDeviceToken(
            userSessionRepository,
            registerAndSaveDevice
        )
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should fail when user session cannot be found`() = runTest {
        // Given
        val expectedResult = Result.failure(
            workDataOf(KEY_PM_REGISTRATION_WORKER_ERROR to "Unable to find user session")
        )
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = registerAndSaveDeviceToken(userId, deviceToken = "123")

        // Then
        assertEquals(result, expectedResult)
    }

    @Test
    fun `should fail when token registration is not successful`() = runTest {
        // Given
        val token = "123"
        val userSession = expectValidUserSession(userId)
        val device = RegisteredDevice(
            deviceToken = token,
            environment = DeviceEnvironment.GOOGLE,
            pingNotificationStatus = null,
            pushNotificationStatus = null
        )
        val expectedError = Result.failure(
            workDataOf(KEY_PM_REGISTRATION_WORKER_ERROR to "Unable to register device")
        )

        coEvery { registerAndSaveDevice(userSession, device) } returns DataError.Remote.Unknown.left()

        // When
        val result = registerAndSaveDeviceToken(userId, token)

        // Then
        assertEquals(result, expectedError)
        coVerify(exactly = 1) { registerAndSaveDevice(userSession, device) }
        confirmVerified(registerAndSaveDevice)
    }

    @Test
    fun `should succeed when token registration is successful`() = runTest {
        // Given
        val token = "123"
        val userSession = expectValidUserSession(userId)
        val device = RegisteredDevice(
            deviceToken = token,
            environment = DeviceEnvironment.GOOGLE,
            pingNotificationStatus = null,
            pushNotificationStatus = null
        )

        coEvery { registerAndSaveDevice(userSession, device) } returns Unit.right()

        // When
        val result = registerAndSaveDeviceToken(userId, token)

        // Then
        assertEquals(result, Result.success())
        coVerify(exactly = 1) { registerAndSaveDevice(userSession, device) }
        confirmVerified(registerAndSaveDevice)
    }


    private fun expectValidUserSession(userId: UserId): MailUserSession {
        val mailUserSession = mockk<MailUserSession> {
            coEvery { this@mockk.user() } returns mockk<MailUserSessionUserResult.Ok>()
        }

        val sessionWrapperMock = mockk<MailUserSessionWrapper> {
            every { this@mockk.getRustUserSession() } returns mailUserSession
        }

        coEvery { userSessionRepository.getUserSession(userId) } returns sessionWrapperMock

        return mailUserSession
    }


    private companion object {

        val userId = UserId("user-id")
    }

}
