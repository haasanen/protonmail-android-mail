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
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.mailsettings.data.usecase.CreateRustCustomSettings
import ch.protonmail.android.mailsettings.data.wrapper.CustomSettingsWrapper
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

class AutoAdvanceDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val createRustCustomSettings = mockk<CreateRustCustomSettings>()
    private val wrapper = mockk<CustomSettingsWrapper>()
    private lateinit var dataSource: AutoAdvanceDataSourceImpl

    private val userId = UserId("user-123")
    private val session = mockk<MailUserSessionWrapper>(relaxed = true)

    @Before
    fun setup() {
        dataSource = AutoAdvanceDataSourceImpl(
            userSessionRepository = userSessionRepository,
            createRustCustomSettings = createRustCustomSettings
        )
    }


    @Test
    fun `getAutoAdvance returns NoUserSession when session missing`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.getAutoAdvance(userId)

        // Then
        assertEquals(DataError.Local.NoUserSession.left(), result)
        verify { createRustCustomSettings wasNot Called }
    }

    @Test
    fun `getAutoAdvance maps true on success when true`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper

        coEvery { wrapper.getNextMessageOnMoveEnabled() } returns Either.Right(true)

        val expected = true

        // When
        val result = dataSource.getAutoAdvance(userId)

        // Then
        assertEquals(result, expected.right())
        coVerify { wrapper.getNextMessageOnMoveEnabled() }
    }


    @Test
    fun `getAutoAdvance maps false on success when false`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper

        coEvery { wrapper.getNextMessageOnMoveEnabled() } returns Either.Right(false)

        val expected = false

        // When
        val result = dataSource.getAutoAdvance(userId)

        // Then
        assertEquals(result, expected.right())
        coVerify { wrapper.getNextMessageOnMoveEnabled() }
    }

    @Test
    fun `getAutoAdvance return error when Rust call fails`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper

        val error = DataError.Local.CryptoError
        coEvery { wrapper.getNextMessageOnMoveEnabled() } returns error.left()

        // When
        val result = dataSource.getAutoAdvance(userId)

        // Then
        assertEquals(error.left(), result)
        coVerify { wrapper.getNextMessageOnMoveEnabled() }
    }
}
