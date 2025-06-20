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

package ch.protonmail.android.mailmessage.presentation.ui

import java.io.ByteArrayInputStream
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.animation.core.ExperimentalAnimatableApi
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.compose.pxToDp
import ch.protonmail.android.mailcommon.presentation.extension.copyTextToClipboard
import ch.protonmail.android.mailcommon.presentation.extension.openShareIntentForUri
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MimeType
import ch.protonmail.android.mailmessage.presentation.R
import ch.protonmail.android.mailmessage.presentation.extension.isEmbeddedImage
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyUiModel
import ch.protonmail.android.mailmessage.presentation.model.ViewModePreference
import ch.protonmail.android.mailmessage.presentation.model.webview.MessageBodyWebViewOperation
import ch.protonmail.android.mailmessage.presentation.viewmodel.MessageBodyWebViewViewModel
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalAnimatableApi::class)
@Composable
fun MessageBodyWebView(
    modifier: Modifier = Modifier,
    messageBodyUiModel: MessageBodyUiModel,
    webViewActions: MessageBodyWebView.Actions,
    onMessageBodyLoaded: (messageId: MessageId, height: Int) -> Unit = { _, _ -> },
    viewModel: MessageBodyWebViewViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val state = rememberWebViewStateWithHTMLData(
        data = messageBodyUiModel.messageBody,
        mimeType = MimeType.Html.value
    )

    val webViewInteractionState = viewModel.state.collectAsStateWithLifecycle().value
    val longClickDialogState = remember { mutableStateOf(false) }

    val actions = webViewActions.copy(
        onMessageBodyLinkLongClicked = {
            viewModel.submit(MessageBodyWebViewOperation.MessageBodyWebViewAction.LongClickLink(it))
        }
    )

    val longClickDialogActions = MessageWebViewLongPressDialog.Actions(
        onCopyClicked = { uri ->
            context.copyTextToClipboard(
                label = context.getString(R.string.message_link_long_click_copy_description),
                text = uri.toString()
            )
            longClickDialogState.value = false
        },
        onShareClicked = { uri ->
            context.openShareIntentForUri(uri, context.getString(R.string.message_link_long_click_share_via))
            longClickDialogState.value = false
        },
        onDismissed = { longClickDialogState.value = false }
    )

    val messageId = messageBodyUiModel.messageId

    val isSystemInDarkTheme = isSystemInDarkTheme()

    var webView by remember { mutableStateOf<WebView?>(null) }
    var contentLoaded = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = messageBodyUiModel.viewModePreference) {
        webView?.let {
            configureDarkLightMode(it, isSystemInDarkTheme, messageBodyUiModel.viewModePreference)
        }
    }

    val client = remember(messageBodyUiModel.messageId) {
        object : AccompanistWebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.let {
                    actions.onMessageBodyLinkClicked(it.url)
                }
                return true
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return if (request?.isEmbeddedImage() == true) {
                    actions.loadEmbeddedImage(messageId, request.url.schemeSpecificPart)?.let {
                        WebResourceResponse(it.mimeType, "", ByteArrayInputStream(it.data))
                    }
                } else {
                    super.shouldInterceptRequest(view, request)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                contentLoaded.value = true
            }
        }
    }

    Column(modifier) {
        key(client) {
            val attachmentsUiModel = messageBodyUiModel.attachments
            if (attachmentsUiModel != null && attachmentsUiModel.attachments.isNotEmpty()) {
                AttachmentList(
                    modifier = Modifier.background(color = ProtonTheme.colors.backgroundNorm),
                    messageAttachmentsUiModel = attachmentsUiModel,
                    actions = AttachmentList.Actions(
                        onShowAllAttachments = actions.onShowAllAttachments,
                        onAttachmentClicked = actions.onAttachmentClicked,
                        onToggleExpandCollapseMode = actions.onToggleAttachmentsExpandCollapseMode
                    )
                )
            }
            RevealWebView(contentLoaded = contentLoaded, onHeightFinalised = { height ->
                onMessageBodyLoaded(messageId, height)
            }) {
                WebView(
                    onCreated = {
                        it.settings.builtInZoomControls = true
                        it.settings.displayZoomControls = false
                        it.settings.javaScriptEnabled = false
                        it.settings.safeBrowsingEnabled = true
                        it.settings.allowContentAccess = false
                        it.settings.allowFileAccess = false
                        it.settings.loadWithOverviewMode = true
                        configureDarkLightMode(it, isSystemInDarkTheme, messageBodyUiModel.viewModePreference)
                        configureLongClick(it, actions.onMessageBodyLinkLongClicked)
                        webView = it
                    },
                    captureBackPresses = false,
                    state = state,
                    modifier = Modifier
                        .testTag(MessageBodyWebViewTestTags.WebView)
                        // there's a bug where if the message is too long the webview will crash
                        .heightIn(max = (WEB_VIEW_FIXED_MAX_HEIGHT - 1).pxToDp())
                        .fillMaxWidth()
                        .wrapContentSize(),
                    client = client
                )
            }
        }

        if (messageBodyUiModel.shouldShowExpandCollapseButton) {
            ExpandCollapseBodyButton(
                modifier = Modifier.offset(x = ProtonDimens.Spacing.Standard),
                onClick = { actions.onExpandCollapseButtonCLicked() }
            )
        }
    }

    if (longClickDialogState.value && webViewInteractionState.lastFocusedUri != null) {
        MessageWebViewLongPressDialog(
            actions = longClickDialogActions,
            linkUri = webViewInteractionState.lastFocusedUri
        )
    }

    ConsumableLaunchedEffect(webViewInteractionState.longClickLinkEffect) {
        longClickDialogState.value = true
    }
}

