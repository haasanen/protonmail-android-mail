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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.websettings.model.WebSettingsAction
import ch.protonmail.android.mailsettings.presentation.websettings.SettingWebView
import ch.protonmail.android.mailsettings.presentation.websettings.WebSettingsState
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
            WebSettingsState.Loading
        ).value
    ) {
        is WebSettingsState.Data -> WebAccountSettingScreen(
            modifier = modifier,
            state = settingsState,
            actions = WebAccountSettingScreen.Actions(
                onBackClick = {
                    accountSettingsViewModel.submit(WebSettingsAction.OnCloseWebSettings)
                    actions.onBackClick()
                }
            )
        )

        is WebSettingsState.Error -> ProtonErrorMessage(errorMessage = settingsState.errorMessage)

        WebSettingsState.Loading -> ProtonCenteredProgress()
        WebSettingsState.NotLoggedIn ->
            ProtonErrorMessage(errorMessage = stringResource(id = R.string.x_error_not_logged_in))
    }.exhaustive

}

@Composable
fun WebAccountSettingScreen(
    modifier: Modifier = Modifier,
    state: WebSettingsState.Data,
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
            SettingWebView(
                modifier
                    .padding(paddingValues),
                state = state
            )
        }
    )
}

object WebAccountSettingScreen {

    data class Actions(
        val onBackClick: () -> Unit
    )
}
