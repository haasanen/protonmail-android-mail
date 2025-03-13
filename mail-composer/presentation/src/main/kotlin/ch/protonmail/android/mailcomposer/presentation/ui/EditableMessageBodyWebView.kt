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
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.protonmail.android.mailcommon.presentation.compose.pxToDp
import ch.protonmail.android.mailcommon.presentation.compose.toDp
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.presentation.extension.getSecuredWebResourceResponse
import ch.protonmail.android.mailmessage.presentation.extension.isEmbeddedImage
import ch.protonmail.android.mailmessage.presentation.extension.isRemoteContent
import ch.protonmail.android.mailmessage.presentation.extension.isRemoteUnsecuredContent
import ch.protonmail.android.mailmessage.presentation.model.ViewModePreference
import ch.protonmail.android.mailmessage.presentation.ui.showInDarkMode
import ch.protonmail.android.mailmessage.presentation.ui.showInLightMode
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
import timber.log.Timber

// Needed to allow "remember" on javascript interface
@SuppressLint("JavascriptInterface")
@Composable
fun EditableMessageBodyWebView(
    modifier: Modifier = Modifier,
    messageBodyUiModel: DraftDisplayBodyUiModel,
    webViewActions: EditableMessageBodyWebView.Actions
) {
    val state = rememberWebViewStateWithHTMLData(
        data = messageBodyUiModel.value,
        mimeType = MimeType.Html.value
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val viewModePreference = ViewModePreference.LightMode
    val showMediaContent = true

    var webView by remember { mutableStateOf<WebView?>(null) }
    LaunchedEffect(key1 = viewModePreference) {
        webView?.let {
            configureDarkLightMode(it, isSystemInDarkTheme, viewModePreference)
        }
    }

    val client = remember(showMediaContent) {
        object : AccompanistWebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.let {
                    webViewActions.onMessageBodyLinkClicked(it.url)
                }
                return true
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return if (!showMediaContent && request?.isRemoteContent() == true) {
                    WebResourceResponse("", "", null)
                } else if (showMediaContent && request?.isEmbeddedImage() == true) {
                    webViewActions.loadEmbeddedImage(request.url.schemeSpecificPart)?.let {
                        WebResourceResponse(it.mimeType, "", ByteArrayInputStream(it.data))
                    }
                } else if (request?.isRemoteUnsecuredContent() == true) {
                    request.getSecuredWebResourceResponse()
                } else {
                    super.shouldInterceptRequest(view, request)
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                if (!showMediaContent && url?.isRemoteContent() == true) {
                    return
                }
                super.onLoadResource(view, url)
            }
        }
    }

    var currentCursorPosition = remember { 0.dp }
    var currentLineHeight = remember { 0.dp }

    val localDensity = LocalDensity.current

    fun onWebViewResize() {
        if (state.loadingState != LoadingState.Finished) {
            Timber.tag("composer-scroll").d("WebView resized while loading state not finished. Skipping.")
            return
        }

        val height = webView?.height ?: 0
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

    key(client) {
        WebView(
            onCreated = {
                it.settings.builtInZoomControls = true
                it.settings.displayZoomControls = false
                it.settings.javaScriptEnabled = true
                it.settings.safeBrowsingEnabled = true
                it.settings.allowContentAccess = false
                it.settings.allowFileAccess = false
                it.settings.loadWithOverviewMode = true
                it.settings.useWideViewPort = true
                configureDarkLightMode(it, isSystemInDarkTheme, viewModePreference)
                it.addJavascriptInterface(javascriptCallback, JAVASCRIPT_CALLBACK_INTERFACE_NAME)
                webView = it
            },
            captureBackPresses = false,
            state = state,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = (WEB_VIEW_FIXED_MAX_HEIGHT - 1).pxToDp()),
            client = client
        )
    }

}

private fun configureDarkLightMode(
    webView: WebView,
    isInDarkTheme: Boolean,
    viewModePreference: ViewModePreference
) {
    if (isInDarkTheme) {
        configureDarkLightModeWhenInDarkTheme(webView, viewModePreference)
    } else {
        webView.showInLightMode()
    }
}

private fun configureDarkLightModeWhenInDarkTheme(webView: WebView, viewModePreference: ViewModePreference) {
    if (viewModePreference == ViewModePreference.LightMode) {
        webView.showInLightMode()
    } else {
        webView.showInDarkMode()
    }
}

object EditableMessageBodyWebView {

    data class Actions(
        val onMessageBodyLinkClicked: (uri: Uri) -> Unit,
        val onAttachmentClicked: (attachmentId: AttachmentId) -> Unit,
        val loadEmbeddedImage: (contentId: String) -> EmbeddedImage?,
        val onMessageBodyChanged: (body: String) -> Unit,
        val onWebViewParamsChanged: (params: WebViewMeasures) -> Unit
    )
}

// Max constraint for WebView height. If the height is greater
// than this value, we will not fix the height of the WebView or it will crash.
// (Limit set in androidx.compose.ui.unit.Constraints)
private const val WEB_VIEW_FIXED_MAX_HEIGHT = 262_143
