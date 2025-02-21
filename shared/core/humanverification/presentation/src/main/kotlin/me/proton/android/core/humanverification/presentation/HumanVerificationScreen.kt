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

package me.proton.android.core.humanverification.presentation

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.component.ProtonCloseButton
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.presentation.ui.webview.ProtonWebView
import me.proton.core.util.kotlin.CoreLogger
import kotlin.time.Duration.Companion.milliseconds

private const val JS_INTERFACE_NAME = "AndroidInterface"
private const val WEB_VIEW_MAX_COLOR_COMPONENT = 255

@Composable
@Suppress("UseComposableActions")
fun HumanVerificationScreen(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onHelpClicked: () -> Unit,
    onSuccess: (String) -> Unit = {},
    url: String,
    defaultCountry: String? = null,
    recoveryPhone: String? = null,
    locale: String? = null,
    headers: List<Pair<String, String>>? = null,
    viewModel: HumanVerificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val connectivityErrorMessage = stringResource(R.string.presentation_connectivity_issues)
    HumanVerificationScreen(
        modifier = modifier,
        onCloseClicked = {
            viewModel.submit(HumanVerificationAction.Cancel())
        },
        onCancel = onCancel,
        onIdle = {
            viewModel.submit(HumanVerificationAction.Load(url, defaultCountry, recoveryPhone, locale, headers))
        },
        onBackClicked = {
            viewModel.submit(HumanVerificationAction.Cancel())
        },
        onHelpClicked = onHelpClicked,
        onSuccess = onSuccess,
        onVerificationResult = {
            viewModel.submit(HumanVerificationAction.Verify(it))
        },
        onResourceLoadingError = {
            viewModel.submit(HumanVerificationAction.Failure.ResourceLoadingError(connectivityErrorMessage))
        },
        headers = headers,
        state = state
    )
}

@Composable
@Suppress("UseComposableActions")
fun HumanVerificationScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onCancel: () -> Unit = {},
    onIdle: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onVerificationResult: (HV3ResponseMessage) -> Unit = {},
    onResourceLoadingError: (response: WebResponseError?) -> Unit = {},
    onSuccess: (String) -> Unit = {},
    headers: List<Pair<String, String>>?,
    state: HumanVerificationViewState
) {
    LaunchedEffect(state) {
        when (state) {
            is HumanVerificationViewState.Success -> onSuccess(state.token)
            is HumanVerificationViewState.Cancel,
            is HumanVerificationViewState.Error -> onCancel()

            is HumanVerificationViewState.Idle -> onIdle()
            else -> Unit
        }
    }

    HumanVerificationScaffold(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onBackClicked = onBackClicked,
        onHelpClicked = onHelpClicked,
        onVerificationResult = onVerificationResult,
        onResourceLoadingError = onResourceLoadingError,
        headers = headers,
        state = state
    )
}

@Composable
@Suppress("UseComposableActions")
fun HumanVerificationScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onBackClicked: () -> Unit,
    onHelpClicked: () -> Unit = {},
    onVerificationResult: (HV3ResponseMessage) -> Unit,
    onResourceLoadingError: (response: WebResponseError?) -> Unit,
    headers: List<Pair<String, String>>?,
    state: HumanVerificationViewState = HumanVerificationViewState.Idle
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.human_verification_title))
                },
                navigationIcon = {
                    ProtonCloseButton(onCloseClicked = onCloseClicked)
                },
                actions = {
                    ProtonTextButton(
                        onClick = onHelpClicked
                    ) {
                        Text(
                            text = stringResource(id = R.string.human_verification_help),
                            color = ProtonTheme.colors.textAccent,
                            style = ProtonTheme.typography.defaultStrongNorm
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            HumanVerificationView(
                modifier = modifier,
                onBackClicked = onBackClicked,
                onVerificationResult = onVerificationResult,
                onResourceLoadingError = onResourceLoadingError,
                headers = headers,
                state = state
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Suppress("UseComposableActions")
@Composable
private fun HumanVerificationView(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit,
    onVerificationResult: (HV3ResponseMessage) -> Unit,
    onResourceLoadingError: (response: WebResponseError?) -> Unit,
    headers: List<Pair<String, String>>?,
    state: HumanVerificationViewState = HumanVerificationViewState.Idle
) {
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    when (state) {
        is HumanVerificationViewState.Load -> {
            HumanVerificationWebViewSetup(
                modifier = modifier,
                onVerificationResult = onVerificationResult,
                headers = headers,
                onResourceLoadingError = onResourceLoadingError,
                state = state
            )
        }

        is HumanVerificationViewState.Idle -> DeferredCircularProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "progressIndicator" },
            defer = 0.milliseconds
        )

        is HumanVerificationViewState.Error.General -> {
            val generalErrorMessage = stringResource(R.string.human_verification_general_error)
            LaunchedEffect(state.message) {
                snackbarHostState.showSnackbar(
                    type = ProtonSnackbarType.ERROR,
                    message = state.message ?: generalErrorMessage
                )
            }
        }

        is HumanVerificationViewState.Notify -> {
            LaunchedEffect(state.message) {
                val snackBarType = when (state.messageType) {
                    HV3ResponseMessage.MessageType.Success -> ProtonSnackbarType.SUCCESS
                    HV3ResponseMessage.MessageType.Error -> ProtonSnackbarType.ERROR
                    else -> ProtonSnackbarType.NORM
                }
                snackbarHostState.showSnackbar(
                    type = snackBarType,
                    message = state.message
                )
            }
        }

        else -> Unit
    }
    BackHandler(true) {
        onBackClicked()
    }
}

@Composable
private fun HumanVerificationWebViewSetup(
    modifier: Modifier = Modifier,
    onVerificationResult: (HV3ResponseMessage) -> Unit,
    onResourceLoadingError: (response: WebResponseError?) -> Unit,
    headers: List<Pair<String, String>>?,
    state: HumanVerificationViewState.Load
) {
    val scope = rememberCoroutineScope()

    AndroidView(
        modifier = modifier,
        factory = {
            ProtonWebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                with(settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                setBackgroundColor(
                    Color.argb(
                        1,
                        WEB_VIEW_MAX_COLOR_COMPONENT,
                        WEB_VIEW_MAX_COLOR_COMPONENT,
                        WEB_VIEW_MAX_COLOR_COMPONENT
                    )
                )
                addJavascriptInterface(VerificationJSInterface(scope, onVerificationResult), JS_INTERFACE_NAME)

                webViewClient =
                    HumanVerificationWebViewClient(
                        headers = state.extraHeaders,
                        loader = state.loader,
                        onResourceLoadingError = onResourceLoadingError
                    )

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                        CoreLogger.i(
                            LogTag.DEFAULT,
                            "Web console: ${message.message()} -- file ${message.sourceId()}(${message.lineNumber()})"
                        )
                        return true
                    }
                }
            }
        }, update = {
            val extraHeaders = headers?.toMap()?.toMutableMap() ?: emptyMap()
            WebView.setWebContentsDebuggingEnabled(state.isWebViewDebuggingEnabled)
            it.loadUrl(state.fullUrl, extraHeaders)
        }
    )
}

@Preview(name = "Light mode", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
internal fun HumanVerificationScreenPreview() {
    ProtonTheme {
        HumanVerificationScreen(
            state = HumanVerificationViewState.Idle,
            headers = emptyList()
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
internal fun HumanVerificationScreenDarkPreview() {
    ProtonTheme {
        HumanVerificationScreen(
            state = HumanVerificationViewState.Idle,
            headers = emptyList()
        )
    }
}
