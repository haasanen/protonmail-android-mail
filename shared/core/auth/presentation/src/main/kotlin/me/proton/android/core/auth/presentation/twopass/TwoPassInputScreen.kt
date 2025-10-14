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

package me.proton.android.core.auth.presentation.twopass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.twopass.TwoPassInputState.Error
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.util.LaunchOnScreenView

@Composable
fun TwoPassInputScreen(
    onClose: () -> Unit,
    onError: (String?) -> Unit,
    onForgotPassword: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TwoPassInputViewModel = hiltViewModel(),
    externalAction: StateFlow<TwoPassInputAction?> = MutableStateFlow(null)
) {
    val action by externalAction.collectAsStateWithLifecycle()
    action?.let { viewModel.submit(it) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    TwoPassInputScreen(
        state = state,
        modifier = modifier,
        onClose = onClose,
        onError = onError,
        onForgotPassword = onForgotPassword,
        onSuccess = onSuccess,
        onUnlock = { viewModel.submit(it) },
        onBackClicked = { viewModel.submit(it) },
        onScreenView = viewModel::onScreenView
    )
}

@Composable
fun TwoPassInputScreen(
    state: TwoPassInputState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onUnlock: (TwoPassInputAction.Unlock) -> Unit = {},
    onBackClicked: (TwoPassInputAction.Close) -> Unit = {},
    onScreenView: () -> Unit = {}
) {
    val isLoading = state.isLoading
    var mailboxPassword by remember { mutableStateOf("") }
    val passwordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state) {
        when (state) {
            is Error.LoginFlow -> onError(state.error)
            is TwoPassInputState.Success -> {
                onSuccess()
            }

            is TwoPassInputState.Closed -> onClose()
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        passwordFocusRequester.requestFocus()
    }

    LaunchOnScreenView(enqueue = onScreenView)

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onBackClicked(TwoPassInputAction.Close) }) {
                        Icon(
                            painterResource(R.drawable.ic_proton_arrow_back),
                            contentDescription = stringResource(id = R.string.presentation_back)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
                Text(
                    style = LocalTypography.current.headline,
                    text = stringResource(R.string.auth_two_pass_title)
                )
                ProtonPasswordOutlinedTextFieldWithError(
                    text = mailboxPassword,
                    onValueChanged = { mailboxPassword = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions { onUnlock(TwoPassInputAction.Unlock(mailboxPassword)) },
                    label = { Text(text = stringResource(R.string.auth_two_pass_input_label)) },
                    enabled = !isLoading,
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .focusRequester(passwordFocusRequester),
                    errorText = when {
                        state is Error.PasswordIsEmpty -> stringResource(R.string.auth_two_pass_input_empty)
                        else -> null
                    }
                )
                ProtonSolidButton(
                    onClick = { onUnlock(TwoPassInputAction.Unlock(mailboxPassword)) },
                    contained = false,
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight),
                    loading = isLoading
                ) {
                    Text(text = stringResource(R.string.auth_two_pass_unlock))
                }
                ProtonTextButton(
                    onClick = onForgotPassword,
                    enabled = !isLoading,
                    contained = false,
                    modifier = Modifier
                        .padding(top = ProtonDimens.SmallSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight)
                ) {
                    Text(text = stringResource(R.string.auth_two_pass_forgot_password))
                }
            }
        }
    }
}

@Composable
@Preview
private fun TwoPassInputScreenPreview() {
    ProtonTheme {
        TwoPassInputScreen(
            state = TwoPassInputState.Idle
        )
    }
}
