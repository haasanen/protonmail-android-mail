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

package ch.protonmail.android.maildetail.domain.usecase

import android.net.Uri
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maildetail.domain.model.OpenAttachmentIntentValues
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.DecryptedAttachment
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageWithBody
import ch.protonmail.android.mailmessage.domain.repository.AttachmentRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.message.MessageBodyTestData
import ch.protonmail.android.testdata.message.MessageTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAttachmentIntentValuesTest {

    private val userId = UserIdSample.Primary
    private val messageId = MessageIdSample.Invoice
    private val attachmentId = AttachmentId("1")

    private val uri = mockk<Uri>()

    private val decryptedAttachment by lazy {
        DecryptedAttachment(
            metadata = AttachmentMetadataSamples.Pdf,
            fileUri = uri
        )
    }

    private val messageWithBody = MessageWithBody(
        message = MessageTestData.messageWithAttachments,
        messageBody = MessageBodyTestData.messageBody
    )

    private val attachmentRepository = mockk<AttachmentRepository>()
    private val messageRepository = mockk<MessageRepository>()

    private val getAttachmentIntentValues =
        GetAttachmentIntentValues(attachmentRepository, messageRepository)

    @Test
    fun `should return intent values when attachment and metadata is locally available`() = runTest {
        // Given
        val id = AttachmentId("6")
        coEvery {
            attachmentRepository.getAttachment(
                userId = userId,
                messageId = messageId,
                attachmentId = id
            )
        } returns decryptedAttachment.right()
        coEvery {
            messageRepository.getMessageWithBody(
                userId,
                messageId,
                MessageBodyTransformations.AttachmentDefaults
            )
        } returns messageWithBody.right()

        // When
        val result = getAttachmentIntentValues(userId, messageId, id)

        // Then
        assertEquals(OpenAttachmentIntentValues("application/pdf", uri).right(), result)
    }

    @Test
    fun `should return no data cached when attachment is not locally available`() = runTest {
        // Given
        coEvery {
            attachmentRepository.getAttachment(
                userId = userId,
                messageId = messageId,
                attachmentId = attachmentId
            )
        } returns DataError.Local.NoDataCached.left()
        coEvery {
            messageRepository.getMessageWithBody(
                userId,
                messageId,
                MessageBodyTransformations.AttachmentDefaults
            )
        } returns messageWithBody.right()

        // When
        val result = getAttachmentIntentValues(userId, messageId, attachmentId)

        // Then
        assertTrue(result.isLeft())
        assertEquals(DataError.Local.NoDataCached.left(), result)
    }

    @Test
    fun `should return no data cached when message is not locally available`() = runTest {
        // Given
        coEvery {
            messageRepository.getMessageWithBody(
                userId,
                messageId,
                MessageBodyTransformations.AttachmentDefaults
            )
        } returns DataError.Local.NoDataCached.left()

        // When
        val result = getAttachmentIntentValues(userId, messageId, attachmentId)

        // Then
        assertTrue(result.isLeft())
        assertEquals(DataError.Local.NoDataCached.left(), result)
    }

    @Test
    fun `should return attachment repository error when getting attachment fails`() = runTest {
        // Given
        coEvery {
            attachmentRepository.getAttachment(
                userId = userId,
                messageId = messageId,
                attachmentId = attachmentId
            )
        } returns DataError.Local.OutOfMemory.left()
        coEvery {
            messageRepository.getMessageWithBody(
                userId,
                messageId,
                MessageBodyTransformations.AttachmentDefaults
            )
        } returns messageWithBody.right()

        // When
        val result = getAttachmentIntentValues(userId, messageId, attachmentId)

        // Then
        assertEquals(DataError.Local.OutOfMemory.left(), result)
    }
}

