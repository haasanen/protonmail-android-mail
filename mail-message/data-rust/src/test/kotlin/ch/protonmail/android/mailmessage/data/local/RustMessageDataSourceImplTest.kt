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

import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
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
    private val createRustMessageAccessor = mockk<CreateRustMessageAccessor>()
    private val createRustMessageBodyAccessor = mockk<CreateRustMessageBodyAccessor>()
    private val dataSource = RustMessageDataSourceImpl(
        userSessionRepository,
        rustMailbox,
        rustMessageQuery,
        createRustMessageAccessor,
        createRustMessageBodyAccessor
    )

    @Test
    fun `get message should return message metadata`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val messageId = LocalMessageIdSample.AugWeatherForecast
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { createRustMessageAccessor(mailSession, messageId) } returns LocalMessageTestData.AugWeatherForecast

        // When
        val result = dataSource.getMessage(userId, messageId)

        // Then
        assertEquals(LocalMessageTestData.AugWeatherForecast, result)
    }

    @Test
    fun `get message should handle session exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        val messageId = LocalMessageIdSample.AugWeatherForecast
        coEvery {
            createRustMessageAccessor.invoke(mailSession, messageId)
        } throws MailSessionException.Io("Db failure")

        // When
        val result = dataSource.getMessage(userId, messageId)

        // Then
        assertNull(result)
    }

    @Test
    fun `get message body should return decrypted message body`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val labelId = LocalLabelId(1uL)
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<Mailbox>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        every { rustMailbox.observeMailbox(labelId) } returns flowOf(mailbox)
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } returns mockk()

        // When
        val result = dataSource.getMessageBody(userId, messageId, labelId)

        // Then
        verify { rustMailbox.observeMailbox(labelId) }
        coVerify { createRustMessageBodyAccessor(mailbox, messageId) }
        assert(result != null)
    }

    @Test
    fun `get message body should handle mailbox exception`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<Mailbox>()
        every { rustMailbox.observeMessageMailbox() } returns flowOf(mailbox)
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } throws MailboxException.Io("DB Exception")
        // When
        val result = dataSource.getMessageBody(userId, messageId, null)

        // Then
        verify { rustMailbox.observeMessageMailbox() }
        assertNull(result)
    }

    @Test
    fun `get messages should return list of message metadata`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        val labelId = LocalLabelId(1uL)
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
        val mailSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        every { rustMessageQuery.disconnect() } returns Unit

        // When
        dataSource.disconnect()

        // Then
        verify { rustMessageQuery.disconnect() }
    }
}
