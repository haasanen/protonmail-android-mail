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

package ch.protonmail.android.mailsettings.presentation.webaccountsettings

import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.webaccountsettings.model.AccountSettingsAction
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonErrorMessage
import me.proton.core.compose.component.ProtonSettingsTopBar
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.util.kotlin.exhaustive
import timber.log.Timber

@Composable
fun WebAccountSettingScreen(
    actions: WebAccountSettingScreen.Actions,
    modifier: Modifier = Modifier,
    accountSettingsViewModel: WebAccountSettingsViewModel = hiltViewModel()
) {
    when (
        val settingsState = rememberAsState(
            flow = accountSettingsViewModel.state,
            WebAccountSettingsState.Loading
        ).value
    ) {
        is WebAccountSettingsState.Data -> WebAccountSettingScreen(
            modifier = modifier,
            state = settingsState,
            actions = WebAccountSettingScreen.Actions(
                onBackClick = {
                    accountSettingsViewModel.submit(AccountSettingsAction.OnCloseAccountSettings)
                    actions.onBackClick()
                }
            )
        )

        is WebAccountSettingsState.Error -> ProtonErrorMessage(errorMessage = settingsState.errorMessage)

        WebAccountSettingsState.Loading -> ProtonCenteredProgress()
        WebAccountSettingsState.NotLoggedIn ->
            ProtonErrorMessage(errorMessage = stringResource(id = R.string.x_error_not_logged_in))
    }.exhaustive

}

@Composable
fun WebAccountSettingScreen(
    modifier: Modifier = Modifier,
    state: WebAccountSettingsState.Data,
    actions: WebAccountSettingScreen.Actions
) {
    Timber.d("web-settings: WebAccountSettingScreen: $state")
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsTopBar(
                title = stringResource(id = R.string.mail_settings_account_settings),
                onBackClick = actions.onBackClick
            )
        },
        content = { paddingValues ->
            AccountSettingWebView(
                modifier
                    .padding(paddingValues),
                state = state
            )
        }
    )

    BackHandler {
        actions.onBackClick()
    }
}

@Composable
fun AccountSettingWebView(modifier: Modifier = Modifier, state: WebAccountSettingsState.Data) {

    val client = remember(state) {
        object : AccompanistWebViewClient() {

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                Timber.e("web-settings: onReceivedSslError: $error")
                super.onReceivedSslError(view, handler, error)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Timber.e("web-settings: onReceivedError: $error")
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                errorResponse?.let {
                    Timber.e("web-settings: HTTP error: ${it.statusCode}, ${it.reasonPhrase}, URL: ${request?.url}")
                }
                super.onReceivedHttpError(view, request, errorResponse)
            }
        }
    }

    val chromeClient = remember {
        object : AccompanistWebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                Timber.d("web-settings: onProgressChanged: $newProgress")
                super.onProgressChanged(view, newProgress)
            }
        }
    }

    val additionalHeaders = mapOf(
        "x-pm-appversion" to "Other"
    )

    val webViewState = rememberWebViewState(
        url = state.accountSettingsUrl,
        additionalHttpHeaders = additionalHeaders
    )
    Column(modifier = modifier.fillMaxSize()) {
        WebView(
            onCreated = {
                it.settings.javaScriptEnabled = true
                it.settings.domStorageEnabled = true
                it.settings.loadWithOverviewMode = true
                it.settings.allowFileAccess = false
                it.settings.allowContentAccess = true
                it.settings.useWideViewPort = true
                it.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                it.settings.safeBrowsingEnabled = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.settings.isAlgorithmicDarkeningAllowed = true
                }
            },
            captureBackPresses = true,
            state = webViewState,
            modifier = Modifier
                .fillMaxSize(),
            client = client,
            chromeClient = chromeClient
        )
    }
}

object WebAccountSettingScreen {

    data class Actions(
        val onBackClick: () -> Unit
    )
}
