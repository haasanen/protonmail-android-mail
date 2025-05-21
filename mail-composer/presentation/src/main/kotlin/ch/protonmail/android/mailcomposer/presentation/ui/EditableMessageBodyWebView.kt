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

import java.io.ByteArrayInputStream
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.ViewGroup.LayoutParams
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.compose.pxToDp
import ch.protonmail.android.mailcommon.presentation.compose.toDp
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.presentation.extension.isEmbeddedImage
import ch.protonmail.android.mailmessage.presentation.ui.showInDarkMode
import ch.protonmail.android.mailmessage.presentation.ui.showInLightMode
import timber.log.Timber

// Needed to allow "remember" on javascript interface
@SuppressLint("JavascriptInterface")
@Composable
fun EditableMessageBodyWebView(
    modifier: Modifier = Modifier,
    messageBodyUiModel: DraftDisplayBodyUiModel,
    shouldRequestFocus: Effect<Unit>,
    webViewActions: EditableMessageBodyWebView.Actions
) {

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val localDensity = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var webView by remember { mutableStateOf<WebView?>(null) }
    var currentCursorPosition = remember { 0.dp }
    var currentLineHeight = remember { 0.dp }
    val focusRequester = remember { FocusRequester() }

    fun onWebViewResize() {
        val height = webView?.height ?: 0
        Timber.d("editor-webview: webview params change height: $height")
        webViewActions.onWebViewParamsChanged(
            WebViewMeasures(height.toDp(localDensity), currentCursorPosition, currentLineHeight)
        )
    }

    fun onCursorPositionChanged(position: Float, lineHeight: Float) {
        // For unclear reasons, the data exposed we get form the webview (through running custom js)
        // which one would expect being is px, is actually already in DP. Hence, no conversion here.
        currentCursorPosition = Dp(position)
        currentLineHeight = Dp(lineHeight)
    }

    val javascriptCallback = remember {
        JavascriptCallback(webViewActions.onMessageBodyChanged, ::onWebViewResize, ::onCursorPositionChanged)
    }

    val contentLoadingFinished = remember { mutableStateOf(false) }
    val client = object : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            contentLoadingFinished.value = true
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            return if (request?.isEmbeddedImage() == true) {
                webViewActions.loadEmbeddedImage(request.url.schemeSpecificPart)?.let {
                    WebResourceResponse(it.mimeType, "", ByteArrayInputStream(it.data))
                }
            } else {
                super.shouldInterceptRequest(view, request)
            }
        }
    }

    webView?.let { wv ->
        LaunchedEffect(isSystemInDarkTheme) {
            configureDarkLightMode(wv, isSystemInDarkTheme)
        }
        LaunchedEffect(wv) {
            Timber.d("editor-webview: setting initial value on webview (should happen only once!)")
            wv.loadDataWithBaseURL(null, messageBodyUiModel.value, MimeType.Html.value, "utf-8", null)
        }

        if (contentLoadingFinished.value) {
            ConsumableLaunchedEffect(shouldRequestFocus) {
                Timber.d("editor-webview: webview is requesting focus...")
                focusRequester.requestFocus()
            }
        }
    }

    BoxWithConstraints(modifier) {
        // WebView changes it's layout strategy based on
        // it's layoutParams. We convert from Compose Modifier to
        // layout params here.
        val width = if (constraints.hasFixedWidth) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
        val height = if (constraints.hasFixedHeight) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
        val layoutParams = FrameLayout.LayoutParams(width, height)

        AndroidView(
            factory = { context ->
                webViewActions.onBuildWebView.invoke(context).apply {
                    this.settings.builtInZoomControls = true
                    this.settings.displayZoomControls = false
                    this.settings.javaScriptEnabled = true
                    this.settings.safeBrowsingEnabled = true
                    this.settings.allowContentAccess = false
                    this.settings.allowFileAccess = false
                    this.settings.loadWithOverviewMode = true
                    this.settings.useWideViewPort = true
                    this.layoutParams = layoutParams
                    this.addJavascriptInterface(javascriptCallback, JAVASCRIPT_CALLBACK_INTERFACE_NAME)
                    this.webViewClient = client

                    webView = this
                }
            },
            modifier = Modifier
                .heightIn(max = (WEB_VIEW_FIXED_MAX_HEIGHT - 1).pxToDp())
                .focusRequester(focusRequester)
                .onFocusEvent { event ->
                    if (event.hasFocus) {
                        Timber.v("editor-webview: composable webview has focus; focusing html element...")
                        webView?.evaluateJavascript("focusEditor();") {
                            Timber.v("editor-webview: editor webview got focused; show keyboard...")
                            keyboardController?.show()
                        }
                    }
                },
            onRelease = {
                Timber.d("editor-webview: webview is leaving composition for good.")
                webView = null
            }
        )
    }
}

private fun configureDarkLightMode(webView: WebView, isInDarkTheme: Boolean) {
    if (isInDarkTheme) {
        webView.showInDarkMode()
    } else {
        webView.showInLightMode()
    }
}

object EditableMessageBodyWebView {

    data class Actions(
        val onMessageBodyLinkClicked: (uri: Uri) -> Unit,
        val onAttachmentClicked: (attachmentId: AttachmentId) -> Unit,
        val loadEmbeddedImage: (contentId: String) -> EmbeddedImage?,
        val onMessageBodyChanged: (body: String) -> Unit,
        val onWebViewParamsChanged: (params: WebViewMeasures) -> Unit,
        val onBuildWebView: (Context) -> WebView
    )
}

// Max constraint for WebView height. If the height is greater
// than this value, we will not fix the height of the WebView or it will crash.
// (Limit set in androidx.compose.ui.unit.Constraints)
private const val WEB_VIEW_FIXED_MAX_HEIGHT = 262_143
