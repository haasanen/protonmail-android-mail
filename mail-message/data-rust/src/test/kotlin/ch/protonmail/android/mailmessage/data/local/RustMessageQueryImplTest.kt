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
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import uniffi.proton_mail_uniffi.MailboxMessageLiveQuery
import kotlin.test.Test
import kotlin.test.assertEquals

class RustMessageQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val messages = listOf(
        LocalMessageTestData.AugWeatherForecast,
        LocalMessageTestData.SepWeatherForecast,
        LocalMessageTestData.OctWeatherForecast
    )

    private val mailboxCallbackSlot = slot<MailboxLiveQueryUpdatedCallback>()

    private val messageLiveQuery: MailboxMessageLiveQuery = mockk {
        every { value() } returns messages
    }

    private val mailbox: Mailbox = mockk {
        every { newMessageLiveQuery(any(), capture(mailboxCallbackSlot)) } returns messageLiveQuery
    }

    private val rustMailbox: RustMailbox = mockk {
        every { observeMessageMailbox() } returns flowOf(mailbox)
    }

    private val invalidationTracker: RustInvalidationTracker = mockk {
        every { notifyInvalidation(any()) } just Runs
    }

    private val rustMessageQuery = RustMessageQueryImpl(rustMailbox, invalidationTracker, testCoroutineScope)

    @Test
    fun `query initializes the mailbox and creates live query when created`() = runTest {
        // Given & When the RustMessageQuery is created

        // Then
        verify { rustMailbox.observeMessageMailbox() }
        coVerify { mailbox.newMessageLiveQuery(any(), any()) }
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
        val labelId: LocalLabelId = 1u
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs

        rustMessageQuery.observeMessages(userId, labelId).test {
            // When
            mailboxCallbackSlot.captured.onUpdated()

            // Then
            skipItems(1) // Skip the initial empty list
            val messageList = awaitItem()

            assertEquals(messages, messageList)
            verify { invalidationTracker.notifyInvalidation(any()) }
        }
    }

    @Test
    fun `disconnect nullifies message live query and disconnects it`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId: LocalLabelId = 1u
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        every { messageLiveQuery.disconnect() } just Runs

        // When
        rustMessageQuery.disconnect()
        rustMessageQuery.observeMessages(userId, labelId).test {
            mailboxCallbackSlot.captured.onUpdated()

            // Then
            val messageList = awaitItem()
            coVerify { messageLiveQuery.disconnect() }
            assertEquals(emptyList(), messageList)
        }
    }
}
