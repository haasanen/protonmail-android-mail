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
import ch.protonmail.android.mailcomposer.presentation.model.ComposeScreenMeasures
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import timber.log.Timber

class EditorScrollManager(
    val onUpdateScroll: (Dp) -> Unit
) {

    private var previousWebViewHeightDp = 0.dp

    fun onEditorParamsChanged(screenMeasure: ComposeScreenMeasures, webViewMeasures: WebViewMeasures) {
        Timber.tag("composer-scroll").d("ComposerForm measures: $screenMeasure")
        Timber.tag("composer-scroll").d("WebView measures: $webViewMeasures")

        val sizeDeltaDp = calculateWebViewSizeDelta(webViewMeasures)

        if (cursorIsNotInFocus(screenMeasure, webViewMeasures)) {
            val scrollToCursor = calculateScrollToCursor(webViewMeasures, screenMeasure)
            Timber.tag("composer-scroll").d("Cursor is out of focus. Scrolling to: $scrollToCursor")
            onUpdateScroll(scrollToCursor)
            return
        }

        if (cursorIsAtThenEndOfVisibleWebView(screenMeasure, webViewMeasures)) {
            val oneLineScroll = screenMeasure.scrollValueDp + sizeDeltaDp
            Timber.tag("composer-scroll").d("Cursor on the last line. Scrolling to: $oneLineScroll")
            onUpdateScroll(oneLineScroll)
            return
        }

        Timber.tag("composer-scroll").d("cursor is focused and not on the last line. No scroll.")
    }

    private fun calculateScrollToCursor(webViewMeasures: WebViewMeasures, screenMeasure: ComposeScreenMeasures) =
        webViewMeasures.cursorPositionDp + screenMeasure.headerHeightDp

    private fun cursorIsAtThenEndOfVisibleWebView(
        screenMeasure: ComposeScreenMeasures,
        webViewMeasures: WebViewMeasures
    ): Boolean {
        val startOfLastLineArea = getStartOfLastLineArea(screenMeasure, webViewMeasures)
        val endOfLastLineArea = getEndOfLastLineArea(startOfLastLineArea, webViewMeasures)
        val isCursorOnLastVisibleLine = webViewMeasures.cursorPositionDp in startOfLastLineArea..endOfLastLineArea

        return isCursorOnLastVisibleLine
    }

    private fun cursorIsNotInFocus(screenMeasure: ComposeScreenMeasures, webViewMeasures: WebViewMeasures): Boolean {
        // This is calculated in ComposerScreen through rect intersection between webview and column
        val portionOfWebViewVisible = getPortionOfVisibleWebView(screenMeasure)
        val startOfWebViewVisibleArea = getStartOfWebViewVisibleArea(screenMeasure)
        val webViewVisibleRange = startOfWebViewVisibleArea..startOfWebViewVisibleArea + portionOfWebViewVisible
        val isCursorNotInFocus = webViewMeasures.cursorPositionDp !in webViewVisibleRange

        return isCursorNotInFocus
    }

    // The bottom bound of the last "line" where the cursor fits
    // (which also corresponds with the end of the visible portion of the webview)
    private fun getEndOfLastLineArea(startOfLastLineArea: Dp, webViewMeasures: WebViewMeasures) =
        startOfLastLineArea + webViewMeasures.lineHeightDp

    // The top bound of the last "line" where the cursor fits
    private fun getStartOfLastLineArea(screenMeasure: ComposeScreenMeasures, webViewMeasures: WebViewMeasures) =
        getStartOfWebViewVisibleArea(screenMeasure) +
            getPortionOfVisibleWebView(screenMeasure) -
            webViewMeasures.lineHeightDp

    // This is calculated in ComposerScreen through rect intersection between webview and column
    private fun getPortionOfVisibleWebView(screenMeasure: ComposeScreenMeasures) = screenMeasure.visibleWebViewHeightDp

    private fun getStartOfWebViewVisibleArea(screenMeasure: ComposeScreenMeasures) =
        (screenMeasure.scrollValueDp - screenMeasure.headerHeightDp).coerceAtLeast(0.dp)

    private fun calculateWebViewSizeDelta(webViewMeasures: WebViewMeasures): Dp {
        val sizeDelta = (webViewMeasures.heightDp - previousWebViewHeightDp).coerceAtLeast(0.dp)
        Timber.tag("composer-scroll").d("size delta (previous webview height to new webview height: $sizeDelta")
        previousWebViewHeightDp = webViewMeasures.heightDp
        return sizeDelta
    }


}
