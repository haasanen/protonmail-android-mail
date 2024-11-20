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

package ch.protonmail.android.mailmessage.data.local

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalViewMode
import ch.protonmail.android.mailmessage.data.usecase.CreateMailbox
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.Mailbox
import kotlin.test.Test
import kotlin.test.assertEquals

class RustMailboxImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val messageMailbox: Mailbox = mockk {
        every { labelId() } returns LocalLabelId(1u)
        every { viewMode() } returns LocalViewMode.MESSAGES
    }

    private val conversationMailbox: Mailbox = mockk {
        every { labelId() } returns LocalLabelId(1u)
        every { viewMode() } returns LocalViewMode.CONVERSATIONS
    }

    private val createMailbox = mockk<CreateMailbox> {
        coEvery { this@mockk.invoke(any(), any()) } returns messageMailbox
    }

    private val userSessionRepository = mockk<UserSessionRepository>()

    private val rustMailbox = RustMailboxImpl(userSessionRepository, createMailbox)

    @Test
    fun `switchToMailbox should initialise the mailbox when there is no mailbox created`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailUserSession = mockk<MailUserSessionWrapper>()
        val labelId = LocalLabelId(1u)
        coEvery { userSessionRepository.getUserSession(userId) } returns mailUserSession

        // When
        rustMailbox.switchToMailbox(userId, labelId)
        rustMailbox.observeMailbox().test {

            // Then
            assertEquals(messageMailbox, awaitItem())
            coVerify { createMailbox(any(), labelId) }
        }
    }

    @Test
    fun `observeeMailbox returns message mailbox flow`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailUserSession = mockk<MailUserSessionWrapper>()
        val labelId = LocalLabelId(1u)
        coEvery { userSessionRepository.getUserSession(userId) } returns mailUserSession
        rustMailbox.switchToMailbox(userId, labelId)

        // When
        rustMailbox.observeMailbox().test {

            // Then
            assertEquals(messageMailbox, awaitItem())
        }
    }

    @Test
    fun `switchToMailbox should not switch the mailbox if it's already in the given label`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailUserSession = mockk<MailUserSessionWrapper>()
        val labelId = LocalLabelId(1u)
        coEvery { createMailbox(any(), labelId) } returns conversationMailbox
        coEvery { userSessionRepository.getUserSession(userId) } returns mailUserSession

        // When: First call to switchToMailbox
        rustMailbox.switchToMailbox(userId, labelId)

        // Then: Verify the mailbox is initialized
        coVerify { createMailbox(mailUserSession, labelId) }
        advanceUntilIdle()

        // When: Second call to switchToMailbox with the same labelId
        rustMailbox.switchToMailbox(userId, labelId)

        // Then: Verify that userSessionRepository.observeCurrentUserSession is not called again
        coVerify(exactly = 1) { createMailbox(mailUserSession, labelId) }
    }

    @Test
    fun `switchToMailbox should switch to a new mailbox when label changes`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailUserSession = mockk<MailUserSessionWrapper>()
        val firstLabelId = LocalLabelId(1u)
        coEvery { createMailbox(any(), firstLabelId) } returns conversationMailbox
        coEvery { userSessionRepository.getUserSession(userId) } returns mailUserSession

        // When: First call to switchToMailbox
        rustMailbox.switchToMailbox(userId, firstLabelId)

        // Then: Verify the mailbox is initialized
        coVerify { createMailbox(mailUserSession, firstLabelId) }
        advanceUntilIdle()

        // Given
        val secondLabelId = LocalLabelId(2u)

        // When: Second call to switchToMailbox with the same labelId
        rustMailbox.switchToMailbox(userId, secondLabelId)

        // Then
        coVerify { createMailbox(mailUserSession, secondLabelId) }
        advanceUntilIdle()
    }

}
