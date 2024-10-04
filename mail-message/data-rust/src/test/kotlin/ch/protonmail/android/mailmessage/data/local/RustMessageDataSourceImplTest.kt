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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.usecase.GetRustSenderImage
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesRead
import ch.protonmail.android.mailmessage.data.usecase.RustMarkMessagesUnread
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.Mailbox
import uniffi.proton_mail_uniffi.MailboxException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RustMessageDataSourceImplTest {

    private val userSessionRepository = mockk<UserSessionRepository>()

    private val rustMailbox: RustMailbox = mockk()
    private val rustMessageQuery: RustMessageQuery = mockk()
    private val createRustMessageAccessor = mockk<CreateRustMessageAccessor>()
    private val createRustMessageBodyAccessor = mockk<CreateRustMessageBodyAccessor>()
    private val getRustSenderImage = mockk<GetRustSenderImage>()
    private val rustMarkMessagesRead = mockk<RustMarkMessagesRead>()
    private val rustMarkMessagesUnread = mockk<RustMarkMessagesUnread>()
    private val dataSource = RustMessageDataSourceImpl(
        userSessionRepository,
        rustMailbox,
        rustMessageQuery,
        createRustMessageAccessor,
        createRustMessageBodyAccessor,
        getRustSenderImage,
        rustMarkMessagesRead,
        rustMarkMessagesUnread
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
        every { rustMailbox.observeMailbox() } returns flowOf(mailbox)
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } throws MailboxException.Io("DB Exception")
        // When
        val result = dataSource.getMessageBody(userId, messageId, null)

        // Then
        verify { rustMailbox.observeMailbox() }
        assertNull(result)
    }

    @Test
    fun `get messages should return list of message metadata`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        val pageKey = PageKey()
        val messages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        coEvery { rustMessageQuery.getMessages(userId, pageKey) } returns messages

        // When
        val result = dataSource.getMessages(userId, pageKey)

        // Then
        coVerify { rustMessageQuery.getMessages(userId, pageKey) }
        assertEquals(messages, result)
    }

    @Test
    fun `getSenderImage should return sender image when session is available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val address = "test@example.com"
        val bimi = "bimiSelector"
        val expectedImage = "image.png"

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { getRustSenderImage(mailSession, address, bimi) } returns expectedImage

        // When
        val result = dataSource.getSenderImage(userId, address, bimi)

        // Then
        coVerify { getRustSenderImage(mailSession, address, bimi) }
        assertEquals(expectedImage, result)
    }

    @Test
    fun `getSenderImage should return null when session is not available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val address = "test@example.com"
        val bimi = "bimiSelector"

        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.getSenderImage(userId, address, bimi)

        // Then
        coVerify(exactly = 0) { getRustSenderImage(any(), any(), any()) }
        assertNull(result)
    }

    @Test
    fun `getSenderImage should return null when exception occurs`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val address = "test@example.com"
        val bimi = "bimiSelector"

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery {
            getRustSenderImage(
                mailSession,
                address,
                bimi
            )
        } throws MailSessionException.Other("Some error")

        // When
        val result = dataSource.getSenderImage(userId, address, bimi)

        // Then
        assertNull(result)
    }

    @Test
    fun `should mark messages as read when session and labelId are available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val currentLabelId = LocalLabelId(123uL)
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustMailbox.observeCurrentLabelId() } returns flowOf(currentLabelId)
        coEvery { rustMarkMessagesRead(mailSession, currentLabelId, messageIds) } just Runs

        // When
        val result = dataSource.markRead(userId, messageIds)

        // Then
        assertTrue(result.isRight())
        coVerify { rustMarkMessagesRead(mailSession, currentLabelId, messageIds) }
    }

    @Test
    fun `should not mark messages as read when session is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.markRead(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        coVerify(exactly = 0) { rustMarkMessagesRead(any(), any(), any()) }
    }

    @Test
    fun `should not mark messages as read when labelId is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustMailbox.observeCurrentLabelId() } returns flowOf()

        // When
        val result = dataSource.markRead(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        coVerify(exactly = 0) { rustMarkMessagesRead(mailSession, any(), messageIds) }
    }

    @Test
    fun `should handle exception when marking messages as read`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val currentLabelId = LocalLabelId(123uL)
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } throws MailSessionException.Other("Error")
        coEvery { rustMailbox.observeCurrentLabelId().firstOrNull() } returns currentLabelId

        // When
        val result = dataSource.markRead(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        coVerify(exactly = 0) { rustMarkMessagesRead(mailSession, currentLabelId, messageIds) }
    }

    @Test
    fun `should mark messages as unread when session and labelId are available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val currentLabelId = LocalLabelId(123uL)
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustMailbox.observeCurrentLabelId() } returns flowOf(currentLabelId)
        coEvery { rustMarkMessagesUnread(mailSession, currentLabelId, messageIds) } just Runs

        // When
        val result = dataSource.markUnread(userId, messageIds)

        // Then
        assertTrue(result.isRight())
        coVerify { rustMarkMessagesUnread(mailSession, currentLabelId, messageIds) }
    }

    @Test
    fun `should not mark messages as unread when session is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = dataSource.markUnread(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        coVerify(exactly = 0) { rustMarkMessagesUnread(any(), any(), any()) }
    }

    @Test
    fun `should not mark messages as unread when labelId is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustMailbox.observeCurrentLabelId() } returns flowOf()

        // When
        val result = dataSource.markUnread(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        coVerify(exactly = 0) { rustMarkMessagesUnread(mailSession, any(), messageIds) }
    }

    @Test
    fun `should handle exception when marking messages as unread`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val currentLabelId = LocalLabelId(123uL)
        val messageIds = listOf(LocalMessageId(1uL), LocalMessageId(2uL))

        coEvery { userSessionRepository.getUserSession(userId) } throws MailSessionException.Other("Error")
        coEvery { rustMailbox.observeCurrentLabelId().firstOrNull() } returns currentLabelId

        // When
        val result = dataSource.markUnread(userId, messageIds)

        // Then
        assertTrue(result.isLeft())
        coVerify(exactly = 0) { rustMarkMessagesUnread(mailSession, currentLabelId, messageIds) }
    }


}
