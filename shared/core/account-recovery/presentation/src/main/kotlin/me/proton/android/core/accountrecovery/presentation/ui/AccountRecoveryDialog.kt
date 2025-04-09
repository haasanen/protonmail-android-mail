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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.DeferredCircularProgressIndicator
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountrecovery.presentation.R
import me.proton.android.core.accountrecovery.presentation.viewmodel.AccountRecoveryDialogViewModel
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.utils.StringBox
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.util.kotlin.exhaustive
import kotlin.time.Duration.Companion.milliseconds

internal const val PASSWORD_FIELD_TAG = "PASSWORD_FIELD_TAG" // gitleaks:allow

@Suppress("UseComposableActions")
@Composable
fun AccountRecoveryDialog(
    modifier: Modifier = Modifier,
    viewModel: AccountRecoveryDialogViewModel = hiltViewModel(),
    onStartPasswordManager: (CoreUserId) -> Unit,
    onClosed: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        lifecycleOwner.launchOnScreenView(savedStateRegistryOwner.savedStateRegistry) {
            // add observability
        }
    }

    LaunchedEffect(state) {
        when (val current = state) {
            is AccountRecoveryViewState.Loading -> Unit
            is AccountRecoveryViewState.Closed -> onClosed(current.hasCancelledSuccessfully)
            is AccountRecoveryViewState.Error -> onError(current.message)
            is AccountRecoveryViewState.StartPasswordManager -> onStartPasswordManager(current.userId)
            is AccountRecoveryViewState.Opened -> Unit
        }
    }

    AccountRecoveryDialog(
        modifier = modifier,
        state = state,
        onDismiss = { viewModel.userAcknowledged() }
    )
}

@Composable
fun AccountRecoveryDialog(
    modifier: Modifier = Modifier,
    state: AccountRecoveryViewState,
    onDismiss: () -> Unit = {}
) {
    when (state) {
        is AccountRecoveryViewState.Error,
        is AccountRecoveryViewState.Closed,
        is AccountRecoveryViewState.StartPasswordManager -> Unit

        is AccountRecoveryViewState.Loading -> {
            AccountRecoveryDialog(
                modifier = modifier,
                onDismiss = onDismiss,
                isLoading = true
            )
        }

        is AccountRecoveryViewState.Opened.GracePeriodStarted -> {
            AccountRecoveryGracePeriodDialog(
                modifier = modifier,
                email = state.email,
                remainingHours = state.remainingHours,
                onShowCancellationForm = state.onShowCancellationForm,
                onDismiss = onDismiss
            )
        }

        is AccountRecoveryViewState.Opened.CancellationHappened ->
            AccountRecoveryCancelledDialog(
                modifier,
                onDismiss = onDismiss
            )

        is AccountRecoveryViewState.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated -> {
            AccountRecoveryPasswordPeriodStartedDialog(
                modifier = modifier,
                endDate = state.endDate,
                onShowCancellationForm = state.onShowCancellationForm,
                onDismiss = onDismiss
            )
        }

        is AccountRecoveryViewState.Opened.PasswordChangePeriodStarted.SelfInitiated -> {
            AccountRecoveryPasswordPeriodStartedSelfInitiatedDialog(
                modifier = modifier,
                endDate = state.endDate,
                onShowResetForm = state.onShowPasswordChangeForm,
                onShowCancellationForm = state.onShowCancellationForm,
                onDismiss = onDismiss
            )
        }

        is AccountRecoveryViewState.Opened.CancelPasswordReset -> {
            AccountRecoveryCancellationForm(
                modifier = modifier,
                isProcessing = state.processing,
                passwordError = state.passwordError,
                onCancelPasswordRequest = state.onCancelPasswordRequest,
                onBack = state.onBack
            )
        }

        is AccountRecoveryViewState.Opened.RecoveryEnded ->
            AccountRecoveryWindowEndedDialog(
                modifier = modifier,
                email = state.email,
                onDismiss = onDismiss
            )
    }.exhaustive
}

// region all recovery dialog types
@Composable
internal fun AccountRecoveryGracePeriodDialog(
    modifier: Modifier = Modifier,
    email: String,
    remainingHours: Int,
    onShowCancellationForm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_grace_period_info_title),
        subtitle = pluralStringResource(
            id = R.plurals.account_recovery_grace_period_info_subtitle,
            remainingHours,
            email,
            remainingHours
        ),
        actionText = stringResource(id = R.string.account_recovery_cancel),
        dismissText = stringResource(id = R.string.account_recovery_dismiss),
        onAction = onShowCancellationForm,
        onDismiss = onDismiss
    )
}

@Composable
internal fun AccountRecoveryCancellationForm(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    passwordError: StringBox? = null,
    onCancelPasswordRequest: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val password = remember { mutableStateOf("") }

    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_cancel_title),
        isActionButtonLoading = isProcessing,
        subtitle = stringResource(id = R.string.account_recovery_cancel_subtitle),
        actionText = stringResource(id = R.string.account_recovery_cancel_now),
        dismissText = stringResource(id = R.string.account_recovery_cancel_back),
        onAction = { onCancelPasswordRequest(password.value) },
        onDismiss = onBack,
        password = password,
        passwordError = passwordError
    )
}

