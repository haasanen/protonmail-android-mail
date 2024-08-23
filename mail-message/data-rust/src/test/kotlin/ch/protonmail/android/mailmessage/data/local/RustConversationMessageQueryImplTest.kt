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
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.domain.mapper.LocalMessageId
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailmessage.data.usecase.CreateRustConversationMessagesWatcher
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.WatchedConversation
import kotlin.test.assertEquals

class RustConversationMessageQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val expectedMessages = listOf(
        LocalMessageTestData.AugWeatherForecast,
        LocalMessageTestData.SepWeatherForecast,
        LocalMessageTestData.OctWeatherForecast
    )
    private val createRustConversationMessagesWatcher: CreateRustConversationMessagesWatcher = mockk()
    private val mailbox: Mailbox = mockk()

    private val rustMailbox: RustMailbox = mockk {
        every { observeConversationMailbox() } returns flowOf(mailbox)
    }
    private val invalidationTracker: RustInvalidationTracker = mockk {
        every { notifyInvalidation(any()) } just Runs
    }

    private val rustConversationMessageQuery = RustConversationMessageQueryImpl(
        rustMailbox,
        createRustConversationMessagesWatcher,
        invalidationTracker,
        testCoroutineScope
    )

    @Test
    fun `initializes the watcher and emits initial items when called`() = runTest {
        // Given
        val conversationId = LocalConversationId(1uL)
        val messageIdToOpen = LocalMessageId(1uL)
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val conversationMessagesWatcher = mockk<WatchedConversation> {
            coEvery { this@mockk.messages } returns expectedMessages
            coEvery { this@mockk.messageIdToOpen } returns messageIdToOpen
        }
        coEvery {
            createRustConversationMessagesWatcher.invoke(mailbox, any(), capture(mailboxCallbackSlot))
        } returns conversationMessagesWatcher

        // When
        rustConversationMessageQuery.observeConversationMessages(UserIdTestData.userId, conversationId).test {

            // Then
            verify { rustMailbox.observeConversationMailbox() }
            coVerify { createRustConversationMessagesWatcher(mailbox, conversationId, any()) }
            val expected = LocalConversationMessages(messageIdToOpen, expectedMessages)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `observeConversationMessages emits new message list when callback is called`() = runTest {
        // Given
        val conversationId = LocalConversationId(1uL)
        val messageIdToOpen = LocalMessageIdSample.AugWeatherForecast
        val localConversationMessages = LocalConversationMessages(
            messageIdToOpen = messageIdToOpen,
            messages = expectedMessages
        )
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val conversationMessagesWatcher = mockk<WatchedConversation> {
            coEvery { this@mockk.messages } returns expectedMessages
            coEvery { this@mockk.messageIdToOpen } returns messageIdToOpen
        }
        coEvery {
            createRustConversationMessagesWatcher.invoke(mailbox, any(), capture(mailboxCallbackSlot))
        } returns conversationMessagesWatcher


        // When
        rustConversationMessageQuery.observeConversationMessages(UserIdTestData.userId, conversationId).test {
            // Simulate callback
            mailboxCallbackSlot.captured.onUpdate()

            // Then
            val result = awaitItem()
            assertEquals(localConversationMessages, result)
            verify { invalidationTracker.notifyInvalidation(any()) }
        }
    }
}
