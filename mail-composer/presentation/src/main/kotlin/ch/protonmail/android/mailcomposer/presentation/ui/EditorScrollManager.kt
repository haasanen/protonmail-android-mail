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

private val MIN_SCROLL_CHANGE = 100.dp

class EditorScrollManager(
    val onUpdateScroll: (Dp) -> Unit
) {

    private var previousWebViewHeightDp = 0.dp

    fun onEditorParamsChanged(screenMeasure: ComposeScreenMeasures, webViewMeasures: WebViewMeasures) {
        Timber.tag("composer-scroll").d("ComposerForm measures: $screenMeasure")
        Timber.tag("composer-scroll").d("WebView measures: $webViewMeasures")

        val sizeDeltaDp = calculateWebViewSizeDelta(webViewMeasures)

        if (sizeDeltaDp > MIN_SCROLL_CHANGE) {
            Timber.tag("composer-scroll").d("WebView height update is more than threshold (assuming init ongoing)")
            return
        }

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
        // This is calculated in ComposerScreen through rect intersection between webview and column
        val portionOfWebViewVisible = screenMeasure.visibleWebViewHeightDp

        val startOfTheWebViewVisibility = (screenMeasure.scrollValueDp - screenMeasure.headerHeightDp)
            .coerceAtLeast(0.dp)
        val startOfLastLineArea = startOfTheWebViewVisibility + portionOfWebViewVisible - webViewMeasures.lineHeightDp
        val endOfLastLineArea = startOfLastLineArea + webViewMeasures.lineHeightDp
        val isCursorOnLastVisibleLine = webViewMeasures.cursorPositionDp in startOfLastLineArea..endOfLastLineArea

        return isCursorOnLastVisibleLine
    }

    private fun cursorIsNotInFocus(screenMeasure: ComposeScreenMeasures, webViewMeasures: WebViewMeasures): Boolean {
        // This is calculated in ComposerScreen through rect intersection between webview and column
        val portionOfWebViewVisible = screenMeasure.visibleWebViewHeightDp
        val startOfTheWebViewVisibility = (screenMeasure.scrollValueDp - screenMeasure.headerHeightDp)
            .coerceAtLeast(0.dp)
        val webViewVisibleRange = startOfTheWebViewVisibility..startOfTheWebViewVisibility + portionOfWebViewVisible
        val isCursorNotInFocus = webViewMeasures.cursorPositionDp !in webViewVisibleRange

        return isCursorNotInFocus
    }

    private fun calculateWebViewSizeDelta(webViewMeasures: WebViewMeasures): Dp {
        val sizeDelta = (webViewMeasures.heightDp - previousWebViewHeightDp).coerceAtLeast(0.dp)
        Timber.tag("composer-scroll").d("size delta (previous webview height to new webview height: $sizeDelta")
        previousWebViewHeightDp = webViewMeasures.heightDp
        return sizeDelta
    }


}
