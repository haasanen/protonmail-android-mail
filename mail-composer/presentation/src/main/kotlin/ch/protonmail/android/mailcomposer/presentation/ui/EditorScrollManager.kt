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

package ch.protonmail.android.mailcomposer.presentation.ui

import ch.protonmail.android.mailcomposer.presentation.model.editor.ComposeScreenMeasures
import ch.protonmail.android.mailcomposer.presentation.model.editor.WebViewMeasures
import ch.protonmail.android.mailcomposer.presentation.model.editor.centerPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@OptIn(FlowPreview::class)
class EditorScrollManager(
    private val scope: CoroutineScope,
    private val onUpdateScroll: (Float) -> Unit
) {

    private var previousWebViewHeightPx = 0f

    private data class EditorMeasures(
        val screenMeasures: ComposeScreenMeasures,
        val webViewMeasures: WebViewMeasures
    )

    private data class EditorViewport(
        val topPx: Float,
        val bottomPx: Float
    )

    private var previousEditorMeasures: EditorMeasures? = null

    private val screenMeasuresFlow = MutableStateFlow(ComposeScreenMeasures.Initial)
    private val webMeasureEvents = MutableSharedFlow<WebViewMeasures>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        scope.launch {
            webMeasureEvents
                .distinctUntilChanged()
                .debounce(WEB_SETTLE_DEBOUNCE_MS)
                .collectLatest { webMeasures ->
                    val screenMeasures = screenMeasuresFlow.value
                    val editorMeasures = EditorMeasures(screenMeasures, webMeasures)

                    if (shouldProcessEditorMeasuresChange(previousEditorMeasures, editorMeasures)) {
                        processEditorMeasuresChanged(editorMeasures)
                    } else {
                        Timber.d("skipping measures change processing")
                    }

                    previousEditorMeasures = editorMeasures

                }
        }
    }

    fun onScreenMeasuresChanged(screenMeasures: ComposeScreenMeasures) {
        screenMeasuresFlow.value = screenMeasures
    }

    fun onWebViewMeasuresChanged(webViewMeasures: WebViewMeasures) {
        webMeasureEvents.tryEmit(webViewMeasures)
    }

    private fun shouldProcessEditorMeasuresChange(previous: EditorMeasures?, current: EditorMeasures): Boolean {
        if (previous == null) return true

        val contentChanged = previous.webViewMeasures.bodyContentVersion != current.webViewMeasures.bodyContentVersion
        val cursorPositionChanged =
            abs(previous.webViewMeasures.cursorPosition.topPx - current.webViewMeasures.cursorPosition.topPx) > 1f
        val keyboardBecameVisible =
            current.webViewMeasures.isKeyboardVisible && !previous.webViewMeasures.isKeyboardVisible
        val shouldProcess = contentChanged || cursorPositionChanged || keyboardBecameVisible

        if (shouldProcess) {
            Timber.d(
                "should process editor measures:  contentChanged: $contentChanged, " +
                    "cursorPositionChanged: $cursorPositionChanged, keyboardBecameVisible: $keyboardBecameVisible}"
            )
        }
        return shouldProcess
    }

    private fun processEditorMeasuresChanged(editorMeasures: EditorMeasures) {
        Timber.tag("composer-scroll").d("Processing editor measures change $editorMeasures")

        val sizeDeltaDp = calculateWebViewSizeDelta(editorMeasures.webViewMeasures)

        if (cursorIsNotInFocus(editorMeasures)) {
            val scrollToCursor = calculateScrollToCursor(editorMeasures)
            Timber.tag("composer-scroll").d("Cursor is out of focus. Scrolling to: $scrollToCursor")
            onUpdateScroll(scrollToCursor)
            return
        }

        if (cursorIsAtTheEndOfVisibleWebView(editorMeasures)) {
            val oneLineScroll = editorMeasures.screenMeasures.scrollValuePx + sizeDeltaDp
            Timber.tag("composer-scroll").d("Cursor on the last line. Scrolling to: $oneLineScroll")
            onUpdateScroll(oneLineScroll)
            return
        }

        Timber.tag("composer-scroll").d("cursor is focused and not on the last line. No scroll.")
    }

    private fun calculateScrollToCursor(em: EditorMeasures): Float {
        val viewport = em.editorViewport()
        val cursor = em.webViewMeasures.cursorPosition

        val currentScroll = em.screenMeasures.scrollValuePx

        return when {
            // Cursor is below viewport: scroll up just enough so cursor bottom hits viewport bottom
            cursor.bottomPx > viewport.bottomPx -> {
                val delta = cursor.bottomPx - viewport.bottomPx
                // Add safety distance since it was observed that sometimes cursor stays
                // behind the keyboard when we just scroll the above delta amount
                val safetyDistance = em.webViewMeasures.lineHeightPx * 2f
                (currentScroll + delta + safetyDistance).coerceAtLeast(0f)
            }

            // Cursor is above viewport: scroll down just enough so cursor top hits viewport top
            cursor.topPx < viewport.topPx -> {
                val delta = viewport.topPx - cursor.topPx
                (currentScroll - delta).coerceAtLeast(0f)
            }

            else -> currentScroll
        }
    }

    private fun cursorIsAtTheEndOfVisibleWebView(em: EditorMeasures): Boolean {
        val viewport = em.editorViewport()

        val endOfLastLineArea = viewport.bottomPx
        val startOfLastLineArea = (endOfLastLineArea - em.webViewMeasures.lineHeightPx).coerceAtLeast(0f)

        val isCursorOnLastVisibleLine =
            em.webViewMeasures.cursorPosition.centerPx() in startOfLastLineArea..endOfLastLineArea

        return isCursorOnLastVisibleLine
    }

    private fun cursorIsNotInFocus(em: EditorMeasures): Boolean {
        // This is calculated in ComposerScreen through rect intersection between webview and column
        val viewport = em.editorViewport()
        val cursor = em.webViewMeasures.cursorPosition
        val cursorOverlapsViewport = cursor.topPx >= viewport.topPx && cursor.bottomPx <= viewport.bottomPx

        return !cursorOverlapsViewport
    }

    // This is calculated in ComposerScreen through rect intersection between webview and column
    private fun getPortionOfVisibleWebView(screenMeasure: ComposeScreenMeasures) = screenMeasure.visibleWebViewHeightPx

    private fun getStartOfWebViewVisibleArea(screenMeasure: ComposeScreenMeasures) =
        (screenMeasure.scrollValuePx - screenMeasure.headerHeightPx).coerceAtLeast(0f)

    private fun calculateWebViewSizeDelta(webViewMeasures: WebViewMeasures): Float {
        val sizeDelta = (webViewMeasures.heightPx - previousWebViewHeightPx).coerceAtLeast(0f)
        Timber.tag("composer-scroll").d("size delta (previous webview height to new webview height: $sizeDelta")
        previousWebViewHeightPx = webViewMeasures.heightPx
        return sizeDelta
    }

    private fun EditorMeasures.editorViewport(): EditorViewport {
        val viewportTop = getStartOfWebViewVisibleArea(screenMeasures)
        val viewportHeight = getPortionOfVisibleWebView(screenMeasures)

        val viewportBottom = (viewportTop + viewportHeight).coerceAtMost(webViewMeasures.heightPx)

        return EditorViewport(
            topPx = viewportTop.coerceAtLeast(0f),
            bottomPx = viewportBottom
        )
    }

    companion object {

        private const val WEB_SETTLE_DEBOUNCE_MS = 150L
    }

}
