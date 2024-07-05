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
import ch.protonmail.android.mailmessage.data.usecase.CreateMailbox
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_api_mail.MailSettingsViewMode
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Mailbox
import kotlin.test.Test
import kotlin.test.assertEquals

class RustMailboxImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val messageMailbox: Mailbox = mockk {
        every { labelId() } returns 1u
        every { viewMode() } returns MailSettingsViewMode.MESSAGES
    }

    private val conversationMailbox: Mailbox = mockk {
        every { labelId() } returns 1u
        every { viewMode() } returns MailSettingsViewMode.CONVERSATIONS
    }

    private val createMailbox = mockk<CreateMailbox> {
        coEvery { this@mockk.invoke(any(), any()) } returns messageMailbox
    }

    private val mailUserSession = mockk<MailUserSession>()
    private val userSessionRepository: UserSessionRepository = mockk {
        every { observeCurrentUserSession() } returns flowOf(mailUserSession)
    }

    private val rustMailbox: RustMailbox = RustMailboxImpl(userSessionRepository, createMailbox, testCoroutineScope)

    @Test
    fun `switchToMailbox should initialise the mailbox when there is no mailbox created`() = runTest {
        // Given
        val labelId: LocalLabelId = 1u

        // When
        rustMailbox.switchToMailbox(labelId)
        rustMailbox.observeMessageMailbox().test {

            // Then
            assertEquals(messageMailbox, awaitItem())
            coVerify { createMailbox(any(), labelId) }
        }
    }

    @Test
    fun `observeMessageMailbox returns message mailbox flow`() = runTest {
        // Given
        val labelId: LocalLabelId = 1u
        rustMailbox.switchToMailbox(labelId)

        // When
        rustMailbox.observeMessageMailbox().test {

            // Then
            assertEquals(messageMailbox, awaitItem())
        }
    }

    @Test
    fun `observeConversationMailbox returns conversation mailbox flow`() = runTest {
        // Given
        val labelId: LocalLabelId = 1u
        coEvery { createMailbox(any(), labelId) } returns conversationMailbox
        rustMailbox.switchToMailbox(labelId)

        // When
        rustMailbox.observeConversationMailbox().test {

            // Then
            assertEquals(conversationMailbox, awaitItem())
        }
    }


    @Test
    fun `switchToMailbox should not switch the mailbox if it's already in the given label`() = runTest {
        // Given
        val labelId: LocalLabelId = 1u

        // When: First call to switchToMailbox
        rustMailbox.switchToMailbox(labelId)

        // Then: Verify the mailbox is initialized
        coVerify { createMailbox(mailUserSession, labelId) }
        advanceUntilIdle()

        // When: Second call to switchToMailbox with the same labelId
        rustMailbox.switchToMailbox(labelId)

        // Then: Verify that userSessionRepository.observeCurrentUserSession is not called again
        coVerify(exactly = 1) { createMailbox(mailUserSession, labelId) }
    }

    @Test
    fun `switchToMailbox should switch to a new mailbox when label changes`() = runTest {
        // Given
        val firstLabelId: LocalLabelId = 1u

        // When: First call to switchToMailbox
        rustMailbox.switchToMailbox(firstLabelId)

        // Then: Verify the mailbox is initialized
        coVerify { createMailbox(mailUserSession, firstLabelId) }
        advanceUntilIdle()

        // Given
        val secondLabelId: LocalLabelId = 2u

        // When: Second call to switchToMailbox with the same labelId
        rustMailbox.switchToMailbox(secondLabelId)

        // Then
        coVerify { createMailbox(mailUserSession, secondLabelId) }
        advanceUntilIdle()
    }

}
