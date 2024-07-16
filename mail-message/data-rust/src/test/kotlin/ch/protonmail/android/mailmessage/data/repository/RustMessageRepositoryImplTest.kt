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

import app.cash.turbine.test
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.local.RustLabelDataSource
import ch.protonmail.android.maillabel.data.usecase.FindLocalLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSource
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMessage
import ch.protonmail.android.mailmessage.data.mapper.toMessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailpagination.domain.model.PageFilter
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.testdata.label.rust.LocalLabelTestData
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RustMessageRepositoryImplTest {

    private val rustMessageDataSource: RustMessageDataSource = mockk()

    // We are unable to mock FindLocalLabelId because of this issue: https://github.com/mockk/mockk/issues/544
    private val systemLabelId = SystemLabelId.Archive.labelId
    private val rustLabelDataSource: RustLabelDataSource = mockk {
        every { observeSystemLabels() } returns flowOf(
            listOf(
                LocalLabelTestData.localSystemLabelWithCount.copy(
                    rid = systemLabelId.id
                )
            )
        )
    }
    private val findLocalLabelId: FindLocalLabelId = FindLocalLabelId(rustLabelDataSource)
    private val repository = RustMessageRepositoryImpl(rustMessageDataSource, findLocalLabelId)

    @Test
    fun `getLocalMessages should return list of messages`() = runTest {
        // Given
        val userId = UserId("userId")
        val pageFilter = PageFilter(labelId = systemLabelId, isSystemFolder = true)
        val pageKey = PageKey(filter = pageFilter)

        val expectedMessages = listOf(
            LocalMessageTestData.AugWeatherForecast,
            LocalMessageTestData.SepWeatherForecast,
            LocalMessageTestData.OctWeatherForecast
        )
        coEvery { rustMessageDataSource.getMessages(any()) } returns expectedMessages

        // When
        val result = repository.getLocalMessages(userId, pageKey)

        // Then
        coVerify { rustMessageDataSource.getMessages(any()) }
        assertEquals(expectedMessages.size, result.size)
        result.forEachIndexed { index, message ->
            assertEquals(expectedMessages[index].subject, message.subject)
        }
    }

    @Test
    fun `observe cached message should return the local message`() = runTest {
        // Given
        val userId = UserId("userId")
        val messageId = MessageId(LocalMessageIdSample.AugWeatherForecast.toString())
        val expectedMessage = LocalMessageTestData.AugWeatherForecast.toMessage()
        coEvery {
            rustMessageDataSource.getMessage(messageId.toLocalMessageId())
        } returns LocalMessageTestData.AugWeatherForecast

        // When
        repository.observeCachedMessage(userId, messageId).test {
            val result = awaitItem().getOrElse { null }

            // Then
            assertEquals(expectedMessage, result)
            coVerify { rustMessageDataSource.getMessage(messageId.toLocalMessageId()) }

            awaitComplete()
        }
    }

    @Test
    fun `observe cached message should return DataError when no message not found`() = runTest {
        // Given
        val userId = UserId("userId")
        val messageId = MessageId(LocalMessageIdSample.AugWeatherForecast.toString())

        coEvery { rustMessageDataSource.getMessage(messageId.toLocalMessageId()) } returns null

        // When
        repository.observeCachedMessage(userId, messageId).test {
            val result = awaitItem()

            // Then
            coVerify { rustMessageDataSource.getMessage(messageId.toLocalMessageId()) }
            assert(result.isLeft())
            assertEquals(DataError.Local.NoDataCached, result.swap().getOrElse { null })
            awaitComplete()
        }
    }

    @Test
    fun `getMessageWithBody should return message with body`() = runTest {
        // Given
        val userId = UserId("userId")
        val messageId = MessageId(LocalMessageIdSample.AugWeatherForecast.toString())
        val localMessage = LocalMessageTestData.AugWeatherForecast
        val localMessageBody = mockk<uniffi.proton_mail_uniffi.DecryptedMessageBody> {
            every { body() } returns "message body"
            every { mimeType() } returns uniffi.proton_api_mail.MimeType.TEXT_PLAIN
        }
        val expectedMessageWithBody = localMessageBody.toMessageBody(messageId)
        coEvery { rustMessageDataSource.getMessage(messageId.toLocalMessageId()) } returns localMessage
        coEvery { rustMessageDataSource.getMessageBody(messageId.toLocalMessageId()) } returns localMessageBody

        // When
        val result = repository.getMessageWithBody(userId, messageId).getOrNull()

        // Then
        assertNotNull(result)
        assertEquals(localMessage.toMessage(), result.message)
        assertEquals(expectedMessageWithBody, result.messageBody)
        coVerify { rustMessageDataSource.getMessage(messageId.toLocalMessageId()) }
        coVerify { rustMessageDataSource.getMessageBody(messageId.toLocalMessageId()) }
    }

}
