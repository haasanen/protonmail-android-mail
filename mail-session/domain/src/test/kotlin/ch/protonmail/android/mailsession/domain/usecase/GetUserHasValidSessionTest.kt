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

import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetUserHasValidSessionTest {

    private val userSessionRepository = mockk<UserSessionRepository>()

    private val observePrimaryUserId = ObservePrimaryUserId(userSessionRepository)
    private val getUserHasValidSession = GetUserHasValidSession(observePrimaryUserId, userSessionRepository)

    @Test
    fun `given no user session then false`() = runTest {
        // Given
        every { userSessionRepository.observePrimaryUserId() } returns flowOf(UserIdTestData.userId)
        coEvery { userSessionRepository.getUserSession(UserIdTestData.userId) } returns null

        // Then
        assertEquals(false, getUserHasValidSession())
    }

    @Test
    fun `given user session then true`() = runTest {
        // Given
        every { userSessionRepository.observePrimaryUserId() } returns flowOf(UserIdTestData.userId)
        coEvery { userSessionRepository.getUserSession(UserIdTestData.userId) } returns MailUserSessionWrapper(
            mockk()
        )

        // Then
        assertEquals(true, getUserHasValidSession())
    }
}