private fun configureLongClick(view: WebView, onLongClick: (uri: Uri) -> Unit) {
    view.setOnLongClickListener {
        val result = (it as WebView).hitTestResult
        val type = result.type

        if (listOf(WebView.HitTestResult.EMAIL_TYPE, WebView.HitTestResult.SRC_ANCHOR_TYPE).contains(type)) {
            val uri = runCatching { Uri.parse(result.extra) }.getOrNull() ?: return@setOnLongClickListener false
            onLongClick(uri)
            return@setOnLongClickListener true
        }

        false
    }
}

private fun configureDarkLightMode(
    webView: WebView,
    isInDarkTheme: Boolean,
    viewModePreference: ViewModePreference
) {
    if (isInDarkTheme && viewModePreference == ViewModePreference.LightMode) {
        webView.showInLightMode()
    }
    // In other cases, media query in Html content will be used to render correctly
}

/** reveals the content when heights are finalised and content is loaded
avoids weird height glitching where the height changes 3X whilst the webview loads
its content.  This view will wait for loading, all measure passes then perform a reveal
animation **/
@OptIn(FlowPreview::class)
@Composable
fun RevealWebView(
    contentLoaded: MutableState<Boolean>,
    onHeightFinalised: (height: Int) -> Unit = { _ -> },
    mainContent: @Composable () -> Unit
) {
    var reportedWebViewHeight by remember { mutableIntStateOf(0) }
    val webViewTargetHeightPx = remember { mutableIntStateOf(0) }

    val webviewHeightAnimationValues: Int by animateIntAsState(
        targetValue = webViewTargetHeightPx.intValue,
        animationSpec = tween(
            durationMillis = 200
        )
    )
    // alpha is calculated as a percentage of the current height reveal
    val alphaTween =
        remember {
            derivedStateOf {
                webviewHeightAnimationValues.divideBy(webViewTargetHeightPx.intValue.toFloat())
            }
        }

    LaunchedEffect(reportedWebViewHeight) {
        snapshotFlow { reportedWebViewHeight }
            // the webpage has loaded and the target height has not already been set
            .filter { contentLoaded.value && webViewTargetHeightPx.intValue == 0 }
            // allow measuring passes and webview to settle
            .debounce(timeoutMillis = 100L)
            .collectLatest { height ->
                webViewTargetHeightPx.intValue = height
                onHeightFinalised(height)
            }
    }

    /**
     * Although this code is  normally possible with the modifiers onSizeChanged() and .height(),
     * it's not possible here since we need these values _before_ the webview has a change to render.
     *
     * We need to go down to the measuring pass in order to buffer the reported heights before the webview renders.
     * Until we are confident with a height the webview will be laid out with a height of 0
     * (note if you do this using modifiers then your onSizeChanged will always stay at 0 and you'll never get a
     * calculated height).
     * As soon as we are happy with our target height (using throttling until the reported height settles)
     * we animate the rendered height (in the layout) to our target height
     */
    Layout(
        content = mainContent,
        modifier = Modifier.graphicsLayer {
            // its recommended to set alpha in the graphics layer if you are
            // setting alpha according to a state
            alpha = alphaTween.value
        }
    ) { measurables, constraints ->

        val placeables: List<Placeable> = measurables.map { measurable ->
            measurable.measure(constraints).apply {
                if (height > 0) {
                    // set our reported height state will will be throttled until it has settled
                    reportedWebViewHeight = height
                }
            }
        }
        val itemsTotalWidth = placeables.sumOf { placeable -> placeable.width }
        // layout according to our reveal animation height
        layout(itemsTotalWidth, webviewHeightAnimationValues) {
            placeables.forEach { placeable ->
                placeable.placeRelative(0, 0)
            }
        }
    }
}


@Composable
private fun ExpandCollapseBodyButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier
            .padding(ProtonDimens.Spacing.Small)
            .height(MessageBodyDimens.ExpandButtonHeight)
            .width(MessageBodyDimens.ExpandButtonWidth),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(MessageBodyDimens.ExpandButtonRoundedCornerPercent),
        border = BorderStroke(MailDimens.DefaultBorder, ProtonTheme.colors.shade20),
        colors = ButtonDefaults.buttonColors().copy(containerColor = ProtonTheme.colors.backgroundNorm),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        onClick = { onClick() }
    ) {
        Icon(
            modifier = Modifier
                .size(MessageBodyDimens.ExpandButtonHeight)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.ic_proton_three_dots_horizontal),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = NO_CONTENT_DESCRIPTION
        )
    }
}

@Preview
@Composable
private fun ExpandCollapseBodyButtonPreview() {
    ProtonTheme {
        ExpandCollapseBodyButton(onClick = {})
    }
}


object MessageBodyWebView {

    data class Actions(
        val onMessageBodyLinkClicked: (uri: Uri) -> Unit,
        val onMessageBodyLinkLongClicked: (uri: Uri) -> Unit,
        val onShowAllAttachments: () -> Unit,
        val onExpandCollapseButtonCLicked: () -> Unit,
        val onAttachmentClicked: (attachmentId: AttachmentId) -> Unit,
        val onToggleAttachmentsExpandCollapseMode: () -> Unit,
        val loadEmbeddedImage: (messageId: MessageId, contentId: String) -> EmbeddedImage?,
        val onPrint: (MessageId) -> Unit
    )
}

object MessageBodyWebViewTestTags {

    const val WebView = "MessageBodyWebView"
}

private const val WEB_VIEW_FIXED_MAX_HEIGHT = 262_143

// we can't divide by 0 so guard
private fun Int.divideBy(value: Float): Float = if (value == 0f) 0f else this.div(value)
