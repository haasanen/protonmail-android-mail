package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
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

    private val getMessageBodyWithClickableLinks = GetMessageBodyWithClickableLinks(
        getDecryptedMessageBody = getDecryptedMessageBody,
        isLinkifyUrlEnabled = mockk<FeatureFlag<Boolean>> {
            coEvery { this@mockk.get() } returns true
        }
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
                hasCalendarInvite = false,
                banners = emptyList()
            ).right()


        // When
        val actual = getMessageBodyWithClickableLinks(userId, messageId, transformations)

        // Then
        assertEquals(testInput.linkifiedBody, actual.getOrNull()?.value, testName)
    }

    companion object {

        private val expectedHitList = listOf(
            TestInput(
                "Here goes a link www.proton.me",
                """Here goes a link <a href="https://www.proton.me">www.proton.me</a>"""
            ),
            TestInput(
                "Here goes a link https://proton.me",
                """Here goes a link <a href="https://proton.me">https://proton.me</a>"""
            ),
            TestInput(
                "Url www.proton.me with following text that is not.a.url",
                """Url <a href="https://www.proton.me">www.proton.me</a> with following text that is not.a.url"""
            ),
            TestInput(
                """
                    |Two links https://proton.me and
                    |www.not-proton.me
                """.trimMargin(),
                """
                    |Two links <a href="https://proton.me">https://proton.me</a> and
                    |<a href="https://www.not-proton.me">www.not-proton.me</a>
                """.trimMargin()
            ),
            TestInput(
                """
                    |One link already clickable, one not
                    |<a href=https://www.proton.me">www.proton.me</a> and
                    |https://another.me
                """.trimMargin(),
                """
                    |One link already clickable, one not
                    |<a href=https://www.proton.me">www.proton.me</a> and
                    |<a href="https://another.me">https://another.me</a>
                """.trimMargin()
            ),
            TestInput(
                """
                    |<pre>https://hello.com<br><br>www.proton.me<br><br>
                    |<br><br>Signature<br><br>
                """.trimMargin(),
                """
                    |<pre><a href="https://hello.com">https://hello.com</a><br><br><a href="https://www.proton.me">www.proton.me</a><br><br>
                    |<br><br>Signature<br><br>
                """.trimMargin()
            ),
            TestInput(
                """
                    |<body>https://hello.com</body>
                """.trimMargin(),
                """
                    |<body><a href="https://hello.com">https://hello.com</a></body>
                """.trimMargin()
            ),
            TestInput(
                """
                    |<div>https://hello.com</div>
                """.trimMargin(),
                """
                    |<div><a href="https://hello.com">https://hello.com</a></div>
                """.trimMargin()
            ),
            TestInput(
                """
                    |<span>https://hello.com</span>
                """.trimMargin(),
                """
                    |<span><a href="https://hello.com">https://hello.com</a></span>
                """.trimMargin()
            ),
            TestInput(
                """
                    |Link 1 www.proton.me and link 2 <a href="https://mail.proton.me">mail.proton.me</a>
                """.trimMargin(),
                """
                    |Link 1 <a href="https://www.proton.me">www.proton.me</a> and link 2 <a href="https://mail.proton.me">mail.proton.me</a>
                """.trimMargin()
            ),
            TestInput(
                """
                    |https://www.proton.me/test/<br><br><div class="protonmail_signature_block-user"><br></div><br><br>Sent from <a target="_blank" href="https://proton.me/mail/home" rel="noreferrer">Proton Mail</a> for Android.
                """.trimMargin(),
                """
                    |<a href="https://www.proton.me/test/">https://www.proton.me/test/</a><br><br><div class="protonmail_signature_block-user"><br></div><br><br>Sent from <a target="_blank" href="https://proton.me/mail/home" rel="noreferrer">Proton Mail</a> for Android.
                """.trimMargin()
            )
        )

        private val expectedMissList = listOf(
            TestInput(
                "Here goes proton.me",
                "Here goes proton.me"
            ),
            TestInput(
                "Here goes ww.proton.me",
                "Here goes ww.proton.me"
            ),
            TestInput(
                """
                    |xmlns="http://www.w3.org/1999/xhtml"
                """.trimMargin(),
                """
                    |xmlns="http://www.w3.org/1999/xhtml"
                """.trimMargin()
            ),
            TestInput(
                """
                    |xmlns="https://www.w3.org/1999/xhtml"
                """.trimMargin(),
                """
                    |xmlns="https://www.w3.org/1999/xhtml"
                """.trimMargin()
            ),
            TestInput(
                """
                    |@import url(https://media-cdn.style.css)
                """.trimMargin(),
                """
                    |@import url(https://media-cdn.style.css)
                """.trimMargin()
            ),
            TestInput(
                """
                    |src="https://website.com/www.why.com/senp.png;
                """.trimMargin(),
                """
                    |src="https://website.com/www.why.com/senp.png;
                """.trimMargin()
            ),
            TestInput(
                """
                    |src=" https://website.com/www.why.com/senp.png;
                """.trimMargin(),
                """
                    |src=" https://website.com/www.why.com/senp.png;
                """.trimMargin()
            )
        )

        private val testInputList = expectedHitList + expectedMissList

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

