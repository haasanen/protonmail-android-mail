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

import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationIdSample
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailboxException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RustConversationDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val sessionManager = mockk<UserSessionRepository>()
    private val mailSessionRepository = mockk<MailSessionRepository>()

    private val rustMailbox: RustMailbox = mockk()
    private val rustConversationDetailQuery: RustConversationDetailQuery = mockk()
    private val rustConversationsQuery: RustConversationsQuery = mockk()
    private val dataSource = RustConversationDataSourceImpl(
        sessionManager,
        mailSessionRepository,
        rustMailbox,
        rustConversationDetailQuery,
        rustConversationsQuery,
        testCoroutineScope
    )

    @Test
    fun `get conversations should return list of conversations`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailSession>()
        val labelId = LocalLabelId(1uL)
        val conversations = listOf(
            LocalConversationTestData.AugConversation,
            LocalConversationTestData.SepConversation,
            LocalConversationTestData.OctConversation
        )
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        coEvery { rustConversationsQuery.observeConversationsByLabel(userId, labelId) } returns flowOf(conversations)

        // When
        val result = dataSource.getConversations(userId, labelId)

        // Then
        coVerify { rustConversationsQuery.observeConversationsByLabel(userId, labelId) }
        assertEquals(conversations, result)
    }

    @Test
    fun `observe conversation should return the conversation for the given id`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailSession>()
        val conversationId = LocalConversationIdSample.AugConversation
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        coEvery {
            rustConversationDetailQuery.observeConversation(userId, conversationId)
        } returns flowOf(LocalConversationTestData.AugConversation)

        // When
        val result = dataSource.observeConversation(userId, conversationId)?.first()

        // Then
        assertEquals(LocalConversationTestData.AugConversation, result)
    }

    @Test
    fun `observe conversation should handle mailbox exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailSession>()
        val conversationId = LocalConversationIdSample.AugConversation
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        coEvery {
            rustConversationDetailQuery.observeConversation(userId, conversationId)
        } throws MailboxException.Io("DB Exception")

        // When
        val result = dataSource.observeConversation(userId, conversationId)

        // Then
        assertNull(result)
    }

    @Test
    fun `disconnect should call disconnect on rustConversationQuery`() {
        // Given
        every { rustConversationDetailQuery.disconnect() } just Runs

        // When
        dataSource.disconnect()

        // Then
        verify { rustConversationDetailQuery.disconnect() }
    }
}
