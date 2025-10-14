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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import ch.protonmail.android.design.compose.component.DeferredCircularProgressIndicator
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountrecovery.presentation.R
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryViewState.Closed
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryViewState.Error
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryViewState.Loading
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryViewState.Opened
import me.proton.android.core.accountrecovery.presentation.ui.AccountRecoveryViewState.StartPasswordManager
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
    onStartPasswordManager: (CoreUserId) -> Unit,
    onClosed: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    viewModel: AccountRecoveryDialogViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        lifecycleOwner.launchOnScreenView(savedStateRegistryOwner.savedStateRegistry) {
            viewModel.screenId.collect { screenId ->
                screenId?.let { viewModel.onScreenView(it) }
            }
        }
    }

    LaunchedEffect(state) {
        when (val current = state) {
            is Loading -> Unit
            is Closed -> onClosed(current.passwordResetCancelled)
            is Opened.Cancellation.Success -> onClosed(true)
            is Error -> onError(current.message)
            is Opened.Cancellation.Error -> onError(current.error)

            is StartPasswordManager -> onStartPasswordManager(current.userId)
            is Opened -> Unit
        }
    }

    AccountRecoveryDialog(
        modifier = modifier,
        state = state,
        onShowCancellationForm = { viewModel.perform(AccountRecoveryDialogAction.ShowCancellationForm) },
        onShowPasswordChangeForm = { viewModel.perform(AccountRecoveryDialogAction.ShowPasswordChangeForm) },
        onCancelPasswordRequest = { viewModel.perform(AccountRecoveryDialogAction.CancelPasswordRequest(it)) },
        onDismiss = { viewModel.perform(AccountRecoveryDialogAction.UserAcknowledged) }
    )
}

@Suppress("UseComposableActions")
@Composable
fun AccountRecoveryDialog(
    modifier: Modifier = Modifier,
    state: AccountRecoveryViewState,
    onShowCancellationForm: () -> Unit = {},
    onShowPasswordChangeForm: () -> Unit = {},
    onCancelPasswordRequest: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    when (state) {
        is Error,
        is Closed,
        is StartPasswordManager -> Unit

        is Loading ->
            BaseAccountRecoveryDialog(
                modifier = modifier,
                onDismiss = onDismiss,
                isLoading = true
            )

        is Opened.GracePeriodStarted ->
            GracePeriodDialog(
                modifier = modifier,
                email = state.email,
                remainingHours = state.remainingHours,
                onShowCancellationForm = onShowCancellationForm,
                onDismiss = onDismiss
            )

        is Opened.CancellationHappened ->
            CancelledDialog(
                modifier,
                onDismiss = onDismiss
            )

        is Opened.PasswordChangePeriodStarted.OtherDeviceInitiated -> {
            PasswordPeriodStartedDialog(
                modifier = modifier,
                endDate = state.endDate,
                onShowCancellationForm = onShowCancellationForm,
                onDismiss = onDismiss,
                isUserInitiated = false
            )
        }

        is Opened.PasswordChangePeriodStarted.SelfInitiated -> {
            PasswordPeriodStartedDialog(
                modifier = modifier,
                endDate = state.endDate,
                onShowResetForm = onShowPasswordChangeForm,
                onShowCancellationForm = onShowCancellationForm,
                onDismiss = onDismiss,
                isUserInitiated = true
            )
        }

        is Opened.Cancellation -> {
            when (state) {
                is Opened.Cancellation.Init -> {
                    CancellationForm(
                        modifier = modifier,
                        isProcessing = false,
                        passwordError = null,
                        onCancelPasswordRequest = onCancelPasswordRequest,
                        onDismiss = onDismiss
                    )
                }

                is Opened.Cancellation.Processing -> {
                    CancellationForm(
                        modifier = modifier,
                        isProcessing = true,
                        passwordError = null,
                        onCancelPasswordRequest = onCancelPasswordRequest,
                        onDismiss = onDismiss
                    )
                }

                is Opened.Cancellation.Error -> {
                    CancellationForm(
                        modifier = modifier,
                        isProcessing = false,
                        passwordError = state.passwordError,
                        onCancelPasswordRequest = onCancelPasswordRequest,
                        onDismiss = onDismiss
                    )
                }

                else -> Unit
            }
        }

        is Opened.RecoveryEnded ->
            AccountRecoveryWindowEndedDialog(
                modifier = modifier,
                email = state.email,
                onDismiss = onDismiss
            )
    }.exhaustive
}

