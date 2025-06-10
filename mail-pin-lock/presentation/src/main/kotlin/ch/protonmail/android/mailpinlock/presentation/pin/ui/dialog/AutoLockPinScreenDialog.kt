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

package ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.DialogType
import ch.protonmail.android.mailpinlock.presentation.pin.ui.PinSecureInputField

@Composable
fun AutoLockPinScreenDialog(
    modifier: Modifier = Modifier,
    dialogType: DialogType,
    onNavigateBack: () -> Unit,
    onSuccessWithResult: (String) -> Unit,
    viewModel: AutoLockDialogPinViewModel = hiltViewModel<AutoLockDialogPinViewModel>()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    ConsumableLaunchedEffect(state.successEffect) {
        dialogType.resultKey?.let { onSuccessWithResult(it) }
            ?: onNavigateBack()
    }

    AutoLockPinScreenDialogContent(
        modifier = modifier,
        state = state,
        pinFieldState = viewModel.textFieldState,
        onProcessPin = { viewModel.processPin(dialogType) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun AutoLockPinScreenDialogContent(
    modifier: Modifier = Modifier,
    state: AutoLockDialogState,
    pinFieldState: TextFieldState,
    onProcessPin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isError = state.error != null

    ProtonAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.mail_settings_pin_lock_dialog_title),
        onDismissRequest = { onNavigateBack() },

        text = {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Column {
                Text(stringResource(R.string.mail_settings_pin_lock_dialog_subtitle))
                Spacer(Modifier.height(ProtonDimens.Spacing.Large))
                PinSecureInputField(
                    modifier = Modifier
                        .padding(horizontal = ProtonDimens.Spacing.ExtraLarge)
                        .focusRequester(focusRequester),
                    pinTextFieldState = pinFieldState,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    maxLength = MAX_PIN_LENGTH,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = state.error.string(),
                                style = ProtonTheme.typography.bodySmallNorm,
                                color = ProtonTheme.colors.notificationError,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            ProtonAlertDialogButton(
                title = stringResource(R.string.mail_settings_pin_lock_dialog_button_next),
                enabled = pinFieldState.text.isNotEmpty()
            ) { onProcessPin() }
        },
        dismissButton = {
            ProtonAlertDialogButton(
                title = stringResource(R.string.mail_settings_pin_lock_dialog_button_cancel)
            ) {
                onNavigateBack()
            }
        }
    )
}

object AutoLockPinScreenDialogKeys {

    const val AutoLockPinDialogModeKey = "dialog_type"
    const val AutoLockPinDialogResultKey = "dialog_type_result"
}

private const val MAX_PIN_LENGTH = 21


@Composable
@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AutoLockPinScreenDialogPreview() {
    ProtonTheme {
        AutoLockPinScreenDialogContent(
            state = AutoLockDialogState(null, Effect.empty()),
            pinFieldState = TextFieldState(),
            onProcessPin = {},
            onNavigateBack = {}
        )
    }
}
