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

package ch.protonmail.android.mailconversation.data.local

import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationIdSample
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RustConversationDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val userSession = mockk<MailUserSession>()
    private val sessionManager: UserSessionRepository = mockk {
        every { observeCurrentUserSession() } returns flowOf(userSession)
    }

    private val rustMailbox: RustMailbox = mockk()
    private val rustConversationQuery: RustConversationQuery = mockk()
    private val dataSource = RustConversationDataSourceImpl(
        sessionManager, rustMailbox, rustConversationQuery, testCoroutineScope
    )

    @Test
    fun `get conversations should return list of conversations`() = runTest {
        // Given
        val labelId: LocalLabelId = 1uL
        val conversations = listOf(
            LocalConversationTestData.AugConversation,
            LocalConversationTestData.SepConversation,
            LocalConversationTestData.OctConversation
        )
        coEvery { rustConversationQuery.observeConversations(labelId) } returns flowOf(conversations)

        // When
        val result = dataSource.getConversations(labelId)

        // Then
        coVerify { rustConversationQuery.observeConversations(labelId) }
        assertEquals(conversations, result)
    }

    @Test
    fun `get conversation should return the conversation for the given id`() = runTest {
        // Given
        val conversationId = LocalConversationIdSample.AugConversation
        coEvery {
            userSession.conversationWithIdWithAllMailContext(conversationId)
        } returns LocalConversationTestData.AugConversation

        // When
        val result = dataSource.getConversation(conversationId)

        // Then
        coVerify { sessionManager.observeCurrentUserSession() }
        coVerify { userSession.conversationWithIdWithAllMailContext(conversationId) }
        assertEquals(LocalConversationTestData.AugConversation, result)
    }

    @Test
    fun `get conversation should handle mailbox exception`() = runTest {
        // Given
        val conversationId = LocalConversationIdSample.AugConversation
        coEvery {
            userSession.conversationWithIdWithAllMailContext(conversationId)
        } throws MailboxException.Db("DB Exception")

        // When
        val result = dataSource.getConversation(conversationId)

        // Then
        verify { sessionManager.observeCurrentUserSession() }
        assertNull(result)
    }

    @Test
    fun `delete conversations should delete conversations`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation,
            LocalConversationIdSample.SepConversation
        )
        val mailbox = mockk<Mailbox> {
            coEvery { deleteConversations(conversationIds) } just Runs
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { userSession.executePendingActions() } just Runs

        // When
        dataSource.deleteConversations(conversationIds)

        // Then
        verify { rustMailbox.observeConversationMailbox() }
        coVerify { mailbox.deleteConversations(conversationIds) }
        coVerify { userSession.executePendingActions() }
    }

    @Test
    fun `mark conversations read should mark conversations as read`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation, LocalConversationIdSample.SepConversation
        )
        val mailbox = mockk<Mailbox> {
            coEvery { markConversationsRead(conversationIds) } just Runs
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { userSession.executePendingActions() } just Runs

        // When
        dataSource.markRead(conversationIds)

        // Then
        verify { rustMailbox.observeConversationMailbox() }
        coVerify { mailbox.markConversationsRead(conversationIds) }
        coVerify { userSession.executePendingActions() }
    }

    @Test
    fun `mark conversations unread should mark conversations as unread`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation, LocalConversationIdSample.SepConversation
        )
        val mailbox = mockk<Mailbox> {
            coEvery { markConversationsUnread(conversationIds) } just Runs
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { userSession.executePendingActions() } just Runs

        // When
        dataSource.markUnread(conversationIds)

        // Then
        verify { rustMailbox.observeConversationMailbox() }
        coVerify { mailbox.markConversationsUnread(conversationIds) }
        coVerify { userSession.executePendingActions() }
    }

    @Test
    fun `star conversations should star conversations`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation, LocalConversationIdSample.SepConversation
        )
        val mailbox = mockk<Mailbox> {
            coEvery { starConversations(conversationIds) } just Runs
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { userSession.executePendingActions() } just Runs

        // When
        dataSource.starConversations(conversationIds)

        // Then
        verify { rustMailbox.observeConversationMailbox() }
        coVerify { mailbox.starConversations(conversationIds) }
        coVerify { userSession.executePendingActions() }
    }

    @Test
    fun `unstar conversations should unstar conversations`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation, LocalConversationIdSample.SepConversation
        )
        val mailbox = mockk<Mailbox> {
            coEvery { unstarConversations(conversationIds) } just Runs
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { userSession.executePendingActions() } just Runs

        // When
        dataSource.unStarConversations(conversationIds)

        // Then
        verify { rustMailbox.observeConversationMailbox() }
        coVerify { mailbox.unstarConversations(conversationIds) }
        coVerify { userSession.executePendingActions() }
    }

    @Test
    fun `relabel conversations should update labels`() = runTest {
        // Given
        val conversationIds = listOf(
            LocalConversationIdSample.AugConversation, LocalConversationIdSample.SepConversation
        )
        val labelsToBeRemoved = listOf(1uL)
        val labelsToBeAdded = listOf(2uL)
        val mailbox = mockk<Mailbox> {
            coEvery { unlabelConversations(1uL, conversationIds) } just Runs
            coEvery { labelConversations(2uL, conversationIds) } just Runs
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { userSession.executePendingActions() } just Runs

        // When
        dataSource.relabel(conversationIds, labelsToBeRemoved, labelsToBeAdded)

        // Then
        verify { rustMailbox.observeConversationMailbox() }
        coVerify { mailbox.unlabelConversations(1uL, conversationIds) }
        coVerify { mailbox.labelConversations(2uL, conversationIds) }
        coVerify { userSession.executePendingActions() }
    }

    @Test
    fun `disconnect should call disconnect on rustConversationQuery`() {
        // Given
        every { rustConversationQuery.disconnect() } just Runs

        // When
        dataSource.disconnect()

        // Then
        verify { rustConversationQuery.disconnect() }
    }
}
