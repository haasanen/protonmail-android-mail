package ch.protonmail.android.mailcomposer.presentation.usecase

import ch.protonmail.android.mailcomposer.domain.model.CspNonce
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.usecase.GenerateCspNonce
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class BuildDraftDisplayBodyTest {

    private val getCustomCss: GetCustomCss = mockk()
    private val getCustomJs: GetCustomJs = mockk()
    private val generateCspNonce: GenerateCspNonce = mockk()

    private val buildDraftDisplayBody = BuildDraftDisplayBody(getCustomCss, getCustomJs, generateCspNonce)

    @Test
    fun `returns html template with injected css and javascript`() {
        // Given
        val messageBodyWithType = DraftBody(messageBody)
        every { getCustomCss() } returns rawCustomCss
        every { getCustomJs() } returns rawCustomJs
        every { generateCspNonce() } returns CspNonce(rawCspNonce)
        val expected = buildHtmlTemplate(messageBody, rawCustomCss, rawCustomJs, rawCspNonce)

        // When
        val actual = buildDraftDisplayBody(messageBodyWithType)

        // Then
        assertEquals(expected, actual)
    }

    private fun buildHtmlTemplate(
        bodyContent: String,
        customCss: String,
        javascript: String,
        cspNonce: String
    ): DraftDisplayBodyUiModel {
        val html = """
            <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>Proton HTML Editor</title>
                    <meta id="myViewport" name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, shrink-to-fit=yes">
                    <meta id="myCSP" http-equiv="Content-Security-Policy" content="script-src 'nonce-$cspNonce'">
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

                    <script nonce="$cspNonce">
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
        private const val rawCspNonce = "2HDB6SOkOQpIn8C8LmPhOGfl3eHwCFVpqlNTK/DrOig="

    }
}
