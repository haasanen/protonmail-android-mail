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

@file:Suppress("UseComposableActions")

package me.proton.android.core.auth.presentation.secondfactor.otp

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun OneTimePasswordInputForm(
    onError: (String?) -> Unit,
    onSuccess: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OneTimePasswordInputViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OneTimePasswordInputForm(
        state = state,
        modifier = modifier,
        onAuthenticate = { viewModel.perform(it) },
        onError = onError,
        onSuccess = onSuccess,
        onClose = onClose
    )
}

@Composable
fun OneTimePasswordInputForm(
    state: OneTimePasswordInputState,
    modifier: Modifier = Modifier,
    onAuthenticate: (OneTimePasswordInputAction.Authenticate) -> Unit = {},
    onError: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    onClose: () -> Unit = {},
    initialMode: OneTimePasswordInputMode = OneTimePasswordInputMode.Totp
) {
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var code by rememberSaveable { mutableStateOf("") }
    val isLoading = state.isLoading
    val twoFactorCodeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state) {
        when (state) {
            is OneTimePasswordInputState.Error.LoginFlow -> onError(state.error)
            is OneTimePasswordInputState.Awaiting2Pass -> onSuccess()
            is OneTimePasswordInputState.LoggedIn -> onSuccess()
            is OneTimePasswordInputState.Closed -> {
                if (state.message != null) onError(state.message)
                onClose()
            }

            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        twoFactorCodeFocusRequester.requestFocus()
    }

    Column(modifier = modifier.padding(top = ProtonDimens.DefaultSpacing)) {
        if (mode == OneTimePasswordInputMode.Totp) {
            ProtonOutlinedTextFieldWithError(
                text = code,
                onValueChanged = { code = it },
                singleLine = true,
                helpText = stringResource(R.string.auth_second_factor_totp_input_help),
                label = { Text(text = stringResource(R.string.auth_second_factor_totp_input_label)) },
                enabled = !isLoading,
                modifier = Modifier.focusRequester(twoFactorCodeFocusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )
        } else if (mode == OneTimePasswordInputMode.RecoveryCode) {
            ProtonOutlinedTextFieldWithError(
                text = code,
                onValueChanged = { code = it },
                singleLine = true,
                helpText = stringResource(R.string.auth_second_factor_recovery_help),
                label = { Text(text = stringResource(R.string.auth_second_factor_recovery_label)) },
                enabled = !isLoading,
                modifier = Modifier.focusRequester(twoFactorCodeFocusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        Spacer(modifier = Modifier.padding(top = ProtonDimens.MediumSpacing))

        ProtonSolidButton(
            onClick = { onAuthenticate(OneTimePasswordInputAction.Authenticate(code, mode)) },
            contained = false,
            modifier = Modifier.height(ProtonDimens.DefaultButtonMinHeight),
            loading = isLoading
        ) {
            Text(text = stringResource(R.string.auth_second_factor_authenticate))
        }
        ProtonTextButton(
            onClick = {
                mode = when (mode) {
                    OneTimePasswordInputMode.Totp -> OneTimePasswordInputMode.RecoveryCode
                    OneTimePasswordInputMode.RecoveryCode -> OneTimePasswordInputMode.Totp
                }
            },
            contained = false,
            modifier = Modifier
                .padding(top = ProtonDimens.SmallSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
            enabled = !isLoading
        ) {
            Text(
                text = stringResource(
                    when (mode) {
                        OneTimePasswordInputMode.Totp -> R.string.auth_second_factor_switch_to_recovery
                        OneTimePasswordInputMode.RecoveryCode -> R.string.auth_second_factor_switch_to_totp
                    }
                )
            )
        }
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
fun OneTimePasswordInputFormTotpPreview() {
    ProtonTheme {
        OneTimePasswordInputForm(
            state = OneTimePasswordInputState.Idle,
            initialMode = OneTimePasswordInputMode.Totp
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
fun OneTimePasswordInputFormRecoveryPreview() {
    ProtonTheme {
        OneTimePasswordInputForm(
            state = OneTimePasswordInputState.Idle,
            initialMode = OneTimePasswordInputMode.RecoveryCode
        )
    }
}
