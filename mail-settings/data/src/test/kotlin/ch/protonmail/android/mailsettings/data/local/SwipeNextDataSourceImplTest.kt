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

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.mailsettings.data.usecase.CreateRustCustomSettings
import ch.protonmail.android.mailsettings.data.wrapper.CustomSettingsWrapper
import ch.protonmail.android.mailsettings.domain.model.SwipeNextPreference
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SwipeNextDataSourceImplTest {

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val createRustCustomSettings = mockk<CreateRustCustomSettings>()
    private val wrapper = mockk<CustomSettingsWrapper>()

    private lateinit var dataSource: SwipeNextDataSourceImpl

    @Before
    fun setup() {
        dataSource = SwipeNextDataSourceImpl(
            userSessionRepository,
            createRustCustomSettings
        )
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return error on invalid user session`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val actual = dataSource.getSwipeNext(userId)

        // Then
        assertEquals(DataError.Local.NoUserSession.left(), actual)
        confirmVerified(createRustCustomSettings, wrapper)
    }

    @Test
    fun `should get the current swipe next preference value`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        val expected = SwipeNextPreference.Enabled

        coEvery { wrapper.getSwipeToAdjacentConversation() } returns expected.enabled.right()
        // When
        val actual = dataSource.getSwipeNext(userId)

        // Then
        assertEquals(expected.right(), actual)
        coVerify(exactly = 1) { wrapper.getSwipeToAdjacentConversation() }
        confirmVerified(wrapper)
    }

    @Test
    fun `should pass the error up on fetching the current swipe next preference value`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        val expected = DataError.Local.NoDataCached.left()

        coEvery { wrapper.getSwipeToAdjacentConversation() } returns expected
        // When
        val actual = dataSource.getSwipeNext(userId)

        // Then
        assertEquals(expected, actual)
        coVerify(exactly = 1) { wrapper.getSwipeToAdjacentConversation() }
        confirmVerified(wrapper)
    }

    @Test
    fun `should set the swipe next preference value`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        val newValue = SwipeNextPreference.Enabled

        coEvery { wrapper.setSwipeToAdjacentConversation(newValue.enabled) } returns Unit.right()
        // When
        val actual = dataSource.setSwipeNextEnabled(userId, newValue.enabled)

        // Then
        assertEquals(Unit.right(), actual)
        coVerify(exactly = 1) { wrapper.setSwipeToAdjacentConversation(newValue.enabled) }
        confirmVerified(wrapper)
    }

    @Test
    fun `should pass the error up on setting the current swipe next preference value`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        every { createRustCustomSettings(any()) } returns wrapper
        val expected = DataError.Local.NoDataCached.left()
        val newValue = SwipeNextPreference.Enabled

        coEvery { wrapper.setSwipeToAdjacentConversation(any()) } returns expected
        // When
        val actual = dataSource.setSwipeNextEnabled(userId, newValue.enabled)

        // Then
        assertEquals(expected, actual)
        coVerify(exactly = 1) { wrapper.setSwipeToAdjacentConversation(newValue.enabled) }
        confirmVerified(wrapper)
    }

    private companion object {

        val userId = UserId("user-id")
        val session = mockk<MailUserSessionWrapper>(relaxed = true)
    }
}
