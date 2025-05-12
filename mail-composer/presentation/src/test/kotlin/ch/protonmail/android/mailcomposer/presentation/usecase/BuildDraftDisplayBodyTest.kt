package ch.protonmail.android.mailcomposer.presentation.usecase

import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.MimeTypeUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class BuildDraftDisplayBodyTest {

    private val getCustomCss: GetCustomCss = mockk()
    private val getCustomJs: GetCustomJs = mockk()

    private val buildDraftDisplayBody = BuildDraftDisplayBody(getCustomCss, getCustomJs)

    @Test
    fun `returns html template with injected css and javascript`() {
        // Given
        val messageBodyWithType = MessageBodyWithType(messageBody, MimeTypeUiModel.Html)
        every { getCustomCss() } returns rawCustomCss
        every { getCustomJs() } returns rawCustomJs
        val expected = buildHtmlTemplate(messageBody, rawCustomCss, rawCustomJs)

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


    companion object TestData {

        private const val messageBody = "This is the message body sanitized"
        private const val rawCustomCss = "<style> ... </style>"
        private const val rawCustomJs = "<script> ... </script>"

    }
}
