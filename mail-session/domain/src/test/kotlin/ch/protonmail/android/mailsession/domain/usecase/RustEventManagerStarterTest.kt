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

import ch.protonmail.android.mailsession.domain.repository.EventLoopRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test

class RustEventManagerStarterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val eventLoopRepository = mockk<EventLoopRepository>(relaxUnitFun = true)
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val rustEventManagerStarter = RustEventManagerStarter(
        userSessionRepository,
        eventLoopRepository,
        testCoroutineScope
    )

    @Test
    fun `triggers event loop for the primary user`() {
        // Given
        val userId = UserId("primary")
        coEvery { userSessionRepository.observeCurrentUserId() } returns flowOf(userId)

        // When
        rustEventManagerStarter.start()

        // Then
        coVerify { eventLoopRepository.trigger(userId) }
    }
}