// region all recovery dialog types
@Composable
internal fun GracePeriodDialog(
    modifier: Modifier = Modifier,
    email: String,
    remainingHours: Int,
    onShowCancellationForm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    BaseAccountRecoveryDialog(
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
internal fun CancellationForm(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    passwordError: StringBox? = null,
    onCancelPasswordRequest: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val password = remember { mutableStateOf("") }

    BaseAccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_cancel_title),
        isActionButtonLoading = isProcessing,
        subtitle = stringResource(id = R.string.account_recovery_cancel_subtitle),
        actionText = stringResource(id = R.string.account_recovery_cancel_now),
        dismissText = stringResource(id = R.string.account_recovery_cancel_back),
        onAction = { onCancelPasswordRequest(password.value) },
        onDismiss = onDismiss,
        password = password,
        passwordError = passwordError
    )
}

@Composable
internal fun CancelledDialog(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    BaseAccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_cancelled_title),
        subtitle = stringResource(id = R.string.account_recovery_cancelled_subtitle),
        dismissText = stringResource(id = R.string.presentation_close),
        onDismiss = onDismiss
    )
}

@Suppress("UseComposableActions")
@Composable
internal fun PasswordPeriodStartedDialog(
    modifier: Modifier = Modifier,
    endDate: String,
    onShowResetForm: (() -> Unit)? = null,
    onShowCancellationForm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    isUserInitiated: Boolean = false
) {
    BaseAccountRecoveryDialog(
        dismissText = stringResource(id = R.string.account_recovery_dismiss),
        onDismiss = onDismiss
    )

    BaseAccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_password_started_title),
        subtitle = stringResource(
            id = if (isUserInitiated) {
                R.string.account_recovery_password_started_self_initiated_subtitle
            } else {
                R.string.account_recovery_password_started_subtitle
            },
            endDate
        ),
        actionText = if (isUserInitiated) {
            stringResource(id = R.string.account_recovery_reset)
        } else {
            stringResource(id = R.string.account_recovery_cancel)
        },
        dismissText = if (isUserInitiated) {
            stringResource(id = R.string.account_recovery_cancel)
        } else {
            stringResource(id = R.string.account_recovery_dismiss)
        },
        onAction = onShowResetForm ?: onShowCancellationForm,
        onDismiss = onDismiss
    )
}

@Composable
internal fun AccountRecoveryWindowEndedDialog(
    modifier: Modifier = Modifier,
    email: String,
    onDismiss: () -> Unit
) {
    BaseAccountRecoveryDialog(
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
private fun BaseAccountRecoveryDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    isLoading: Boolean = false,
    isActionButtonLoading: Boolean = false,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = {},
    onDismissButton: () -> Unit = onDismiss,
    password: MutableState<String>? = null,
    passwordError: StringBox? = null
) {
    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = title,
        text = {
            when {
                isLoading -> DeferredCircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "progressIndicator" },
                    defer = 0.milliseconds
                )

                else -> DialogContent(
                    subtitle = subtitle,
                    password = password,
                    passwordError = passwordError,
                    isActionButtonLoading = isActionButtonLoading
                )
            }
        },
        confirmButton = {
            actionText?.let {
                ProtonAlertDialogButton(
                    onClick = { onAction?.invoke() },
                    title = it,
                    loading = isActionButtonLoading,
                    enabled = true
                )
            }
        },
        dismissButton = {
            dismissText?.let {
                ProtonAlertDialogButton(
                    onClick = onDismissButton,
                    title = it
                )
            }
        }
    )
}

@Composable
private fun DialogContent(
    subtitle: String,
    password: MutableState<String>?,
    passwordError: StringBox?,
    isActionButtonLoading: Boolean
) {
    Column {
        ProtonAlertDialogText(text = subtitle)
        password?.let {
            PasswordInputField(
                password = it,
                passwordError = passwordError,
                enabled = !isActionButtonLoading
            )
        }
    }
}

@Composable
private fun PasswordInputField(
    password: MutableState<String>,
    passwordError: StringBox?,
    enabled: Boolean
) {
    ProtonOutlinedTextFieldWithError(
        text = password.value,
        onValueChanged = { password.value = it },
        enabled = enabled,
        errorText = passwordError?.get(LocalContext.current),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        label = { Text(text = stringResource(id = R.string.account_recovery_cancel_password_label)) },
        placeholder = { Text(text = stringResource(id = R.string.account_recovery_cancel_password_placeholder)) },
        maxLines = 1,
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
            state = Opened.GracePeriodStarted(
                email = "user@email.test",
                remainingHours = 24
            ),
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
            state = Opened.Cancellation.Init,
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
            Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(endDate = "16 Aug"),
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
            state = Opened.PasswordChangePeriodStarted.SelfInitiated(endDate = "16 Aug"),
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
            state = Loading,
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
            state = Opened.CancellationHappened,
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
            state = Opened.RecoveryEnded("user@email.test"),
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("FunctionMaxLength")
fun AccountRecoveryCancellationFormPreview() {
    ProtonTheme {
        CancellationForm(
            onDismiss = { }
        )
    }
}
