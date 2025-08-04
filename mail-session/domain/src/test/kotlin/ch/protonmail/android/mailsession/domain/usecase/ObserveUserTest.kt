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
import arrow.core.right
import ch.protonmail.android.mailsession.domain.model.User
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveUserTest {

    private val userSessionRepository = mockk<UserSessionRepository>()

    private lateinit var observeUser: ObserveUser

    @BeforeTest
    fun setup() {
        observeUser = ObserveUser(userSessionRepository)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should proxy the call to the repository`() = runTest {
        // Given
        val userId = UserId("user-id")
        val expectedUser = mockk<User>().right()
        every { userSessionRepository.observeUser(userId) } returns flowOf(expectedUser)

        // When + Then
        observeUser(userId).test {
            assertEquals(expectedUser, awaitItem())
            awaitComplete()
        }
    }
}
