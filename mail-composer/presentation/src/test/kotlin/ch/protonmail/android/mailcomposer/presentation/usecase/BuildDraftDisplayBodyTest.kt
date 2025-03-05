package ch.protonmail.android.mailcomposer.presentation.usecase

import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.ui.JAVASCRIPT_CALLBACK_INTERFACE_NAME
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.MimeTypeUiModel
import ch.protonmail.android.mailmessage.presentation.usecase.SanitizeHtmlOfDecryptedMessageBody
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class BuildDraftDisplayBodyTest {

    private val getCustomCss: GetCustomCss = mockk()
    private val sanitizeHtmlOfDecryptedMessageBody: SanitizeHtmlOfDecryptedMessageBody = mockk()

    private val buildDraftDisplayBody = BuildDraftDisplayBody(
        getCustomCss,
        sanitizeHtmlOfDecryptedMessageBody
    )

    @Test
    fun `returns html template with injected css and javascript`() {
        // Given
        val messageBodyWithType = MessageBodyWithType(rawMessageBody, MimeTypeUiModel.Html)
        every { sanitizeHtmlOfDecryptedMessageBody(messageBodyWithType) } returns sanitizedMessageBody
        every { getCustomCss() } returns rawCustomCss
        val expected = buildHtmlTemplate(sanitizedMessageBody, rawCustomCss, getJavascript())

        // When
        val actual = buildDraftDisplayBody(messageBodyWithType)

        // Then
        assertEquals(expected, actual)
    }

    private fun buildHtmlTemplate(
        bodyContent: String,
        customCss: String,
        javascript: String
    ): DraftDisplayBodyUiModel {
        val html = """
            <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>Proton HTML Editor</title>
                    <meta id="myViewport" name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, shrink-to-fit=yes">
                    <meta id="myCSP" http-equiv="Content-Security-Policy" content="script-src 'unsafe-inline' 'unsafe-eval'">
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                    <style>
                        $customCss
                    </style>
                </head>
                <body>
                    <div id="editor_header"></div>
                    <div role="textbox" aria-multiline="true" id="$EDITOR_ID" contentEditable="true" placeholder="" class="placeholder">
                        $bodyContent
                    </div>
                    <div id="editor_footer"></div>

                    <script>
                        $javascript
                    </script>
                </body>
                </html>
        """.trimIndent()

        return DraftDisplayBodyUiModel(html)
    }

    private fun getJavascript() = """
        document.getElementById('$EDITOR_ID').addEventListener('input', function(){
            console.log("composer-js-editor: on body changed event")
            var body = document.getElementById('$EDITOR_ID').innerHTML
            $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onBodyUpdated(body)
        });
    """.trimIndent()

    companion object TestData {
        private const val rawMessageBody = "This is the message body unsanitized <script>...</script>"
        private const val sanitizedMessageBody = "This is the message body sanitized"
        private const val rawCustomCss = "<style> ... </style>"

    }
}
