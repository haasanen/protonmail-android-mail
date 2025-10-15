package ch.protonmail.android.mailcomposer.presentation.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.protonmail.android.mailcomposer.presentation.model.ComposeScreenMeasures
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import org.junit.Test
import kotlin.test.assertEquals

class EditorScrollManagerTest {

    private var actualScrollValue: Dp = 0.dp
    private val onUpdateScrollCallbackMock: (Dp) -> Unit = { scrollValue ->
        actualScrollValue = scrollValue
    }

    private val editorScrollManager = EditorScrollManager(onUpdateScrollCallbackMock)

    @Test
    fun `does not perform any scroll when cursor is within the visible webview area`() {
        // Given
        val screenParams = buildComposerScreenParams(
            visibleWebViewHeightDp = 50,
            visibleHeaderHeightDp = 0,
            headerHeightDp = 20,
            scrollValueDp = 0
        )
        val webViewParams = buildWebViewParams(heightDp = 100, cursorPositionDp = 30)

        // When
        editorScrollManager.onEditorParamsChanged(screenParams, webViewParams)

        // Then
        assertEquals(0.dp, actualScrollValue)
    }

    @Test
    fun `brings cursor back in focus when out of focus`() {
        // Given
        val screenParams = buildComposerScreenParams(
            visibleWebViewHeightDp = 50,
            visibleHeaderHeightDp = 0,
            headerHeightDp = 20,
            scrollValueDp = 0
        )
        val webViewParams = buildWebViewParams(heightDp = 100, cursorPositionDp = 60)
        val expectedScroll = 80.dp // Header Height + Cursor Position

        // When
        editorScrollManager.onEditorParamsChanged(screenParams, webViewParams)

        // Then
        assertEquals(expectedScroll, actualScrollValue)
    }

    @Test
    fun `scrolls one line when cursor is on the last visible line of the webview`() {
        // Given
        val screenParams = buildComposerScreenParams(
            visibleWebViewHeightDp = 50,
            visibleHeaderHeightDp = 0,
            headerHeightDp = 20,
            scrollValueDp = 0
        )
        val webViewParams = buildWebViewParams(heightDp = 100, cursorPositionDp = 50, lineHeightDp = 10)
        val expectedScroll = 100.dp // Current Scroll + WebView Size Delta

        // When
        editorScrollManager.onEditorParamsChanged(screenParams, webViewParams)

        // Then
        assertEquals(expectedScroll, actualScrollValue)
    }

    private fun buildWebViewParams(
        heightDp: Int = 0,
        cursorPositionDp: Int = 0,
        lineHeightDp: Int = 0
    ) = WebViewMeasures(heightDp.dp, cursorPositionDp.dp, lineHeightDp.dp)

    private fun buildComposerScreenParams(
        visibleWebViewHeightDp: Int = 0,
        visibleHeaderHeightDp: Int = 0,
        headerHeightDp: Int = 0,
        scrollValueDp: Int = 0
    ) = ComposeScreenMeasures(
        visibleWebViewHeightDp.dp,
        visibleHeaderHeightDp.dp,
        headerHeightDp.dp,
        scrollValueDp.dp
    )
}
