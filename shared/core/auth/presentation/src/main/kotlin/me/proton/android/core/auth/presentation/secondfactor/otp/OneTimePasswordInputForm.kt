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

package me.proton.android.core.auth.presentation.secondfactor.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.viewmodel.hiltViewModelOrNull

@Composable
fun OneTimePasswordInputForm(
    onError: (Throwable) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OneTimePasswordInputViewModel? = hiltViewModelOrNull()
) {
    val mode by viewModel?.mode?.collectAsStateWithLifecycle()
        ?: remember { derivedStateOf { OneTimePasswordInputMode.Totp } }
    val state by viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { derivedStateOf { OneTimePasswordInputState.Idle } }

    OneTimePasswordInputForm(
        mode = mode,
        state = state,
        modifier = modifier,
        onAuthenticate = { viewModel?.submit(it) },
        onError = onError,
        onSuccess = onSuccess,
        onSwitchMode = { viewModel?.submit(it) }
    )
}

@Composable
fun OneTimePasswordInputForm(
    mode: OneTimePasswordInputMode,
    state: OneTimePasswordInputState,
    modifier: Modifier = Modifier,
    onAuthenticate: (OneTimePasswordInputAction.Authenticate) -> Unit = {},
    onError: (Throwable) -> Unit = {},
    onSuccess: () -> Unit = {},
    onSwitchMode: (OneTimePasswordInputAction.SwitchMode) -> Unit = {}
) {
    var code by remember { mutableStateOf("") }
    val isLoading = state is OneTimePasswordInputState.Loading

    LaunchedEffect(state) {
        when (state) {
            is OneTimePasswordInputState.Success -> onSuccess()
            is OneTimePasswordInputState.LoginError -> onError(state.cause)
            else -> Unit
        }
    }

    Column(modifier = modifier.background(LocalColors.current.backgroundNorm)) {
        if (mode == OneTimePasswordInputMode.Totp) {
            ProtonOutlinedTextFieldWithError(
                text = code,
                onValueChanged = { code = it },
                singleLine = true,
                // helpText = stringResource(R.string.auth_totp_input_help), // TODO: uncomment when available in Core
                label = { Text(text = stringResource(R.string.auth_second_factor_totp_input_label)) },
                enabled = !isLoading
            )
        } else if (mode == OneTimePasswordInputMode.RecoveryCode) {
            ProtonOutlinedTextFieldWithError(
                text = code,
                onValueChanged = { code = it },
                singleLine = true,
                // helpText = stringResource(R.string.auth_second_factor_recovery_help), // TODO: uncomment when available in Core
                label = { Text(text = stringResource(R.string.auth_second_factor_recovery_label)) },
                enabled = !isLoading
            )
        }

        ProtonSolidButton(
            onClick = { onAuthenticate(OneTimePasswordInputAction.Authenticate(code, mode)) },
            contained = false,
            modifier = Modifier
                .height(ProtonDimens.DefaultButtonMinHeight),
            loading = isLoading
        ) {
            Text(text = stringResource(R.string.auth_second_factor_authenticate))
        }
        ProtonTextButton(
            onClick = {
                val nextMode = when (mode) {
                    OneTimePasswordInputMode.Totp -> OneTimePasswordInputMode.RecoveryCode
                    OneTimePasswordInputMode.RecoveryCode -> OneTimePasswordInputMode.Totp
                }
                onSwitchMode(OneTimePasswordInputAction.SwitchMode(nextMode))
            },
            contained = false,
            modifier = Modifier
                .padding(top = ProtonDimens.SmallSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
            loading = isLoading
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

@Composable
@Preview
fun OneTimePasswordInputFormTotpPreview() {
    ProtonTheme {
        OneTimePasswordInputForm(
            mode = OneTimePasswordInputMode.Totp,
            state = OneTimePasswordInputState.Idle
        )
    }
}

@Composable
@Preview
fun OneTimePasswordInputFormRecoveryPreview() {
    ProtonTheme {
        OneTimePasswordInputForm(
            mode = OneTimePasswordInputMode.RecoveryCode,
            state = OneTimePasswordInputState.Idle
        )
    }
}
