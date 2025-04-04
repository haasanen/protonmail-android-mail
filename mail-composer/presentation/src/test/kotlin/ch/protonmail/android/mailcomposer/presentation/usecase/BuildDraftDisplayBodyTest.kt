package ch.protonmail.android.mailcomposer.presentation.usecase

import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.ui.JAVASCRIPT_CALLBACK_INTERFACE_NAME
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.MimeTypeUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class BuildDraftDisplayBodyTest {

    private val getCustomCss: GetCustomCss = mockk()

    private val buildDraftDisplayBody = BuildDraftDisplayBody(getCustomCss)

    @Test
    fun `returns html template with injected css and javascript`() {
        // Given
        val messageBodyWithType = MessageBodyWithType(messageBody, MimeTypeUiModel.Html)
        every { getCustomCss() } returns rawCustomCss
        val expected = buildHtmlTemplate(messageBody, rawCustomCss, getJavascript())

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
            var body = document.getElementById('$EDITOR_ID').innerHTML
            $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onBodyUpdated(body)
        });

        const observer = new ResizeObserver(entries => {
        for (const entry of entries) {
            $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onWebViewSizeChanged()
        }
        });
        observer.observe(document.querySelector('body'));

        function trackCursorPosition() {
            var editor = document.getElementById('$EDITOR_ID');

            editor.addEventListener('keyup', updateCaretPosition);
            editor.addEventListener('click', updateCaretPosition);
            editor.addEventListener('touchend', updateCaretPosition);

            function updateCaretPosition() {
                var selection = window.getSelection();
                if (selection.rangeCount > 0) {
                    var range = selection.getRangeAt(0);

                    // Create a temporary span element to measure the caret position
                    const span = document.createElement('span');
                    span.textContent = '\u200B'; // Zero-width space character
                    range.insertNode(span);

                    // Get the bounding client rect of the span
                    const rect = span.getBoundingClientRect();
                    // Get the line height of the span
                    const lineHeight = window.getComputedStyle(span).lineHeight;
                    const lineHeightValue = lineHeight.replace(/[^\d.]/g, '');
                    const parsedLineHeight = parseFloat(lineHeightValue) * 1.2;

                    // Remove the temporary span element
                    range.deleteContents();

                    // Calculate the height of the caret position relative to the inputDiv
                    const caretPosition = rect.top - editor.getBoundingClientRect().top;
                    $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onCaretPositionChanged(caretPosition, parsedLineHeight);
                }
            }
        }

        trackCursorPosition();
    """.trimIndent()

    companion object TestData {

        private const val messageBody = "This is the message body sanitized"
        private const val rawCustomCss = "<style> ... </style>"

    }
}
