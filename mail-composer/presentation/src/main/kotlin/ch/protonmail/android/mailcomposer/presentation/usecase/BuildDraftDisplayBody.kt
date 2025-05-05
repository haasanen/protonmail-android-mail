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

package ch.protonmail.android.mailcomposer.presentation.usecase

import androidx.annotation.VisibleForTesting
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.ui.JAVASCRIPT_CALLBACK_INTERFACE_NAME
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import javax.inject.Inject

@VisibleForTesting
internal const val EDITOR_ID = "EditorId"

class BuildDraftDisplayBody @Inject constructor(private val getCustomCss: GetCustomCss) {

    operator fun invoke(messageBodyWithType: MessageBodyWithType): DraftDisplayBodyUiModel {
        val bodyContent = messageBodyWithType.messageBody
        val css: String = getCustomCss()
        val javascript: String = getJavascript()

        return buildHtmlTemplate(bodyContent, css, javascript)
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

    @Suppress("LongMethod")
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

            let touchStartTime = 0;

            editor.addEventListener('touchstart', (e) => {
                touchStartTime = Date.now();
            });
            
            editor.addEventListener('touchend', (e) => {
                // This bit is required to allow the "native" long press to be triggered (for context menu in Android)
                if (Date.now() - touchStartTime < 500) {
                    updateCaretPosition();
                }
            });

            function updateCaretPosition() {
                var editor = document.getElementById('$EDITOR_ID');
                var selection = window.getSelection();
                if (selection.rangeCount > 0) {
                    var range = selection.getRangeAt(0);

                    // Update the caret position only if the range is collapsed to prevent selection deletion.
                    if (!range.collapsed) {
                        // If the text is selected, we can't modify the DOM.
                        return;
                    }

                    // Create a temporary span element to measure the caret position
                    const span = document.createElement('span');
                    span.textContent = '\u200B'; // Zero-width space character

                    range.insertNode(span);

                    // Get the bounding client rect of the span
                    const rect = span.getBoundingClientRect();
                    
                    // Get the line height of the span
                    const lineHeight = window.getComputedStyle(span).lineHeight;
                    let parsedLineHeight = 16; // Default fallback
                    let parsedLineHeightFactor = 1.2
                    
                    // Check if lineHeight is not 'normal' before parsing
                    if (lineHeight && lineHeight !== 'normal') {
                        const lineHeightValue = lineHeight.replace(/[^\d.]/g, '');
                        // Add another check to ensure parsing is possible
                        if (lineHeightValue) {
                             parsedLineHeight = parseFloat(lineHeightValue) * parsedLineHeightFactor;
                        }
                    } else {
                        // Handle 'normal' line height - still using 1.2 * font-size.
                        const fontSize = window.getComputedStyle(span).fontSize;
                        const fontSizeValue = fontSize.replace(/[^\d.]/g, '');
                         if (fontSizeValue) {
                             parsedLineHeight = parseFloat(fontSizeValue) * parsedLineHeightFactor;
                         }
                    }

                    // Remove the temporary span element using its parent node
                    if (span.parentNode) {
                         span.parentNode.removeChild(span);
                    }

                    // Restore the original selection (caret position)
                    selection.removeAllRanges();
                    selection.addRange(range); // Add the original range back

                    // Calculate the height of the caret position relative to the inputDiv
                    const caretPosition = rect.top - editor.getBoundingClientRect().top;
                    $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onCaretPositionChanged(caretPosition, parsedLineHeight);
                }
            }
        }

        trackCursorPosition();
    """.trimIndent()
}
