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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetDecryptedMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageWithBody
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.testdata.message.MessageBodyTestData
import ch.protonmail.android.testdata.message.MessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class GetDecryptedMessageBodyTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val messageId = MessageId("messageId")
    private val messageRepository = mockk<MessageRepository>()

    private val getDecryptedMessageBody = GetDecryptedMessageBody(
        messageRepository
    )

    @Test
    fun `when repository gets message body and decryption is successful then the decrypted message body is returned`() =
        runTest {
            // Given
            val transformations = MessageBodyTransformations(
                showQuotedText = false,
                hideEmbeddedImages = false,
                hideRemoteContent = false,
                messageThemeOptions = null
            )
            val expected = DecryptedMessageBody(
                messageId = messageId,
                value = testInput.messageWithBody.messageBody.body,
                mimeType = testInput.messageWithBody.messageBody.mimeType,
                hasQuotedText = false,
                isUnread = false,
                banners = emptyList(),
                attachments = testInput.messageWithBody.message.attachments,
                transformations = transformations
            ).right()
            coEvery {
                messageRepository.getMessageWithBody(
                    UserIdTestData.userId,
                    messageId,
                    MessageBodyTransformations.MessageDetailsDefaults
                )
            } returns testInput.messageWithBody.right()
            coEvery {
                messageRepository.getMessageWithBody(
                    UserIdTestData.userId,
                    messageId,
                    MessageBodyTransformations.AttachmentDefaults
                )
            } returns testInput.messageWithBody.right()

            // When
            val actual = getDecryptedMessageBody(UserIdTestData.userId, messageId)

            // Then
            assertEquals(expected, actual, testName)
        }

    @Test
    fun `when repository method returns an error then the use case returns the error`() = runTest {
        // Given
        val expected = GetDecryptedMessageBodyError.Data(DataError.Local.NoDataCached).left()
        coEvery {
            messageRepository.getMessageWithBody(
                UserIdTestData.userId,
                messageId,
                MessageBodyTransformations.MessageDetailsDefaults
            )
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = getDecryptedMessageBody(UserIdTestData.userId, messageId)

        // Then
        assertEquals(expected, actual, testName)
    }

    companion object {

        private val testInputList = listOf(
            TestInput(
                MimeType.Html,
                MessageWithBody(MessageTestData.message, MessageBodyTestData.htmlMessageBody)
            ),
            TestInput(
                MimeType.MultipartMixed,
                MessageWithBody(MessageTestData.message, MessageBodyTestData.multipartMixedMessageBody)
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return testInputList
                .map { testInput ->
                    val testName = """
                        Message type: ${testInput.mimeType}
                    """.trimIndent()
                    arrayOf(testName, testInput)
                }
        }
    }

    data class TestInput(
        val mimeType: MimeType,
        val messageWithBody: MessageWithBody
    )
}