@Composable
internal fun AccountRecoveryCancelledDialog(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_cancelled_title),
        subtitle = stringResource(id = R.string.account_recovery_cancelled_subtitle),
        dismissText = stringResource(id = R.string.presentation_close),
        onDismiss = onDismiss
    )
}

@Suppress("FunctionMaxLength")
@Composable
internal fun AccountRecoveryPasswordPeriodStartedDialog(
    modifier: Modifier = Modifier,
    endDate: String,
    onShowCancellationForm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_password_started_title),
        subtitle = stringResource(
            id = R.string.account_recovery_password_started_subtitle,
            endDate
        ),
        actionText = stringResource(id = R.string.account_recovery_cancel),
        onAction = onShowCancellationForm,
        dismissText = stringResource(id = R.string.account_recovery_dismiss),
        onDismiss = onDismiss
    )
}

@Suppress("FunctionMaxLength", "UseComposableActions")
@Composable
internal fun AccountRecoveryPasswordPeriodStartedSelfInitiatedDialog(
    modifier: Modifier = Modifier,
    endDate: String,
    onShowResetForm: () -> Unit = {},
    onShowCancellationForm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_password_started_title),
        subtitle = stringResource(
            id = R.string.account_recovery_password_started_self_initiated_subtitle,
            endDate
        ),
        actionText = stringResource(id = R.string.account_recovery_reset),
        onAction = onShowResetForm,
        dismissText = stringResource(id = R.string.account_recovery_cancel),
        onDismiss = onDismiss,
        onDismissButton = onShowCancellationForm
    )
}

@Composable
internal fun AccountRecoveryWindowEndedDialog(
    modifier: Modifier = Modifier,
    email: String,
    onDismiss: () -> Unit
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_window_ended_title),
        subtitle = stringResource(
            id = R.string.account_recovery_window_ended_subtitle,
            email
        ),
        dismissText = stringResource(id = R.string.account_recovery_dismiss),
        onDismiss = onDismiss
    )
}
// endregion

@Suppress("UseComposableActions")
@Composable
private fun AccountRecoveryDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    isLoading: Boolean = false,
    isActionButtonLoading: Boolean = false,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = { },
    onDismissButton: () -> Unit = onDismiss,
    password: MutableState<String>? = null,
    passwordError: StringBox? = null
) {
    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onDismiss()
        },
        title = title,
        text = {
            if (isLoading)
                DeferredCircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "progressIndicator" },
                    defer = 0.milliseconds
                )
            else
                Column {
                    ProtonAlertDialogText(text = subtitle)
                    if (password != null) {
                        PasswordField(
                            password = password,
                            passwordError = passwordError,
                            enabled = !isActionButtonLoading
                        )
                    }
                }
        },
        confirmButton = {
            if (!actionText.isNullOrEmpty()) {
                ProtonAlertDialogButton(
                    onClick = {
                        if (onAction != null) {
                            onAction()
                        }
                    },
                    title = actionText,
                    loading = isActionButtonLoading,
                    enabled = true
                )
            }
        },
        dismissButton = {
            if (!dismissText.isNullOrEmpty()) {
                ProtonAlertDialogButton(
                    onClick = onDismissButton,
                    title = dismissText
                )
            }
        }
    )
}

@Composable
private fun PasswordField(
    password: MutableState<String>,
    passwordError: StringBox?,
    enabled: Boolean
) {
    me.proton.core.compose.component.ProtonOutlinedTextFieldWithError(
        text = password.value,
        onValueChanged = { password.value = it },
        enabled = enabled,
        errorText = passwordError?.get(LocalContext.current),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        label = {
            Text(text = stringResource(id = R.string.account_recovery_cancel_password_label))
        },
        maxLines = 1,
        placeholder = {
            Text(text = stringResource(id = R.string.account_recovery_cancel_password_placeholder))
        },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DefaultSpacing)
            .testTag(PASSWORD_FIELD_TAG)
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("FunctionMaxLength")
fun AccountRecoveryAlertDialogGracePeriodPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewState.Opened.GracePeriodStarted(email = "user@email.test", remainingHours = 24),
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("FunctionMaxLength")
fun AccountRecoveryAlertDialogGracePeriodProcessingPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewState.Opened.CancelPasswordReset(processing = true),
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("FunctionMaxLength")
fun AccountRecoveryAlertDialogChangePasswordPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state =
            AccountRecoveryViewState.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(endDate = "16 Aug"),
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("FunctionMaxLength")
fun AccountRecoveryAlertDialogChangePasswordSelfInitiatedPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewState.Opened.PasswordChangePeriodStarted.SelfInitiated(endDate = "16 Aug"),
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccountRecoveryAlertDialogLoadingPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewState.Loading,
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("FunctionMaxLength")
fun AccountRecoveryAlertDialogCancellationHappenedPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewState.Opened.CancellationHappened,
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("FunctionMaxLength")
fun AccountRecoveryAlertDialogRecoveryEndedPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewState.Opened.RecoveryEnded("user@email.test"),
            onDismiss = { }
        )
    }
}
