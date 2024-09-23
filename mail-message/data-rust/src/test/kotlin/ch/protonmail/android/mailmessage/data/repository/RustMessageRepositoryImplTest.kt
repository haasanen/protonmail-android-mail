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

package ch.protonmail.android.mailmessage.data.repository

import java.io.File
import app.cash.turbine.test
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMimeType
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSource
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMessage
import ch.protonmail.android.mailmessage.data.mapper.toMessageBody
import ch.protonmail.android.mailmessage.data.mapper.toMessageId
import ch.protonmail.android.mailmessage.domain.model.SenderImage
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import uniffi.proton_mail_uniffi.BodyOutput
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RustMessageRepositoryImplTest {

    private val rustMessageDataSource: RustMessageDataSource = mockk()
    private val selectedMailLabelId: SelectedMailLabelId = mockk()

    private val userId = UserId("userId")
    private val repository = RustMessageRepositoryImpl(rustMessageDataSource, selectedMailLabelId)

    @Test
    fun `getLocalMessages should return list of messages`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val pageKey = PageKey(labelId = SystemLabelId.Archive.labelId)

        val expectedMessages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        coEvery { rustMessageDataSource.getMessages(userId, any()) } returns expectedMessages

        // When
        val result = repository.getMessages(userId, pageKey)

        // Then
        coVerify { rustMessageDataSource.getMessages(userId, any()) }
        assertEquals(expectedMessages.size, result.size)
        result.forEachIndexed { index, message ->
            assertEquals(expectedMessages[index].subject, message.subject)
        }
    }

    @Test
    fun `observe cached message should return the local message`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast.toMessageId()
        val expectedMessage = LocalMessageTestData.AugWeatherForecast.toMessage()
        coEvery {
            rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId())
        } returns LocalMessageTestData.AugWeatherForecast

        // When
        repository.observeMessage(userId, messageId).test {
            val result = awaitItem().getOrElse { null }

            // Then
            assertEquals(expectedMessage, result)
            coVerify { rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId()) }

            awaitComplete()
        }
    }

    @Test
    fun `observe cached message should return DataError when no message not found`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast.toMessageId()

        coEvery { rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId()) } returns null

        // When
        repository.observeMessage(userId, messageId).test {
            val result = awaitItem()

            // Then
            coVerify { rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId()) }
            assert(result.isLeft())
            assertEquals(DataError.Local.NoDataCached, result.swap().getOrElse { null })
            awaitComplete()
        }
    }

    @Test
    fun `getMessageWithBody should return message with body`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast.toMessageId()
        val localMessage = LocalMessageTestData.AugWeatherForecast
        val bodyOutput = BodyOutput("message body", false, 0uL, 0uL)
        val localMimeType = LocalMimeType.TEXT_PLAIN
        val localMessageBody = mockk<uniffi.proton_mail_uniffi.DecryptedMessage> {
            coEvery { body(any()) } returns bodyOutput
            every { mimeType() } returns localMimeType
        }
        val expectedMessageWithBody = bodyOutput.toMessageBody(messageId, localMimeType)
        coEvery { rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId()) } returns localMessage
        coEvery {
            rustMessageDataSource.getMessageBody(
                userId,
                messageId.toLocalMessageId(),
                SystemLabelId.Inbox.labelId.toLocalLabelId()
            )
        } returns localMessageBody
        coEvery {
            selectedMailLabelId.flow
        } returns MutableStateFlow(MailLabelId.System(SystemLabelId.Inbox.labelId)).asStateFlow()
        // When
        val result = repository.getMessageWithBody(userId, messageId).getOrNull()

        // Then
        assertNotNull(result)
        assertEquals(localMessage.toMessage(), result.message)
        assertEquals(expectedMessageWithBody, result.messageBody)
        coVerify { rustMessageDataSource.getMessage(userId, messageId.toLocalMessageId()) }
        coVerify {
            rustMessageDataSource.getMessageBody(
                userId,
                messageId.toLocalMessageId(),
                SystemLabelId.Inbox.labelId.toLocalLabelId()
            )
        }
    }

    @Test
    fun `markRead should mark conversations as read`() = runTest {
        // Given
        val messageIds = listOf(LocalMessageIdSample.AugWeatherForecast.toMessageId())
        coEvery { rustMessageDataSource.markRead(userId, any()) } just Runs

        // When
        val result = repository.markRead(userId, messageIds)

        // Then
        coVerify { rustMessageDataSource.markRead(userId, messageIds.map { it.toLocalMessageId() }) }
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun `markUnread should mark conversations as unread`() = runTest {
        // Given
        val messageIds = listOf(LocalMessageIdSample.AugWeatherForecast.toMessageId())
        coEvery { rustMessageDataSource.markUnread(userId, any()) } just Runs

        // When
        val result = repository.markUnread(userId, messageIds)

        // Then
        coVerify { rustMessageDataSource.markUnread(userId, messageIds.map { it.toLocalMessageId() }) }
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun `getSenderImage should return sender image when available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val address = "test@example.com"
        val bimi = "bimiSelector"
        val imagePath = "image.png"
        val expectedSenderImage = SenderImage(File(imagePath))

        coEvery { rustMessageDataSource.getSenderImage(userId, address, bimi) } returns imagePath

        // When
        val result = repository.getSenderImage(userId, address, bimi)

        // Then
        coVerify { rustMessageDataSource.getSenderImage(userId, address, bimi) }
        assertEquals(expectedSenderImage, result)
    }

    @Test
    fun `getSenderImage should return null when sender image is not available`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val address = "test@example.com"
        val bimi = "bimiSelector"

        coEvery { rustMessageDataSource.getSenderImage(userId, address, bimi) } returns null

        // When
        val result = repository.getSenderImage(userId, address, bimi)

        // Then
        coVerify { rustMessageDataSource.getSenderImage(userId, address, bimi) }
        assertNull(result)
    }
}
