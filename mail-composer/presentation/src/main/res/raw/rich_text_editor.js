
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

function focusEditor() {
    var editor = document.getElementById('$EDITOR_ID');
    editor.focus();
}

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

