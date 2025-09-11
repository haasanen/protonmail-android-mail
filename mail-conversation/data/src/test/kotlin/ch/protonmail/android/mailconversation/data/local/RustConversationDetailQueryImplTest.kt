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
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationWatcher
import ch.protonmail.android.maillabel.data.local.RustMailboxFactory
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailconversation.data.usecase.GetRustConversation
import ch.protonmail.android.maillabel.data.wrapper.MailboxWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.ConversationAndMessages
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.WatchedConversation
import kotlin.test.Test
import kotlin.test.assertEquals

class RustConversationDetailQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val mailbox = mockk<MailboxWrapper>()

    private val createRustConversationWatcher: CreateRustConversationWatcher = mockk()
    private val rustMailboxFactory: RustMailboxFactory = mockk()
    private val getRustConversation: GetRustConversation = mockk()

    private val rustConversationQuery = RustConversationDetailQueryImpl(
        rustMailboxFactory,
        createRustConversationWatcher,
        getRustConversation,
        testCoroutineScope
    )

    @Test
    fun `initializes the watcher and emits initial conversation when called`() = runTest {
        // Given
        val conversationId = LocalConversationId(1uL)
        val messageToOpen = LocalMessageId(100u)
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedConversation = LocalConversationTestData.AugConversation
        val expectedMessages = LocalConversationMessages(messageToOpen, listOf(mockk()))
        val userId = UserIdTestData.userId
        val watcherMock = mockk<WatchedConversation> {
            every { this@mockk.conversation } returns expectedConversation
            every { this@mockk.messages } returns expectedMessages.messages
            every { this@mockk.messageIdToOpen } returns messageToOpen
            every { this@mockk.handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        val localLabelId = LocalLabelId(1uL)
        coEvery { rustMailboxFactory.create(userId, localLabelId) } returns mailbox.right()
        coEvery {
            createRustConversationWatcher(mailbox, conversationId, capture(callbackSlot))
        } returns watcherMock.right()

        // When
        rustConversationQuery.observeConversation(userId, conversationId, localLabelId).test {
            // Then
            assertEquals(expectedConversation.right(), awaitItem())
        }

        // When
        rustConversationQuery.observeConversationMessages(userId, conversationId, localLabelId).test {

            // Then
            val result = awaitItem()
            assertEquals(expectedMessages.right(), result)
            assertEquals(messageToOpen, result.getOrNull()?.messageIdToOpen)
        }
    }

    @Test
    fun `new conversation and messages are emitted when conversation watcher callback is called`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId = LocalConversationId(1u)
        val messageToOpen = LocalMessageId(100u)
        val messages = listOf(LocalMessageTestData.AugWeatherForecast)
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedConversation = LocalConversationTestData.AugConversation
        val expectedMessages = LocalConversationMessages(messageToOpen, messages)
        val watcherMock = mockk<WatchedConversation> {
            every { this@mockk.conversation } returns expectedConversation
            every { this@mockk.messages } returns expectedMessages.messages
            every { this@mockk.messageIdToOpen } returns messageToOpen
            every { this@mockk.handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        val localLabelId = LocalLabelId(1uL)
        coEvery { rustMailboxFactory.create(userId, localLabelId) } returns mailbox.right()
        coEvery {
            createRustConversationWatcher(mailbox, conversationId, capture(callbackSlot))
        } returns watcherMock.right()
        coEvery {
            getRustConversation(mailbox, conversationId)
        } returns ConversationAndMessages(expectedConversation, messageToOpen, messages).right()
        rustConversationQuery.observeConversation(userId, conversationId, localLabelId).test {
            skipItems(1)
            // When
            val updatedConversation = expectedConversation.copy(isStarred = true)
            val updatedMessageToOpen = LocalMessageId(200u)
            val updatedMessages = expectedMessages.copy(messageIdToOpen = updatedMessageToOpen)
            every { watcherMock.conversation } returns updatedConversation
            every { watcherMock.messages } returns updatedMessages.messages
            every { watcherMock.messageIdToOpen } returns messageToOpen
            coEvery {
                getRustConversation(mailbox, conversationId)
            } returns ConversationAndMessages(
                updatedConversation,
                updatedMessageToOpen,
                updatedMessages.messages
            ).right()

            // When
            callbackSlot.captured.onUpdate()

            // Then
            assertEquals(updatedConversation.right(), awaitItem())

            // When
            rustConversationQuery.observeConversationMessages(UserIdTestData.userId, conversationId, localLabelId)
                .test {

                    // Then
                    val result = awaitItem()
                    assertEquals(updatedMessages.right(), result)
                    assertEquals(updatedMessageToOpen, result.getOrNull()?.messageIdToOpen)
                }
        }
    }

    @Test
    fun `watcher is created only once when observing the same conversation`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId = LocalConversationId(1u)
        val messageToOpen = LocalMessageId(100u)
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedConversation = LocalConversationTestData.AugConversation
        val expectedMessages = LocalConversationMessages(messageToOpen, listOf(mockk()))
        val localLabelId = LocalLabelId(1uL)

        val watcherMock = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation
            every { messages } returns expectedMessages.messages
            every { messageIdToOpen } returns messageToOpen
            every { handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        coEvery { rustMailboxFactory.create(userId, localLabelId) } returns mailbox.right()
        coEvery {
            createRustConversationWatcher(mailbox, conversationId, capture(callbackSlot))
        } returns watcherMock.right()

        rustConversationQuery.observeConversation(userId, conversationId, localLabelId).test {
            skipItems(1)
            rustConversationQuery.observeConversation(userId, conversationId, localLabelId).test {
                assertEquals(expectedConversation.right(), awaitItem())
            }

            // Then
            coVerify(exactly = 1) { createRustConversationWatcher(mailbox, conversationId, any()) }
        }
    }

    @Test
    fun `new watcher is created when current user changes event convo ids are same`() = runTest {
        // Given
        val oldUserId = UserIdTestData.userId
        val newUserId = UserIdTestData.userId1
        val newMailbox = mockk<MailboxWrapper>()
        val conversationId1 = LocalConversationId(1u)
        val conversationId2 = LocalConversationId(1u)
        val messageToOpen = LocalMessageId(100u)
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedMessages = LocalConversationMessages(messageToOpen, listOf(mockk()))
        val expectedConversation1 = LocalConversationTestData.AugConversation
        val expectedConversation2 = expectedConversation1.copy(id = conversationId2)
        val watcherMock1 = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation1
            every { messages } returns expectedMessages.messages
            every { messageIdToOpen } returns messageToOpen
            every { handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        val watcherMock2 = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation2
            every { messages } returns expectedMessages.messages
            every { messageIdToOpen } returns messageToOpen
            every { handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        val localLabelId = LocalLabelId(1uL)
        coEvery { rustMailboxFactory.create(oldUserId, localLabelId) } returns mailbox.right()
        coEvery { rustMailboxFactory.create(newUserId, localLabelId) } returns newMailbox.right()
        coEvery {
            createRustConversationWatcher(mailbox, conversationId1, capture(callbackSlot))
        } returns watcherMock1.right()
        coEvery {
            createRustConversationWatcher(newMailbox, conversationId2, capture(callbackSlot))
        } returns watcherMock2.right()

        // When - First conversation
        val job1 = launch {
            rustConversationQuery.observeConversation(oldUserId, conversationId1, localLabelId).test {
                assertEquals(expectedConversation1.right(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
        job1.join()

        // When - Second conversation
        val job2 = launch {
            rustConversationQuery.observeConversation(newUserId, conversationId2, localLabelId).test {
                assertEquals(expectedConversation2.right(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
        job2.join()

        // Then
        coVerify(exactly = 1) { createRustConversationWatcher(mailbox, conversationId1, any()) }
        coVerify(exactly = 1) { createRustConversationWatcher(newMailbox, conversationId2, any()) }
    }

    @Test
    fun `watcher is re-created when a different conversation id is passed`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId1 = LocalConversationId(1u)
        val conversationId2 = LocalConversationId(2u)
        val messageToOpen = LocalMessageId(100u)
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedMessages = LocalConversationMessages(messageToOpen, listOf(mockk()))
        val expectedConversation1 = LocalConversationTestData.AugConversation
        val expectedConversation2 = expectedConversation1.copy(id = conversationId2)
        val watcherMock1 = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation1
            every { messages } returns expectedMessages.messages
            every { messageIdToOpen } returns messageToOpen
            every { handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        val watcherMock2 = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation2
            every { messages } returns expectedMessages.messages
            every { messageIdToOpen } returns messageToOpen
            every { handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        val localLabelId = LocalLabelId(1uL)
        coEvery { rustMailboxFactory.create(userId, localLabelId) } returns mailbox.right()
        coEvery {
            createRustConversationWatcher(mailbox, conversationId1, capture(callbackSlot))
        } returns watcherMock1.right()
        coEvery {
            createRustConversationWatcher(mailbox, conversationId2, capture(callbackSlot))
        } returns watcherMock2.right()

        // When - First conversation
        val job1 = launch {
            rustConversationQuery.observeConversation(userId, conversationId1, localLabelId).test {
                assertEquals(expectedConversation1.right(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
        job1.join()

        // When - Second conversation
        val job2 = launch {
            rustConversationQuery.observeConversation(userId, conversationId2, localLabelId).test {
                assertEquals(expectedConversation2.right(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
        job2.join()

        // Then
        coVerify(exactly = 1) { createRustConversationWatcher(mailbox, conversationId1, any()) }
        coVerify(exactly = 1) { createRustConversationWatcher(mailbox, conversationId2, any()) }
    }

    @Test
    fun `concurrent calls to observeConversation should initialize the watcher only once`() = runTest {
        // Given
        val conversationId = LocalConversationId(1uL)
        val messageToOpen = LocalMessageId(100u)
        val callbackSlot = slot<LiveQueryCallback>()
        val expectedConversation = LocalConversationTestData.AugConversation
        val expectedMessages = LocalConversationMessages(messageToOpen, listOf(mockk()))
        val userId = UserIdTestData.userId
        val watcherMock = mockk<WatchedConversation> {
            every { conversation } returns expectedConversation
            every { messages } returns expectedMessages.messages
            every { messageIdToOpen } returns messageToOpen
            every { handle } returns mockk {
                every { disconnect() } returns Unit
            }
        }
        val localLabelId = LocalLabelId(1uL)
        coEvery { rustMailboxFactory.create(userId, localLabelId) } returns mailbox.right()
        coEvery { createRustConversationWatcher(mailbox, conversationId, capture(callbackSlot)) } coAnswers {
            delay(100) // Simulate delay to enforce concurrency
            watcherMock.right()
        }

        // When
        val numberOfConcurrentCalls = 10
        val jobList = mutableListOf<Deferred<Unit>>()
        repeat(numberOfConcurrentCalls) {
            val job = async {
                rustConversationQuery.observeConversation(userId, conversationId, localLabelId).test {
                    assertEquals(expectedConversation.right(), awaitItem())
                }
            }
            jobList.add(job)
        }
        jobList.awaitAll()

        // Then
        coVerify(exactly = 1) { createRustConversationWatcher(mailbox, conversationId, any()) }
    }

}


