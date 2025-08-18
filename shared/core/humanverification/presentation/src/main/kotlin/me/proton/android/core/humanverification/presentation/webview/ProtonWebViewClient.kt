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

package me.proton.android.core.humanverification.presentation.webview

import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.google.accompanist.web.AccompanistWebViewClient
import me.proton.core.presentation.utils.openBrowserLink

/**
 * Proton WebViewClient supporting:
 * - Proton Extra Headers.
 */
// See: https://commonsware.com/blog/2015/06/11/psa-webview-regression.html.
open class ProtonWebViewClient(
    private val headers: List<Pair<String, String>>?
) : AccompanistWebViewClient() {

    private var isFinished = false

    var shouldOpenLinkInBrowser: Boolean = true

    override fun onPageFinished(view: WebView?, url: String?) {
        isFinished = true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (shouldOpenLinkInBrowser) {
            val url = request.url.toString()
            return if (shouldKeepInWebView(url)) {
                view.loadUrl(url, headers?.toMap() ?: emptyMap())
                true
            } else {
                view.context.openBrowserLink(url)
                true
            }
        }
        return false
    }

    open fun shouldKeepInWebView(url: String): Boolean = !isFinished
}
