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

import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RustMessageDataSourceImplTest {

    private val userSession = mockk<MailUserSession>()
    private val sessionManager: UserSessionRepository = mockk {
        every { observeCurrentUserSession() } returns flowOf(userSession)
    }

    private val rustMailbox: RustMailbox = mockk()
    private val rustMessageQuery: RustMessageQuery = mockk()
    private val dataSource = RustMessageDataSourceImpl(sessionManager, rustMailbox, rustMessageQuery)


    @Test
    fun `get message should return message metadata`() = runTest {
        // Given
        val messageId = LocalMessageIdSample.AugWeatherForecast
        every { userSession.messageMetadata(messageId) } returns LocalMessageTestData.AugWeatherForecast

        // When
        val result = dataSource.getMessage(messageId)

        // Then
        coVerify { sessionManager.observeCurrentUserSession() }
        verify { userSession.messageMetadata(messageId) }
        assertEquals(LocalMessageTestData.AugWeatherForecast, result)
    }

    @Test
    fun `get message should handle session exception`() = runTest {
        // Given
        val messageId = LocalMessageIdSample.AugWeatherForecast
        every { userSession.messageMetadata(messageId) } throws MailSessionException.Db("DB Exception")

        // When
        val result = dataSource.getMessage(messageId)

        // Then
        verify { sessionManager.observeCurrentUserSession() }
        assertNull(result)
    }


    @Test
    fun `get message body should return decrypted message body`() = runTest {
        // Given
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val messageMailbox = mockk<Mailbox> {
            coEvery { messageBody(messageId) } returns mockk()
        }
        every { rustMailbox.observeMessageMailbox() } returns flowOf(messageMailbox)

        // When
        val result = dataSource.getMessageBody(messageId)

        // Then
        verify { rustMailbox.observeMessageMailbox() }
        coVerify { messageMailbox.messageBody(messageId) }
        assert(result != null)
    }


    @Test
    fun `get message body should handle mailbox exception`() = runTest {
        // Given
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val messageMailbox = mockk<Mailbox> {
            coEvery { messageBody(messageId) } throws MailboxException.Db("DB Exception")
        }
        every { rustMailbox.observeMessageMailbox() } returns flowOf(messageMailbox)
        // When
        val result = dataSource.getMessageBody(messageId)

        // Then
        verify { rustMailbox.observeMessageMailbox() }
        assertNull(result)
    }

    @Test
    fun `get messages should return list of message metadata`() = runTest {
        // Given
        val labelId: LocalLabelId = 1uL
        val messages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        coEvery { rustMessageQuery.observeMessages(labelId) } returns flowOf(messages)

        // When
        val result = dataSource.getMessages(labelId)

        // Then
        coVerify { rustMessageQuery.observeMessages(labelId) }
        assertEquals(messages, result)
    }

    @Test
    fun `disconnect should call disconnect on rustMessageQuery`() {
        // Given
        every { rustMessageQuery.disconnect() } returns Unit

        // When
        dataSource.disconnect()

        // Then
        verify { rustMessageQuery.disconnect() }
    }
}
