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

package ch.protonmail.android.mailsession.domain.usecase

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.model.User
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ObservePrimaryUserTest {

    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val observeUser = mockk<ObserveUser>()

    private lateinit var observePrimaryUser: ObservePrimaryUser

    @BeforeTest
    fun setup() {
        observePrimaryUser = ObservePrimaryUser(
            observePrimaryUserId,
            observeUser
        )
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return null on null userId`() = runTest {
        // Given
        every { observePrimaryUserId() } returns flowOf(null)

        // When + Then
        observePrimaryUser().test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should return the user on non null userId`() = runTest {
        // Given
        val userId = UserId("user-id")
        val expectedUser = mockk<User>().right()
        every { observePrimaryUserId() } returns flowOf(userId)
        every { observeUser(userId) } returns flowOf(expectedUser)

        observePrimaryUser().test {
            assertEquals(expectedUser, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should propagate the error on failed observation`() = runTest {
        // Given
        val userId = UserId("user-id")
        val expectedError = DataError.Local.NoUserSession.left()
        every { observePrimaryUserId() } returns flowOf(userId)
        every { observeUser(userId) } returns flowOf(expectedError)

        // When + Then
        observePrimaryUser().test {
            assertEquals(expectedError, awaitItem())
            awaitComplete()
        }
    }
}
