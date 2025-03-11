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

import ch.protonmail.android.mailcomposer.presentation.model.ComposeScreenParams
import ch.protonmail.android.mailcomposer.presentation.model.WebViewParams
import timber.log.Timber

private const val MIN_SCROLL_CHANGE = 100

class EditorScrollManager(
    val onUpdateScroll: (Int) -> Unit
) {

    private var previousWebViewHeight = 0

    fun onEditorParamsChanged(composeScreenParams: ComposeScreenParams, webViewParams: WebViewParams) {
        val sizeDelta = (webViewParams.height - previousWebViewHeight).coerceAtLeast(0)
        previousWebViewHeight = webViewParams.height

        Timber.d("composer-scroll: composer form params: $composeScreenParams")
        Timber.d("composer-scroll: WebView params: $webViewParams")
        Timber.d("composer-scroll: size delta (previous webview height to new webview height: $sizeDelta")

        if (sizeDelta > MIN_SCROLL_CHANGE) {
            Timber.d("composer-scroll: that's too much scrolling. I'd rather stay.")
            return
        }

        val value = composeScreenParams.scrollValue + sizeDelta
        Timber.d("composer-scroll: required scroll value $value")
        onUpdateScroll(value)
    }


}
