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

package me.proton.android.core.auth.presentation.signup.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.signup.CreateRecoveryAction.DialogAction.RecoverySkipped
import me.proton.android.core.auth.presentation.signup.CreateRecoveryAction.DialogAction.WantSkipDialogClosed
import me.proton.android.core.auth.presentation.signup.CreateRecoveryState.SkipFailed
import me.proton.android.core.auth.presentation.signup.CreateRecoveryState.SkipSuccess
import me.proton.android.core.auth.presentation.signup.CreateRecoveryState.WantSkip
import me.proton.android.core.auth.presentation.signup.viewmodel.SignUpViewModel
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun CreateRecoverySkipDialog(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = { },
    onSkip: (String) -> Unit = { },
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentState = state
    LaunchedEffect(currentState) {
        when (currentState) {
            is SkipSuccess -> onSkip(currentState.route)
            is SkipFailed -> onCloseClicked()
            else -> Unit
        }
    }

    when (state) {
        is WantSkip -> {
            CreateRecoverySkipDialog(
                modifier = modifier,
                onCloseClicked = { viewModel.perform(WantSkipDialogClosed) },
                onSkipClicked = { viewModel.perform(RecoverySkipped) }
            )
        }

        else -> Unit
    }

}

@Composable
fun CreateRecoverySkipDialog(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = { },
    onSkipClicked: () -> Unit = { }
) {
    ProtonAlertDialog(
        modifier = modifier,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            securePolicy = SecureFlagPolicy.Inherit
        ),
        title = stringResource(R.string.auth_signup_skip_recovery_title),
        text = { ProtonAlertDialogText(stringResource(R.string.auth_signup_skip_recovery_description)) },
        onDismissRequest = { },
        confirmButton = {
            Column {
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    title = stringResource(R.string.auth_signup_skip_recovery),
                    onClick = onSkipClicked,
                    loading = false
                )
            }
        },
        dismissButton = {
            Column {
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    title = stringResource(R.string.auth_signup_set_recovery),
                    onClick = onCloseClicked,
                    loading = false
                )
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun ChangePasswordDialogPreview() {
    ProtonTheme {
        CreateRecoverySkipDialog()
    }
}
