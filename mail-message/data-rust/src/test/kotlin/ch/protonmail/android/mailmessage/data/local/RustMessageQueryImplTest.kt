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
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesWatcher
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
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
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.WatchedMessages
import kotlin.test.Test
import kotlin.test.assertEquals

class RustMessageQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val expectedMessages = listOf(
        LocalMessageTestData.AugWeatherForecast,
        LocalMessageTestData.SepWeatherForecast,
        LocalMessageTestData.OctWeatherForecast
    )

    private val messagesWatcher: WatchedMessages = mockk()

    private val rustMailbox: RustMailbox = mockk()
    private val createRustMessagesWatcher: CreateRustMessagesWatcher = mockk()
    private val userSessionRepository = mockk<UserSessionRepository>()

    private val invalidationTracker: RustInvalidationTracker = mockk {
        every { notifyInvalidation(any()) } just Runs
    }

    private val rustMessageQuery = RustMessageQueryImpl(
        userSessionRepository,
        invalidationTracker,
        createRustMessagesWatcher,
        rustMailbox,
        testCoroutineScope
    )

    @Test
    fun `emits initial value from watcher when messages watcher is initialized`() = runTest {
        // Given
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1u)
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        every { messagesWatcher.messages } returns expectedMessages
        coEvery {
            createRustMessagesWatcher.invoke(
                userSession,
                labelId,
                capture(mailboxCallbackSlot)
            )
        } returns messagesWatcher
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs

        // When
        rustMessageQuery.observeMessages(userId, labelId).test {
            // Then
            assertEquals(expectedMessages, awaitItem())
            coVerify { createRustMessagesWatcher(userSession, labelId, mailboxCallbackSlot.captured) }
        }
    }

    @Test
    fun `observing messages with labelId switches mailbox if needed`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LocalLabelId(1u)
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        every { messagesWatcher.messages } returns expectedMessages
        coEvery { createRustMessagesWatcher.invoke(userSession, labelId, any()) } returns messagesWatcher

        // When
        rustMessageQuery.observeMessages(userId, labelId).test {
            val messageList = awaitItem()

            // Then
            assertNotNull(messageList)
            coVerify { rustMailbox.switchToMailbox(userId, labelId) }
        }
    }

    @Test
    fun `new message list is emitted when mailbox live query callback is called`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val labelId = LocalLabelId(1u)
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        every { messagesWatcher.messages } returns emptyList()
        coEvery {
            createRustMessagesWatcher.invoke(
                userSession,
                labelId,
                capture(mailboxCallbackSlot)
            )
        } returns messagesWatcher

        rustMessageQuery.observeMessages(userId, labelId).test {
            skipItems(1) // first emission
            val updatedMessages = expectedMessages.onEach { it.id = LocalMessageId(Math.random().toULong()) }
            every { messagesWatcher.messages } returns updatedMessages
            // When
            mailboxCallbackSlot.captured.onUpdate()

            // Then
            val messageList = awaitItem()

            assertEquals(updatedMessages, messageList)
            verify { invalidationTracker.notifyInvalidation(any()) }
        }
    }
}
