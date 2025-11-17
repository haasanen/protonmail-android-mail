package ch.protonmail.android.mailcomposer.presentation.ui

import ch.protonmail.android.mailcomposer.presentation.model.editor.ComposeScreenMeasures
import ch.protonmail.android.mailcomposer.presentation.model.editor.CursorPosition
import ch.protonmail.android.mailcomposer.presentation.model.editor.EditorViewDrawingState
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import org.junit.Test
import kotlin.test.assertEquals
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class EditorScrollManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val defaultScreenMeasures = ComposeScreenMeasures(
        visibleWebViewHeightPx = 100f,
        visibleHeaderHeightPx = 0f,
        headerHeightPx = 0f,
        scrollValuePx = 0f
    )

    private val defaultLineHeightPx = 20f

    private val onUpdateScroll: (Float) -> Unit = mockk(relaxed = true)
    private val onToggleViewportAlignment: (Boolean) -> Unit = mockk(relaxed = true)

    private val editorScrollManager =
        EditorScrollManager(
            scope = scope,
            onUpdateScroll = onUpdateScroll,
            onToggleViewportAlignment = onToggleViewportAlignment
        )

    private fun emitScreenMeasures(screenMeasures: ComposeScreenMeasures) {
        editorScrollManager.onScreenMeasuresChanged(screenMeasures)
    }

    private fun emitWebMeasures(webMeasures: EditorViewDrawingState) {
        editorScrollManager.onWebViewMeasuresChanged(webMeasures)
    }

    @Test
    fun `scrolls up when cursor is below viewport`() = runTest {
        // Given
        emitScreenMeasures(defaultScreenMeasures)

        // viewport: [0, 100], cursor: [150, 170]
        val webMeasures = EditorViewDrawingState(
            heightPx = 300f,
            cursorPosition = CursorPosition(
                topPx = 150f,
                bottomPx = 170f
            ),
            lineHeightPx = defaultLineHeightPx,
            bodyContentVersion = 1,
            isKeyboardVisible = false
        )

        // When
        emitWebMeasures(webMeasures)
        advanceUntilIdle()

        // Then
        // viewportBottom = 100
        // cursorBottom = 170 --> delta = 70
        // safetyDistance = lineHeight * 2 = 40
        // newScroll = 0 + 70 + 40 = 110
        verify(exactly = 1) {
            onUpdateScroll(
                withArg { value ->
                    assertEquals(110f, value, 0.01f)
                }
            )
        }
    }

    @Test
    fun `scrolls down when cursor is above viewport`() = runTest {
        // Given
        // viewportTop = scroll - header = 100, viewportBottom = 200
        val screen = defaultScreenMeasures.copy(
            visibleWebViewHeightPx = 100f,
            scrollValuePx = 100f
        )
        emitScreenMeasures(screen)

        // cursor above viewport: [50, 70]
        val webMeasures = EditorViewDrawingState(
            heightPx = 300f,
            cursorPosition = CursorPosition(
                topPx = 50f,
                bottomPx = 70f
            ),
            lineHeightPx = defaultLineHeightPx,
            bodyContentVersion = 1,
            isKeyboardVisible = false
        )

        // When
        emitWebMeasures(webMeasures)
        advanceUntilIdle()

        // Then
        // viewportTop = 100, cursorTop = 50
        // delta = 100 - 50 = 50
        // newScroll = 100 - 50 = 50
        verify(exactly = 1) {
            onUpdateScroll(
                withArg { value ->
                    assertEquals(50f, value, 0.01f)
                }
            )
        }
    }

    @Test
    fun `does not scroll when cursor is fully inside viewport`() = runTest {
        // Given
        emitScreenMeasures(defaultScreenMeasures)

        // viewport: [0, 100], cursor: [40, 60] fully inside
        val webMeasures = EditorViewDrawingState(
            heightPx = 300f,
            cursorPosition = CursorPosition(
                topPx = 40f,
                bottomPx = 60f
            ),
            lineHeightPx = defaultLineHeightPx,
            bodyContentVersion = 1,
            isKeyboardVisible = false
        )

        // When
        emitWebMeasures(webMeasures)
        advanceUntilIdle()

        // Then
        verify(exactly = 0) { onUpdateScroll(any()) }
    }

    @Test
    fun `second identical measures should be ignored, no extra scroll`() = runTest {
        // Given
        emitScreenMeasures(defaultScreenMeasures)

        val webMeasures = EditorViewDrawingState(
            heightPx = 300f,
            cursorPosition = CursorPosition(
                topPx = 150f,
                bottomPx = 170f
            ),
            lineHeightPx = defaultLineHeightPx,
            bodyContentVersion = 1,
            isKeyboardVisible = false
        )

        // When
        // First emission -> should scroll
        emitWebMeasures(webMeasures)
        advanceUntilIdle()

        // Second identical emission
        emitWebMeasures(webMeasures)
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { onUpdateScroll(any()) }
    }

    @Test
    fun `content change triggers reprocessing even when cursor does not move`() = runTest {
        // Given
        emitScreenMeasures(defaultScreenMeasures)

        val webMeasuresFirst = EditorViewDrawingState(
            heightPx = 300f,
            cursorPosition = CursorPosition(
                topPx = 150f,
                bottomPx = 170f
            ),
            lineHeightPx = defaultLineHeightPx,
            bodyContentVersion = 1,
            isKeyboardVisible = false
        )
        val webMeasuresSecond = webMeasuresFirst.copy(bodyContentVersion = 2)

        // When
        emitWebMeasures(webMeasuresFirst)
        advanceUntilIdle()

        emitWebMeasures(webMeasuresSecond)
        advanceUntilIdle()

        // Then
        verify(exactly = 2) { onUpdateScroll(any()) }
    }

    @Test
    fun `keyboard visibility change triggers reprocessing when cursor out of viewport`() = runTest {
        // Given
        emitScreenMeasures(defaultScreenMeasures)

        // cursor below viewport
        val webMeasuresKeyboardHidden = EditorViewDrawingState(
            heightPx = 300f,
            cursorPosition = CursorPosition(
                topPx = 150f,
                bottomPx = 170f
            ),
            lineHeightPx = defaultLineHeightPx,
            bodyContentVersion = 1,
            isKeyboardVisible = false
        )
        val webMeasuresKeyboardVisible = webMeasuresKeyboardHidden.copy(isKeyboardVisible = true)

        // When
        emitWebMeasures(webMeasuresKeyboardHidden)
        advanceUntilIdle()

        emitWebMeasures(webMeasuresKeyboardVisible)
        advanceUntilIdle()

        // Then
        verify(exactly = 2) { onUpdateScroll(any()) }
    }

    @Test
    fun `cursor on last visible line scrolls by webview growth delta`() = runTest {
        // Given
        emitScreenMeasures(
            defaultScreenMeasures.copy(
                visibleWebViewHeightPx = 100f,
                scrollValuePx = 0f
            )
        )

        val firstMeasures = EditorViewDrawingState(
            heightPx = 200f,
            cursorPosition = CursorPosition(
                topPx = 80f,
                bottomPx = 100f
            ),
            lineHeightPx = defaultLineHeightPx,
            bodyContentVersion = 1,
            isKeyboardVisible = false
        )
        emitWebMeasures(firstMeasures)
        advanceUntilIdle()

        // When
        // Second: webview grows by 50px, cursor still at bottom of viewport
        val secondMeasures = firstMeasures.copy(
            heightPx = 250f,
            bodyContentVersion = 2
        )
        emitWebMeasures(secondMeasures)
        advanceUntilIdle()

        // Then
        // We expect two scrolls overall:
        //  - first: sizeDelta = 200 --> scroll by 200 (cursor at last line)
        //  - second: sizeDelta = 50  --> scroll by 50
        verify(exactly = 2) {
            onUpdateScroll(any())
        }
    }
}
