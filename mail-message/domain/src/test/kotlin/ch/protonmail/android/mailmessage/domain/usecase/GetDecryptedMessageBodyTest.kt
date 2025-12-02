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
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.repository.RsvpEventRepository
import ch.protonmail.android.testdata.message.MessageBodyTestData
import ch.protonmail.android.testdata.message.MessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class GetDecryptedMessageBodyTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val messageId = MessageId("messageId")
    private val messageRepository = mockk<MessageRepository>()
    private val messageBodyRepository = mockk<MessageBodyRepository>()
    private val injectViewPortMetaTagIntoMessageBody = mockk<InjectViewPortMetaTagIntoMessageBody>()
    private val injectFixedHeightCss = mockk<InjectFixedHeightCss>()
    private val rsvpEventRepository = mockk<RsvpEventRepository>()

    private val isCssInjectionEnabled = mockk<FeatureFlag<Boolean>>()

    private val getDecryptedMessageBody = GetDecryptedMessageBody(
        messageRepository,
        messageBodyRepository,
        rsvpEventRepository,
        injectViewPortMetaTagIntoMessageBody,
        injectFixedHeightCss,
        isCssInjectionEnabled
    )

    @Test
    fun `when repository gets the decrypted message body successfully then it is returned`() = runTest {
        // Given
        val transformations = MessageBodyTransformations(
            showQuotedText = false,
            hideEmbeddedImages = false,
            hideRemoteContent = false,
            messageThemeOptions = null
        )
        val expected = DecryptedMessageBody(
            messageId = messageId,
            value = testInput.messageBody.body,
            mimeType = testInput.messageBody.mimeType,
            hasQuotedText = false,
            hasCalendarInvite = true,
            isUnread = false,
            banners = emptyList(),
            attachments = testInput.messageBody.attachments,
            transformations = transformations
        ).right()
        coEvery {
            messageRepository.getMessage(
                UserIdTestData.userId,
                messageId
            )
        } returns testInput.message.right()
        coEvery {
            messageBodyRepository.getMessageBody(
                UserIdTestData.userId,
                messageId,
                MessageBodyTransformations.MessageDetailsDefaults
            )
        } returns testInput.messageBody.right()
        every {
            injectViewPortMetaTagIntoMessageBody(testInput.messageBody.body)
        } returns testInput.messageBody.body

        coEvery { isCssInjectionEnabled.get() } returns testInput.shouldInjectCss
        coEvery {
            injectFixedHeightCss(testInput.messageBody.body)
        } returns testInput.messageBody.body
        coEvery { rsvpEventRepository.identifyRsvp(UserIdTestData.userId, messageId) } returns true.right()

        // When
        val actual = getDecryptedMessageBody(UserIdTestData.userId, messageId)

        // Then
        assertEquals(expected, actual, testName)

        if (testInput.shouldInjectCss) {
            verify(exactly = 1) { injectFixedHeightCss(testInput.messageBody.body) }
        } else {
            verify { injectFixedHeightCss wasNot called }
        }
    }

    @Test
    fun `when message repository returns an error then the use case returns the error`() = runTest {
        // Given
        val expected = GetMessageBodyError.Data(DataError.Local.NoDataCached).left()
        coEvery {
            messageRepository.getMessage(
                UserIdTestData.userId,
                messageId
            )
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = getDecryptedMessageBody(UserIdTestData.userId, messageId)

        // Then
        assertEquals(expected, actual, testName)
    }

    @Test
    fun `when body repository returns an error then the use case returns the error`() = runTest {
        // Given
        val expected = GetMessageBodyError.Data(DataError.Local.NoDataCached).left()
        coEvery {
            messageRepository.getMessage(
                UserIdTestData.userId,
                messageId
            )
        } returns testInput.message.right()
        coEvery {
            messageBodyRepository.getMessageBody(
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
                MessageTestData.message,
                MessageBodyTestData.htmlMessageBody,
                shouldInjectCss = true
            ),
            TestInput(
                MimeType.Html,
                MessageTestData.message,
                MessageBodyTestData.htmlMessageBody,
                shouldInjectCss = false
            ),
            TestInput(
                MimeType.MultipartMixed,
                MessageTestData.message,
                MessageBodyTestData.multipartMixedMessageBody,
                shouldInjectCss = true
            ),
            TestInput(
                MimeType.MultipartMixed,
                MessageTestData.message,
                MessageBodyTestData.multipartMixedMessageBody,
                shouldInjectCss = false
            ),
            TestInput(
                MimeType.Html,
                MessageTestData.message,
                MessageBodyTestData.attachmentsBody,
                shouldInjectCss = false
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return testInputList
                .map { testInput ->
                    val testName = """
                        Message type: ${testInput.mimeType},
                        Inject CSS: ${testInput.shouldInjectCss}
                    """.trimIndent()
                    arrayOf(testName, testInput)
                }
        }
    }

    data class TestInput(
        val mimeType: MimeType,
        val message: Message,
        val messageBody: MessageBody,
        val shouldInjectCss: Boolean
    )
}
