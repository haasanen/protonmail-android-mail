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
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationWatcher
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.WatchedConversation
import kotlin.test.Test
import kotlin.test.assertEquals

class RustConversationDetailQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val createRustConversationWatcher: CreateRustConversationWatcher = mockk()

    private val rustMailbox: RustMailbox = mockk()

    private val rustConversationQuery = RustConversationDetailQueryImpl(
        rustMailbox,
        createRustConversationWatcher,
        testCoroutineScope
    )

    @Test
    fun `initializes the watcher and emits initial items when called`() = runTest {
        // Given
        val conversationId = LocalConversationId(1uL)
        val mailbox = mockk<Mailbox>()
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedConversation = LocalConversationTestData.AugConversation
        val watcherMock = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { createRustConversationWatcher(mailbox, conversationId, capture(callbackSlot)) } returns watcherMock

        // When
        rustConversationQuery.observeConversation(UserIdTestData.userId, conversationId).test {
            // Then
            assertEquals(expectedConversation, awaitItem())
        }
    }

    @Test
    fun `new conversation is emitted when mailbox live query callback is called`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId = LocalConversationId(1u)
        val mailbox = mockk<Mailbox>()
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedConversation = LocalConversationTestData.AugConversation
        val watcherMock = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation
        }
        every { rustMailbox.observeConversationMailbox() } returns flowOf(mailbox)
        coEvery { createRustConversationWatcher(mailbox, conversationId, capture(callbackSlot)) } returns watcherMock

        rustConversationQuery.observeConversation(userId, conversationId).test {
            skipItems(1)
            // When
            val updatedConversation = expectedConversation.copy(isStarred = true)
            every { watcherMock.conversation } returns updatedConversation
            callbackSlot.captured.onUpdate()

            // Then
            assertEquals(updatedConversation, awaitItem())
        }
    }
}
