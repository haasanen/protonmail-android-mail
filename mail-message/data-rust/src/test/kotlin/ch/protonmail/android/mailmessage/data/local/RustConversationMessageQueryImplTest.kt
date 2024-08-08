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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_common.LocalConversationId
import uniffi.proton_mail_uniffi.ConversationMessagesLiveQueryResult
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class RustConversationMessageQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val messages = listOf(
        LocalMessageTestData.AugWeatherForecast,
        LocalMessageTestData.SepWeatherForecast,
        LocalMessageTestData.OctWeatherForecast
    )

    private val conversationMessagesLiveQueryResult: ConversationMessagesLiveQueryResult = mockk {
        every { query.value() } returns messages
        every { messageIdToOpen } returns 1uL
    }

    private val mailboxCallbackSlot = slot<MailboxLiveQueryUpdatedCallback>()

    private val mailbox: Mailbox = mockk {
        coEvery {
            newConversationMessagesLiveQuery(
                any(),
                capture(mailboxCallbackSlot)
            )
        } returns conversationMessagesLiveQueryResult
    }

    private val rustMailbox: RustMailbox = mockk {
        every { observeConversationMailbox() } returns flowOf(mailbox)
    }

    private val invalidationTracker: RustInvalidationTracker = mockk {
        every { notifyInvalidation(any()) } just Runs
    }

    private val rustConversationMessageQuery = RustConversationMessageQueryImpl(
        rustMailbox, invalidationTracker, testCoroutineScope
    )

    @Test
    fun `init initializes the mailbox and creates live query when called`() = runTest {
        // Given
        val conversationId: LocalConversationId = 1uL

        // When
        rustConversationMessageQuery.observeConversationMessages(UserIdTestData.userId, conversationId).test {

            // Then
            skipItems(1)
            verify { rustMailbox.observeConversationMailbox() }
            coVerify { mailbox.newConversationMessagesLiveQuery(conversationId, any()) }
        }
    }

    @Test
    fun `observeConversationMessages emits new message list when callback is called`() = runTest {
        // Given
        val conversationId: LocalConversationId = 1uL

        // When
        rustConversationMessageQuery.observeConversationMessages(UserIdTestData.userId, conversationId).test {
            // Simulate callback
            mailboxCallbackSlot.captured.onUpdated()

            // Then
            val result = awaitItem()
            assertEquals(messages, result)
            verify { invalidationTracker.notifyInvalidation(any()) }
        }
    }

}
