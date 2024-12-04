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

package ch.protonmail.android.mailsession.domain.usecase

import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.EventLoopRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.usecase.RustEventManagerStarter.Companion.EVENT_LOOP_DELAY
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class RustEventManagerStarterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val eventLoopRepository = mockk<EventLoopRepository>(relaxUnitFun = true)
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val rustEventManagerStarter = RustEventManagerStarter(
        userSessionRepository,
        eventLoopRepository,
        testCoroutineScope
    )

    @Test
    fun `triggers event loop for the ready user`() {
        // Given
        val userId = UserId("primary")
        every { userSessionRepository.observeAccounts() } returns flowOf(
            listOf(makeAccount(state = AccountState.Ready, userId = userId))
        )

        // When
        rustEventManagerStarter.start()
        // Simulate two event loop iterations:
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(2 * EVENT_LOOP_DELAY + 1)

        // Then
        assertEquals(false, rustEventManagerStarter.eventLoopJobs[userId]?.isCancelled)
        coVerify(exactly = 2) { eventLoopRepository.trigger(userId) }
    }

    @Test
    fun `stops event loop for the disabled user`() {
        // Given
        val userId = UserId("user-id")
        val accountFlow = MutableStateFlow(listOf(makeAccount(state = AccountState.Ready, userId = userId)))
        every { userSessionRepository.observeAccounts() } returns accountFlow

        // When
        rustEventManagerStarter.start()
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(EVENT_LOOP_DELAY + 1)

        accountFlow.value = listOf(makeAccount(state = AccountState.Disabled, userId = userId))
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Then
        assertEquals(true, rustEventManagerStarter.eventLoopJobs[userId]?.isCancelled)
        coVerify(exactly = 1) { eventLoopRepository.trigger(userId) }
    }

    private fun makeAccount(state: AccountState, userId: UserId) = Account(
        userId = userId,
        nameOrAddress = "User",
        state = state,
        username = "user",
        primaryAddress = "email",
        displayName = "User",
        avatarInfo = null
    )
}
