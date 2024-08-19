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
import ch.protonmail.android.mailmessage.data.model.LocalConversationMessages
import ch.protonmail.android.mailcommon.domain.mapper.LocalConversationId
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RustMessageDataSourceImplTest {

    private val userSessionRepository = mockk<UserSessionRepository>()

    private val rustMailbox: RustMailbox = mockk()
    private val rustMessageQuery: RustMessageQuery = mockk()
    private val rustConversationMessageQuery: RustConversationMessageQuery = mockk()
    private val dataSource = RustMessageDataSourceImpl(
        userSessionRepository,
        rustMailbox, rustMessageQuery, rustConversationMessageQuery
    )

    @Test
    fun `get message should return message metadata`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val userSession = mockk<MailUserSession>()
        val messageId = LocalMessageIdSample.AugWeatherForecast
        every { userSession.messageMetadata(messageId) } returns LocalMessageTestData.AugWeatherForecast
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession

        // When
        val result = dataSource.getMessage(userId, messageId)

        // Then
        coVerify { userSessionRepository.getUserSession(userId) }
        verify { userSession.messageMetadata(messageId) }
        assertEquals(LocalMessageTestData.AugWeatherForecast, result)
    }

    @Test
    fun `get message should handle session exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        val messageId = LocalMessageIdSample.AugWeatherForecast
        every { userSession.messageMetadata(messageId) } throws MailSessionException.Db("DB Exception")

        // When
        val result = dataSource.getMessage(userId, messageId)

        // Then
        coVerify { userSessionRepository.getUserSession(userId) }
        assertNull(result)
    }

    @Test
    fun `get message body should return decrypted message body`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val messageMailbox = mockk<Mailbox> {
            coEvery { messageBody(messageId) } returns mockk()
        }
        every { rustMailbox.observeMessageMailbox() } returns flowOf(messageMailbox)

        // When
        val result = dataSource.getMessageBody(userId, messageId)

        // Then
        verify { rustMailbox.observeMessageMailbox() }
        coVerify { messageMailbox.messageBody(messageId) }
        assert(result != null)
    }

    @Test
    fun `get message body should handle mailbox exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val messageMailbox = mockk<Mailbox> {
            coEvery { messageBody(messageId) } throws MailboxException.Db("DB Exception")
        }
        every { rustMailbox.observeMessageMailbox() } returns flowOf(messageMailbox)
        // When
        val result = dataSource.getMessageBody(userId, messageId)

        // Then
        verify { rustMailbox.observeMessageMailbox() }
        assertNull(result)
    }

    @Test
    fun `get messages should return list of message metadata`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        val labelId: LocalLabelId = 1uL
        val messages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        coEvery { rustMessageQuery.observeMessages(userId, labelId) } returns flowOf(messages)

        // When
        val result = dataSource.getMessages(userId, labelId)

        // Then
        coVerify { rustMessageQuery.observeMessages(userId, labelId) }
        assertEquals(messages, result)
    }

    @Test
    fun `disconnect should call disconnect on rustMessageQuery`() {
        // Given
        val userId = UserIdTestData.userId
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        every { rustMessageQuery.disconnect() } returns Unit

        // When
        dataSource.disconnect()

        // Then
        verify { rustMessageQuery.disconnect() }
    }

    @Test
    fun `observeConversationMessages should return conversation messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val conversationId: LocalConversationId = 1uL
        val messages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        val localConversationMessages = LocalConversationMessages(
            messageIdToOpen = LocalMessageIdSample.AugWeatherForecast,
            messages = messages
        )
        coEvery {
            rustConversationMessageQuery.observeConversationMessages(
                userId, conversationId
            )
        } returns flowOf(localConversationMessages)

        // When
        dataSource.observeConversationMessages(userId, conversationId).test {

            // Then
            // skipItems(1) // skip empty list
            val result = awaitItem()
            assertEquals(localConversationMessages, result)
            coVerify { rustConversationMessageQuery.observeConversationMessages(userId, conversationId) }

            awaitComplete()
        }
    }

}
