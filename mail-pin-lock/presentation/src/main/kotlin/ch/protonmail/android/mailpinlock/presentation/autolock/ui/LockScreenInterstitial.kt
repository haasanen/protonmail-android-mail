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

package ch.protonmail.android.mailpinlock.presentation.autolock.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockInterstitialState
import ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel.LockScreenViewModel

@Composable
fun LockScreenInterstitial(
    onClose: () -> Unit,
    onNavigateToPinInsertion: () -> Unit,
    viewModel: LockScreenViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val item = LocalActivity.current

    when (state) {
        AutoLockInterstitialState.Biometrics -> LockScreenBiometricsPrompt(
            {
                viewModel.onSuccessfulBiometrics()
            },
            onCloseAll = { item?.finishAffinity() }
        )

        AutoLockInterstitialState.Error -> onClose()
        AutoLockInterstitialState.Loading -> Unit
        AutoLockInterstitialState.Pin -> onNavigateToPinInsertion()
    }
}

@Composable
private fun LockScreenBiometricsPrompt(onClose: () -> Unit, onCloseAll: () -> Unit) {
    val biometricPrompt = rememberBiometricAuthenticator(
        title = stringResource(R.string.mail_settings_biometrics_title_app_locked),
        subtitle = stringResource(R.string.mail_settings_biometrics_subtitle_app_locked),
        negativeButtonText = stringResource(R.string.mail_settings_biometrics_button_negative),
        onAuthenticationError = { _, _ ->
            onCloseAll()
        },
        onAuthenticationFailed = {},
        onAuthenticationSucceeded = {
            onClose()
        }
    )

    biometricPrompt.authenticate()
}
