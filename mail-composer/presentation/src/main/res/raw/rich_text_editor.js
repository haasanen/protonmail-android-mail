
/*******************************************************************************
 * Event listeners
 ******************************************************************************/
/* Listen for changes to the body and dispatches them to KT */
document.getElementById('$EDITOR_ID').addEventListener('input', function(){
    var body = document.getElementById('$EDITOR_ID').innerHTML
    $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onBodyUpdated(body)

    requestAnimationFrame(() => updateCaretPosition());
});

/* Listen for changes to the body where images are removed and dispatches them to KT */
const removeInlineImageObserver = new MutationObserver(mutations => {
    mutations.forEach(mutation => {
        if (mutation.type === 'childList') {
            mutation.removedNodes.forEach(node => {
                if (node.nodeName === 'IMG') {
                    const src = node.getAttribute('src');
                    if (src && src.startsWith('cid:')) {
                        const cid = src.substring(4);
                        $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onInlineImageDeleted(cid)
                    }
                }
            });
        }
    });
});
removeInlineImageObserver.observe(document.getElementById('$EDITOR_ID'), {childList: true, subtree: true});

/* Listen for taps on images in the body that contains a "cid" (inline images) and dispatches the event to KT */
document.getElementById('$EDITOR_ID').addEventListener('click', function(event) {
    if (event.target.nodeName === 'IMG') {
        const src = event.target.getAttribute('src');
        if (src && src.startsWith('cid:')) {
            const cid = src.substring(4);
            $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onInlineImageTapped(cid)
        }
    }
});


/* Observes the cursor position and notifies kotlin through js interface. Invoked at script init (bottom of this file).*/
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
}
trackCursorPosition();

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
        const density = window.devicePixelRatio || 1.0;

        // Calculate the height of the caret position relative to the inputDiv
        const caretPosition = rect.top - editor.getBoundingClientRect().top;
        $JAVASCRIPT_CALLBACK_INTERFACE_NAME.onCaretPositionChanged(
                 caretPosition * density,
                 parsedLineHeight * density);

    }
}

/*******************************************************************************
 * Public functions invoked by kotlin through webview evaluate javascript method
 ******************************************************************************/

function focusEditor() {
    var editor = document.getElementById('$EDITOR_ID');
    editor.focus();
}

function injectInlineImage(contentId) {
    var editor = document.getElementById('$EDITOR_ID');

    editor.focus();

    var selection = window.getSelection();
    if (selection.rangeCount > 0) {
        var range = selection.getRangeAt(0);

        const img = document.createElement('img');
        img.src = "cid:" + contentId;
        img.style = "max-width: 100%;";
        range.insertNode(img);
        range.setStartAfter(img);
        range.collapse(true);

        // Insert a blank line after the image
        const br = document.createElement('br');
        const br1 = document.createElement('br');
        range.insertNode(br1);
        range.insertNode(br);

        // Move the cursor after the <br>
        range.setStartAfter(br1);
        range.collapse(true);

        selection.removeAllRanges();
        selection.addRange(range);
    }
    // Dispatch an input updated event to ensure body is saved
    editor.dispatchEvent(new Event('input'));
}

function stripInlineImage(contentId) {
    // Disable remove image observer as we don't want this strip to trigger a delete
    removeInlineImageObserver.disconnect();

    var editor = document.getElementById('$EDITOR_ID');
    const exactCidPattern = 'cid:' + contentId + '(?![0-9a-zA-Z])';
    const cidMatcher = new RegExp(exactCidPattern);
    const images = editor.getElementsByTagName('img');

    for (const img of images) {
        console.log("Checking image..." + img.src)
        // Check src attribute for a match
        if (cidMatcher.test(img.src)) {
            console.log("Image was actually matched and removed")
            img.remove();
            break;
        }
    }
    // Dispatch an input updated event to ensure body is saved
    editor.dispatchEvent(new Event('input'));
    // Re-enable remove image observer to react to DOM events again
    removeInlineImageObserver.observe(document.getElementById('$EDITOR_ID'), {childList: true, subtree: true});
}


/*******************************************************************************
 * This function compensates for the visual viewport’s vertical offset
 * by applying CSS padding.
 *
 * Problem: unreachable top content due to the visual viewport having top offset. This happens
 * when we copy-paste some text into the editor and then scroll up and down. It's observed randomly.
 * There is no clear pattern to it.
 *
 * How it works:
 *
 * Read `window.visualViewport.offsetTop` and apply it as a
 * `padding-top` via a CSS custom property (`--vv-top-inset`).
 * This visually shifts the page content down so that the real top text becomes
 * visible again inside the viewport. Please ensure the CSS custom property is defined.
 ******************************************************************************/
function compensateVisualViewportOffset() {
    // Visual viewport is not supported on this browser; nothing to fix.
    if (!window.visualViewport) return;

    let lastPadding = 0;
    const MIN_OFFSET_CHANGE_PX = 2;

    function applyOffsetCompensation() {
        const topCssPadding = Math.round(window.visualViewport.offsetTop || 0);

        // Update only if the value changed meaningfully
        if (Math.abs(topCssPadding - lastPadding) > MIN_OFFSET_CHANGE_PX) {
            document.documentElement.style.setProperty('--vv-top-inset', topCssPadding + 'px');
            document.body.style.paddingTop = 'var(--vv-top-inset)';
            lastPadding = topCssPadding;
        }
    }

    // Keep padding in sync with viewport movements/resizes
    window.visualViewport.addEventListener('scroll', applyOffsetCompensation, { passive: true });
    window.visualViewport.addEventListener('resize', applyOffsetCompensation, { passive: true });

    // Initial compensation
    applyOffsetCompensation();
}
compensateVisualViewportOffset();