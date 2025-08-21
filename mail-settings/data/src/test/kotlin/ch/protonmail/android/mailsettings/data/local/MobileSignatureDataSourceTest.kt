/*
 * Copyright (c) 2025 Proton Technologies AG
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
package ch.protonmail.android.mailsettings.data.local

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalMobileSignatureStatus
import ch.protonmail.android.mailcommon.data.mapper.LocalMobileSignature
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.mailsettings.data.usecase.CreateRustCustomSettings
import ch.protonmail.android.mailsettings.data.wrapper.CustomSettingsWrapper
import ch.protonmail.android.mailsettings.domain.model.MobileSignaturePreference
import ch.protonmail.android.mailsettings.domain.model.MobileSignatureStatus
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MobileSignatureDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val createRustCustomSettings = mockk<CreateRustCustomSettings>()
    private val wrapper = mockk<CustomSettingsWrapper>()

    private lateinit var dataSource: MobileSignatureDataSourceImpl

    private val userId = UserId("user-123")
    private val session = mockk<MailUserSessionWrapper>(relaxed = true)

    @Before
    fun setup() {
        dataSource = MobileSignatureDataSourceImpl(
            userSessionRepository = userSessionRepository,
            createRustCustomSettings = createRustCustomSettings
        )
    }

    @Test
    fun `getMobileSignature returns NoUserSession when session missing`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.getMobileSignature(userId)

        // Then
        assertEquals(DataError.Local.NoUserSession.left(), result)
        verify { createRustCustomSettings wasNot Called }
    }

    @Test
    fun `getMobileSignature maps local model to domain on success`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        val signature = "Best regards,\nSerdar"
        val local = LocalMobileSignature(
            body = signature,
            status = LocalMobileSignatureStatus.ENABLED
        )
        coEvery { wrapper.getMobileSignature() } returns Either.Right(local)

        val expected = MobileSignaturePreference(
            value = signature,
            status = MobileSignatureStatus.Enabled
        )

        // When
        val result = dataSource.getMobileSignature(userId)

        // Then
        assertEquals(result, expected.right())
        coVerify { wrapper.getMobileSignature() }
    }

    @Test
    fun `getMobileSignature return error when Rust call fails`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper

        val error = DataError.Local.Unknown
        coEvery { wrapper.getMobileSignature() } returns error.left()

        // When
        val result = dataSource.getMobileSignature(userId)

        // Then
        assertEquals(error.left(), result)
        coVerify { wrapper.getMobileSignature() }
    }

    @Test
    fun `setMobileSignature returns NoUserSession when session missing`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.setMobileSignature(userId, "sig")

        // Then
        assertEquals(DataError.Local.NoUserSession.left(), result)
        verify { createRustCustomSettings wasNot Called }
    }

    @Test
    fun `setMobileSignature returns right on success`() = runTest {
        // Given
        val signature = "My signature"
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        coEvery { wrapper.setMobileSignature(signature) } returns Unit.right()

        // When
        val result = dataSource.setMobileSignature(userId, signature)

        // Then
        assertEquals(Unit.right(), result)
        coVerify { wrapper.setMobileSignature(signature) }
    }

    @Test
    fun `setMobileSignature returns error when Rust call fails`() = runTest {
        // Given
        val signature = "My signature"
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        val error = DataError.Local.Unknown
        coEvery { wrapper.setMobileSignature(signature) } returns error.left()

        // When
        val result = dataSource.setMobileSignature(userId, signature)

        // Then
        assertEquals(error.left(), result)
        coVerify { wrapper.setMobileSignature(signature) }
    }

    @Test
    fun `setMobileSignatureEnabled returns NoUserSession when session missing`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.setMobileSignatureEnabled(userId, true)

        // Then
        assertEquals(DataError.Local.NoUserSession.left(), result)
        verify { createRustCustomSettings wasNot Called }
    }

    @Test
    fun `setMobileSignatureEnabled returns right on success`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        coEvery { wrapper.setMobileSignatureEnabled(true) } returns Unit.right()

        // When
        val result = dataSource.setMobileSignatureEnabled(userId, true)

        // Then
        assertEquals(Unit.right(), result)
        coVerify { wrapper.setMobileSignatureEnabled(true) }
    }

    @Test
    fun `setMobileSignatureEnabled returns error when Rust call fails`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        val error = DataError.Local.Unknown
        coEvery { wrapper.setMobileSignatureEnabled(false) } returns error.left()

        // When
        val result = dataSource.setMobileSignatureEnabled(userId, false)

        // Then
        assertEquals(error.left(), result)
        coVerify { wrapper.setMobileSignatureEnabled(false) }
    }
}
