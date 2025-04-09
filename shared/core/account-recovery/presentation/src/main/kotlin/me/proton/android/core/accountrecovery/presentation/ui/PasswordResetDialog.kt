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

package me.proton.android.core.accountrecovery.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.viewmodel.hiltViewModelOrNull
import me.proton.android.core.accountrecovery.presentation.R
import me.proton.android.core.accountrecovery.presentation.viewmodel.PasswordResetDialogViewModel
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHint
import me.proton.core.compose.theme.defaultStrongNorm

@Suppress("UseComposableActions")
@Composable
fun PasswordResetDialog(
    modifier: Modifier = Modifier,
    onRecoveryMethod: () -> Unit = { },
    onDismiss: () -> Unit = { },
    onError: (String?) -> Unit = { },
    onSuccess: () -> Unit = { },
    viewModel: PasswordResetDialogViewModel? = hiltViewModelOrNull()
) {
    val state = when (viewModel) {
        null -> PasswordResetDialogViewState.Ready("example@domain.com")
        else -> viewModel.state.collectAsStateWithLifecycle().value
    }

    PasswordResetDialog(
        modifier = modifier,
        onRequestReset = { viewModel?.perform(PasswordResetDialogAction.RequestReset) },
        onRecoveryMethod = onRecoveryMethod,
        onDismiss = onDismiss,
        onError = onError,
        onSuccess = onSuccess,
        state = state
    )
}

@Suppress("UseComposableActions")
@Composable
fun PasswordResetDialog(
    modifier: Modifier = Modifier,
    onRequestReset: () -> Unit = { },
    onRecoveryMethod: () -> Unit = { },
    onDismiss: () -> Unit = { },
    onError: (String?) -> Unit = { },
    onSuccess: () -> Unit = { },
    state: PasswordResetDialogViewState
) {
    val (email, isLoading) = remember(state) {
        when (state) {
            is PasswordResetDialogViewState.Loading -> state.email to true
            is PasswordResetDialogViewState.Ready -> state.email to false
            else -> null to false
        }
    }

    when (state) {
        is PasswordResetDialogViewState.Error -> onError(state.message)
        is PasswordResetDialogViewState.Loading,
        is PasswordResetDialogViewState.Ready -> PasswordResetDialog(
            modifier = modifier,
            email = email ?: "...",
            onRequestReset = onRequestReset,
            onRecoveryMethod = onRecoveryMethod,
            onDismiss = onDismiss,
            isRequestResetLoading = isLoading
        )

        is PasswordResetDialogViewState.ResetRequested -> onSuccess()
    }
}

@Suppress("UseComposableActions")
@Composable
private fun PasswordResetDialog(
    modifier: Modifier = Modifier,
    email: String,
    onRequestReset: () -> Unit = { },
    onRecoveryMethod: () -> Unit = { },
    onDismiss: () -> Unit = { },
    isRequestResetLoading: Boolean = false
) {
    me.proton.core.compose.component.ProtonAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.account_recovery_reset_dialog_title),
        text = {
            Column {
                Spacer(modifier = Modifier.size(ProtonDimens.DefaultSpacing))
                me.proton.core.compose.component.ProtonAlertDialogText(
                    stringResource(R.string.account_recovery_reset_dialog_text, email)
                )
                Spacer(modifier = Modifier.size(ProtonDimens.LargeSpacing))
                Card(
                    onClick = onRecoveryMethod,
                    colors = CardDefaults.cardColors(
                        containerColor = ProtonTheme.colors.backgroundSecondary,
                        contentColor = ProtonTheme.colors.textNorm
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(ProtonDimens.SmallSpacing)
                    ) {
                        Row {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.account_recovery_reset_dialog_action_use_recovery),
                                style = ProtonTheme.typography.defaultStrongNorm
                            )
                            Icon(
                                modifier = Modifier.size(ProtonDimens.DefaultIconSize),
                                painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
                                contentDescription = "",
                                tint = ProtonTheme.colors.iconHint
                            )
                        }
                        Spacer(modifier = Modifier.size(ProtonDimens.SmallSpacing))
                        Text(
                            text = stringResource(R.string.account_recovery_reset_dialog_action_use_recovery_hint),
                            style = ProtonTheme.typography.defaultHint
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Column {
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    title = stringResource(R.string.account_recovery_reset_dialog_action_request_reset),
                    onClick = onRequestReset,
                    loading = isRequestResetLoading
                )
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    title = stringResource(id = R.string.account_recovery_dismiss),
                    onClick = onDismiss
                )
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun PreviewPasswordResetDialog() {
    ProtonTheme {
        PasswordResetDialog(email = "example@domain.com")
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
internal fun PreviewPasswordResetDialogLoading() {
    ProtonTheme {
        PasswordResetDialog(
            email = "example@domain.com",
            isRequestResetLoading = true
        )
    }
}
