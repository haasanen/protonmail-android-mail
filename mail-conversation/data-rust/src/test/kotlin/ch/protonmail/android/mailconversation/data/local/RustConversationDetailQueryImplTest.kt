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

import app.cash.turbine.test
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxConversationLiveQuery
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import kotlin.test.Test
import kotlin.test.assertEquals

class RustConversationDetailQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val conversations = listOf(
        LocalConversationTestData.AugConversation,
        LocalConversationTestData.SepConversation,
        LocalConversationTestData.OctConversation
    )

    private val mailboxCallbackSlot = slot<MailboxLiveQueryUpdatedCallback>()

    private val conversationLiveQuery: MailboxConversationLiveQuery = mockk {
        every { value() } returns conversations
    }

    private val mailbox: Mailbox = mockk {
        every { newConversationLiveQuery(any(), capture(mailboxCallbackSlot)) } returns conversationLiveQuery
    }

    private val rustMailbox: RustMailbox = mockk {
        every { observeConversationMailbox() } returns flowOf(mailbox)
    }

    private val invalidationTracker: RustInvalidationTracker = mockk {
        every { notifyInvalidation(any()) } just Runs
    }

    private val rustConversationQuery = RustConversationDetailQueryImpl(rustMailbox, testCoroutineScope)

    @Test
    fun `query initializes the mailbox and creates live query when created`() = runTest {
        // Given & When the RustConversationQuery is created

        // Then
        verify { rustMailbox.observeConversationMailbox() }
        coVerify { mailbox.newConversationLiveQuery(any(), any()) }
    }

    @Test
    fun `observing conversations with labelId switches mailbox if needed`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId: LocalLabelId = 1u
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs

        // When
        rustConversationQuery.observeConversations(userId, labelId).test {
            val conversationList = awaitItem()

            // Then
            assertNotNull(conversationList)
            coVerify { rustMailbox.switchToMailbox(userId, labelId) }
        }
    }

    @Test
    fun `new conversation list is emitted when mailbox live query callback is called`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId: LocalLabelId = 1u
        every { rustMailbox.switchToMailbox(userId, labelId) } just Runs

        rustConversationQuery.observeConversations(userId, labelId).test {
            // When
            mailboxCallbackSlot.captured.onUpdated()

            // Then
            skipItems(1) // Skip the initial empty list
            val conversationList = awaitItem()

            assertEquals(conversations, conversationList)
            verify { invalidationTracker.notifyInvalidation(any()) }
        }
    }

    @Test
    fun `disconnect nullifies conversation live query and disconnects it`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId: LocalLabelId = 1u
        every { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        every { conversationLiveQuery.disconnect() } just Runs

        // When
        rustConversationQuery.disconnect()
        rustConversationQuery.observeConversations(userId, labelId).test {
            mailboxCallbackSlot.captured.onUpdated()

            // Then
            val conversationList = awaitItem()
            coVerify { conversationLiveQuery.disconnect() }
            assertEquals(emptyList(), conversationList)
        }
    }
}
