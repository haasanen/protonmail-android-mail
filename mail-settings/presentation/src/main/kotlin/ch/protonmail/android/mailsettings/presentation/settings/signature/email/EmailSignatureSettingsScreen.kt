/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.presentation.settings.signature.email

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonErrorMessage
import ch.protonmail.android.design.compose.component.ProtonSettingsDetailsAppBar
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.websettings.SettingWebView
import ch.protonmail.android.mailsettings.presentation.websettings.WebSettingsScreenActions
import ch.protonmail.android.mailsettings.presentation.websettings.WebSettingsState
import ch.protonmail.android.mailsettings.presentation.websettings.model.WebSettingsAction
import me.proton.core.util.kotlin.exhaustive
import timber.log.Timber

@Composable
fun EmailSignatureSettingScreen(
    actions: WebSettingsScreenActions,
    modifier: Modifier = Modifier,
    emailSignatureSettingsViewModel: EmailSignatureSettingsViewModel = hiltViewModel()
) {
    val settingsState = emailSignatureSettingsViewModel.state.collectAsStateWithLifecycle(
        WebSettingsState.Loading
    ).value

    EmailSignatureSettingScreen(
        modifier = modifier,
        state = settingsState,
        actions = WebSettingsScreenActions.Empty.copy(
            onBackClick = {
                emailSignatureSettingsViewModel.submit(WebSettingsAction.OnCloseWebSettings)
                actions.onBackClick()
            }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSignatureSettingScreen(
    modifier: Modifier = Modifier,
    state: WebSettingsState,
    actions: WebSettingsScreenActions
) {
    Timber.d("web-settings: EmailSignatureSettingScreen: $state")
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsDetailsAppBar(
                title = stringResource(id = R.string.mail_settings_email_signature_settings),
                onBackClick = actions.onBackClick
            )
        },
        content = { paddingValues ->
            when (state) {
                is WebSettingsState.Data ->
                    SettingWebView(
                        modifier.padding(paddingValues),
                        state = state
                    )

                is WebSettingsState.Error -> ProtonErrorMessage(
                    modifier = modifier.padding(paddingValues),
                    errorMessage = state.errorMessage
                )

                WebSettingsState.Loading -> ProtonCenteredProgress(modifier = modifier.padding(paddingValues))
                WebSettingsState.NotLoggedIn ->
                    ProtonErrorMessage(
                        modifier = modifier.padding(paddingValues),
                        errorMessage = stringResource(id = R.string.x_error_not_logged_in)
                    )
            }.exhaustive
        }
    )
}
