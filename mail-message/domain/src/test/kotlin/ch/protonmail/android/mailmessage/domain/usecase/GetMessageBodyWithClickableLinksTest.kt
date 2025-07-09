package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class GetMessageBodyWithClickableLinksTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val getDecryptedMessageBody = mockk<GetDecryptedMessageBody>()

    private val getMessageBodyWithClikableLinks = GetMessageBodyWithClickableLinks(
        getDecryptedMessageBody = getDecryptedMessageBody
    )

    @Test
    fun `transform plain text links into clickable links`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.AugWeatherForecast
        val transformations = MessageBodyTransformations(
            showQuotedText = false,
            hideEmbeddedImages = false,
            hideRemoteContent = false,
            messageThemeOptions = null
        )
        coEvery { getDecryptedMessageBody(userId, messageId, transformations) } returns
            DecryptedMessageBody(
                messageId = messageId,
                value = testInput.originalBody,
                isUnread = false,
                mimeType = MimeType.Html,
                hasQuotedText = false,
                banners = emptyList()
            ).right()


        // When
        val actual = getMessageBodyWithClikableLinks(userId, messageId, transformations)

        // Then
        assertEquals(testInput.linkifiedBody, actual.getOrNull()?.value, testName)
    }

    companion object {

        private val testInputList = listOf(
            TestInput(
                "Here goes a link www.proton.me",
                "Here goes a link <a href=\"www.proton.me\">www.proton.me</a>"
            ),
            TestInput(
                "Here goes a link https://proton.me",
                "Here goes a link <a href=\"https://proton.me\">https://proton.me</a>"
            ),
            TestInput(
                """
                    |Here goes two links www.proton.me and
                    | www.not-proton.me
                """.trimMargin(),
                """
                    |Here goes two links <a href="www.proton.me">www.proton.me</a> and
                    | <a href="www.not-proton.me">www.not-proton.me</a>
                """.trimMargin()
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return testInputList
                .map { testInput ->
                    val testName = """
                        Linkify body: ${testInput.originalBody}
                    """.trimIndent()
                    arrayOf(testName, testInput)
                }
        }
    }

    data class TestInput(
        val originalBody: String,
        val linkifiedBody: String
    )
}

