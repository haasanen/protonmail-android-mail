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
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesWatcher
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
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
import org.junit.Ignore
import org.junit.Rule
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.WatchedMessages
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore("TODO after rust lib bump to 0.11.* completed")
class RustMessageQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)
    private val mailSession = mockk<MailSession>()

    private val expectedMessages = listOf(
        LocalMessageTestData.AugWeatherForecast,
        LocalMessageTestData.SepWeatherForecast,
        LocalMessageTestData.OctWeatherForecast
    )

    private val messagesWatcher: WatchedMessages = mockk {
        every { this@mockk.messages } returns expectedMessages
    }

    private val rustMailbox: RustMailbox = mockk()
    private val createRustMessagesWatcher: CreateRustMessagesWatcher = mockk()
    private val mailSessionRepository: MailSessionRepository = mockk {
        coEvery { this@mockk.getMailSession() } returns mailSession
    }

    private val invalidationTracker: RustInvalidationTracker = mockk {
        every { notifyInvalidation(any()) } just Runs
    }

    private val rustMessageQuery = RustMessageQueryImpl(
        mailSessionRepository,
        invalidationTracker,
        createRustMessagesWatcher,
        testCoroutineScope
    )

    @Test
    fun `query initializes the mailbox and creates live query when created`() = runTest {
        // Given
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val labelId: LocalLabelId = 1u
        coEvery {
            createRustMessagesWatcher.invoke(
                mailSession,
                labelId,
                capture(mailboxCallbackSlot)
            )
        } returns messagesWatcher

        // When
        // TODO once compiling, trigger observing here and rename this test....

        // Then
        coVerify { createRustMessagesWatcher(mailSession, labelId, mailboxCallbackSlot.captured) }
    }

    @Test
    fun `observing messages with labelId switches mailbox if needed`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId: LocalLabelId = 1u
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs

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
        val labelId: LocalLabelId = 1u
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        coEvery {
            createRustMessagesWatcher.invoke(
                mailSession,
                labelId,
                capture(mailboxCallbackSlot)
            )
        } returns messagesWatcher

        rustMessageQuery.observeMessages(userId, labelId).test {
            // When
            mailboxCallbackSlot.captured.onUpdate()

            // Then
            val messageList = awaitItem()

            assertEquals(expectedMessages, messageList)
            verify { invalidationTracker.notifyInvalidation(any()) }
        }
    }

    @Test
    fun `disconnect nullifies message live query and disconnects it`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val labelId: LocalLabelId = 1u
        coEvery {
            createRustMessagesWatcher.invoke(
                mailSession,
                labelId,
                capture(mailboxCallbackSlot)
            )
        } returns messagesWatcher
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        every { messagesWatcher.handle.disconnect() } just Runs

        rustMessageQuery.observeMessages(userId, labelId).test {
            awaitItem()

            // When
            rustMessageQuery.disconnect()
            // Then
            val messageList = awaitItem()
            coVerify { messagesWatcher.handle.disconnect() }
            assertEquals(emptyList(), messageList)
        }
    }
}
