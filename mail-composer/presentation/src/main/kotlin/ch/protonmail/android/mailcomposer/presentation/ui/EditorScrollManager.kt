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

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import ch.protonmail.android.mailcomposer.presentation.model.ComposeScreenParams
import ch.protonmail.android.mailcomposer.presentation.model.WebViewParams
import timber.log.Timber

private val MIN_SCROLL_CHANGE = 100.dp

class EditorScrollManager(
    val onUpdateScroll: (Dp) -> Unit
) {

    private var previousWebViewHeightDp = 0.dp

    fun onEditorParamsChanged(screenParams: ComposeScreenParams, webViewParams: WebViewParams) {
        Timber.d("composer-scroll: ComposerForm params: $screenParams")
        Timber.d("composer-scroll: WebView params: $webViewParams")

        val sizeDeltaDp = calculateWebViewSizeDelta(webViewParams)

        if (sizeDeltaDp > MIN_SCROLL_CHANGE) {
            Timber.d("composer-scroll: that's too much scrolling. I'd rather stay.")
            Timber.d("composer-scroll: ----------------------------------------------------------------------")
            return
        }

        if (cursorIsNotInFocus(screenParams, webViewParams)) {
            val scrollToCursor = webViewParams.cursorPositionDp + screenParams.scrollValueDp
            Timber.d("composer-scroll: Cursor out of focus scrolling: $scrollToCursor")
            onUpdateScroll(scrollToCursor)
            Timber.d("composer-scroll: ----------------------------------------------------------------------")
            return
        }
        Timber.d("composer-scroll: cursor is in focus")

        if (cursorIsAtThenEndOfVisibleWebView(screenParams, webViewParams)) {
            val oneLineScroll = screenParams.scrollValueDp + sizeDeltaDp
            Timber.d("composer-scroll: Cursor in last line, scrolling to: $oneLineScroll")
            onUpdateScroll(oneLineScroll)
            Timber.d("composer-scroll: ----------------------------------------------------------------------")
            return
        }

        Timber.d("composer-scroll: cursor is neither on last line nor out of focus. No scroll.")
        Timber.d("composer-scroll: ----------------------------------------------------------------------")
    }

    private fun cursorIsAtThenEndOfVisibleWebView(
        screenParams: ComposeScreenParams,
        webViewParams: WebViewParams
    ): Boolean {
        // This is calculated in ComposerScreen through rect intersection between webview and column
        val portionOfWebViewVisible = screenParams.visibleWebViewHeightDp

        // Full line height (including hardcoded interline space) coming through javascript interface
        val numberOfLinesFittingVisibleWebView = portionOfWebViewVisible / webViewParams.lineHeightDp

        val startOfTheWebViewVisibility = (screenParams.scrollValueDp - screenParams.headerHeightDp).coerceAtLeast(0.dp)
        val startOfLastLineArea = startOfTheWebViewVisibility + portionOfWebViewVisible - webViewParams.lineHeightDp
        val endOfLastLineArea = startOfLastLineArea + webViewParams.lineHeightDp
        val isCursorOnLastVisibleLine = webViewParams.cursorPositionDp in startOfLastLineArea..endOfLastLineArea

        Timber.d(
            """
                composer-scroll: is cursor at the end of the webview:
                | portionOfWebViewVisible : $portionOfWebViewVisible 
                | numberOfLinesFittingVisibleWebView : $numberOfLinesFittingVisibleWebView 
                | startOfTheWebViewVisibility : $startOfTheWebViewVisibility 
                | startOfLastLineArea : $startOfLastLineArea 
                | endOfLastLineArea : $endOfLastLineArea
                | iscursorOnLastVisibleLine : $isCursorOnLastVisibleLine 
            """.trimIndent()
        )

        return isCursorOnLastVisibleLine
    }

    // TODO: implement, shadow the logic above
    @SuppressWarnings("ForbiddenComment", "ExpressionBodySyntax", "FunctionOnlyReturningConstant")
    private fun cursorIsNotInFocus(screenParams: ComposeScreenParams, webViewParams: WebViewParams): Boolean {
//        val portionOfWebViewVisible = screenParams.screenHeightDp - screenParams.visibleHeaderHeightDp

//        val startOfTheWebViewVisibility = screenParams.scrollValueDp - screenParams.headerHeightDp

//        val webViewVisibleRange = startOfTheWebViewVisibility..startOfTheWebViewVisibility + portionOfWebViewVisible

//        val isCursorNotInFocus = webViewParams.cursorPositionDp !in webViewVisibleRange
//        Timber.d("""
//            | composer-scroll: is cursor out of focus
//            | portionOfWebViewVisible : $portionOfWebViewVisible
//            | startOfTheWebViewVisibility : $startOfTheWebViewVisibility
//            | webViewVisibleRange : $webViewVisibleRange
//            | isCursorNotInFocus: $isCursorNotInFocus
//        """.trimIndent())

//        return isCursorNotInFocus
        return false
    }

    private fun calculateWebViewSizeDelta(webViewParams: WebViewParams): Dp {
        val sizeDelta = (webViewParams.heightDp - previousWebViewHeightDp).coerceAtLeast(0.dp)
        Timber.d("composer-scroll: size delta (previous webview height to new webview height: $sizeDelta")
        previousWebViewHeightDp = webViewParams.heightDp
        return sizeDelta
    }


}
