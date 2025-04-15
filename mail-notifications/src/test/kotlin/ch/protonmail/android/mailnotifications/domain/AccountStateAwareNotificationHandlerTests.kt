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

package ch.protonmail.android.mailnotifications.domain

import ch.protonmail.android.mailnotifications.domain.handler.AccountStateAwareNotificationHandler
import ch.protonmail.android.mailnotifications.domain.usecase.DismissEmailNotificationsForUser
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.After
import org.junit.Test

internal class AccountStateAwareNotificationHandlerTests {

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val dismissEmailNotificationsForUser: DismissEmailNotificationsForUser = mockk()
    private val scope = TestScope()
    private val notificationHandler = AccountStateAwareNotificationHandler(
        userSessionRepository,
        dismissEmailNotificationsForUser,
        scope
    )

    @After
    fun resetMocks() {
        unmockkAll()
    }


    @Test
    fun `should call notifications dismissal when account state becomes disabled`() = runTest {
        // given
        val expectedAccount = BaseAccount.copy(state = AccountState.Disabled)
        every { userSessionRepository.observeAccounts() } returns flowOf(listOf(expectedAccount))

        // when
        notificationHandler.handle()
        scope.advanceUntilIdle()

        // then
        verify(exactly = 1) { dismissEmailNotificationsForUser(expectedAccount.userId) }
    }

    private companion object {
        val BaseAccount = Account(
            userId = UserId("user-id"),
            name = "name",
            state = AccountState.Disabled,
            primaryAddress = "address@proton.me",
            avatarInfo = null
        )
    }
}
