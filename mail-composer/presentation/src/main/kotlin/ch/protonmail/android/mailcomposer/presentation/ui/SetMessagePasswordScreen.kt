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

package ch.protonmail.android.mailcomposer.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonOutlinedButton
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.compose.HyperlinkText
import ch.protonmail.android.mailcommon.presentation.ui.CommonTestTags
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.MessagePasswordOperation
import ch.protonmail.android.mailcomposer.presentation.model.SetMessagePasswordState
import ch.protonmail.android.mailcomposer.presentation.viewmodel.SetMessagePasswordViewModel
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost
import ch.protonmail.android.uicomponents.thenIf

@Composable
fun SetMessagePasswordScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetMessagePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SetMessagePasswordScreen(
        modifier,
        state,
        SetMessagePasswordContent.Actions(
            validatePassword = { password ->
                viewModel.submit(MessagePasswordOperation.Action.ValidatePassword(password))
            },
            onApplyButtonClick = { messagePassword, messagePasswordHint ->
                viewModel.submit(MessagePasswordOperation.Action.ApplyPassword(messagePassword, messagePasswordHint))
            },
            onRemoveButtonClick = {
                viewModel.submit(MessagePasswordOperation.Action.RemovePassword)
            },
            onBackClick = { onBackClick() }
        )
    )
}

@Composable
private fun SetMessagePasswordScreen(
    modifier: Modifier,
    state: SetMessagePasswordState,
    actions: SetMessagePasswordContent.Actions
) {
    val snackBarHostState = remember { ProtonSnackbarHostState() }
    val messagePasswordTextFieldState = rememberTextFieldState()
    val messagePasswordHintTextFieldState = rememberTextFieldState()

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    SetPasswordTopBarTitle(
                        onApplyButtonClick = { pass, hint -> actions.onApplyButtonClick(pass, hint) },
                        messagePasswordTextFieldState = messagePasswordTextFieldState,
                        messagePasswordHintTextFieldState = messagePasswordHintTextFieldState
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = stringResource(id = R.string.presentation_close),
                            tint = ProtonTheme.colors.iconNorm
                        )
                    }
                }
            )
        },
        snackbarHost = {
            DismissableSnackbarHost(
                modifier = Modifier.testTag(CommonTestTags.SnackbarHost),
                protonSnackbarHostState = snackBarHostState
            )
        }
    ) { paddingValues ->
        when (state) {
            is SetMessagePasswordState.Loading -> ProtonCenteredProgress()
            is SetMessagePasswordState.Data -> {

                LaunchedEffect(state.initialMessagePasswordValue) {
                    messagePasswordTextFieldState.edit { append(state.initialMessagePasswordValue) }
                }
                LaunchedEffect(state.initialMessagePasswordHintValue) {
                    messagePasswordHintTextFieldState.edit { append(state.initialMessagePasswordHintValue) }
                }

                SetMessagePasswordContent(
                    modifier = Modifier.padding(paddingValues),
                    state = state,
                    messagePasswordTextFieldState = messagePasswordTextFieldState,
                    messagePasswordHintTextFieldState = messagePasswordHintTextFieldState,
                    actions = actions
                )

                ConsumableTextEffect(state.error) { string ->
                    snackBarHostState.showSnackbar(ProtonSnackbarType.ERROR, message = string)
                }

                ConsumableLaunchedEffect(effect = state.exitScreen) {
                    actions.onBackClick()
                }
            }
        }
    }
}

@Composable
private fun SetPasswordTopBarTitle(
    onApplyButtonClick: (String, String) -> Unit,
    messagePasswordTextFieldState: TextFieldState,
    messagePasswordHintTextFieldState: TextFieldState
) {
    val isSaveEnabled = remember {
        derivedStateOf {
            messagePasswordTextFieldState.text.length >= SetMessagePasswordViewModel.MIN_PASSWORD_LENGTH
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = ProtonDimens.Spacing.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.set_message_password_title),
            style = ProtonTheme.typography.titleMediumNorm
        )
        Button(
            onClick = {
                onApplyButtonClick(
                    messagePasswordTextFieldState.text.toString(),
                    messagePasswordHintTextFieldState.text.toString()
                )
            },
            enabled = isSaveEnabled.value,
            modifier = Modifier
                .thenIf(!isSaveEnabled.value) { semantics { disabled() } },
            shape = ProtonTheme.shapes.huge,
            colors = ButtonDefaults.buttonColors(
                containerColor = ProtonTheme.colors.interactionBrandWeakNorm,
                disabledContainerColor = ProtonTheme.colors.interactionBrandWeakDisabled,
                contentColor = ProtonTheme.colors.textAccent,
                disabledContentColor = ProtonTheme.colors.brandMinus20
            ),
            contentPadding = PaddingValues(
                horizontal = ProtonDimens.Spacing.Large,
                vertical = ProtonDimens.Spacing.Standard
            )
        ) {
            Text(
                text = stringResource(R.string.set_message_password_button_save_changes),
                style = ProtonTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun SetMessagePasswordContent(
    state: SetMessagePasswordState.Data,
    messagePasswordTextFieldState: TextFieldState,
    messagePasswordHintTextFieldState: TextFieldState,
    actions: SetMessagePasswordContent.Actions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProtonTheme.colors.backgroundNorm)
            .verticalScroll(rememberScrollState(), reverseScrolling = true)
            .padding(ProtonDimens.Spacing.Large)
    ) {

        MessagePasswordInfo()
        MessagePasswordSpacer()
        PasswordInputField(
            titleRes = R.string.set_message_password_label,
            supportingTextRes = if (state.hasMessagePasswordError) {
                R.string.set_message_password_supporting_error_text
            } else {
                R.string.set_message_password_supporting_text
            },
            textFieldState = messagePasswordTextFieldState,
            showTrailingIcon = true,
            isError = state.hasMessagePasswordError
        )
        MessagePasswordSpacer()
        PasswordInputField(
            titleRes = R.string.set_message_password_label_hint,
            supportingTextRes = null,
            textFieldState = messagePasswordHintTextFieldState,
            showTrailingIcon = false,
            isError = false
        )
        MessagePasswordSpacer(height = ProtonDimens.Spacing.Jumbo)
        MessagePasswordButtons(
            shouldShowEditingButtons = state.isInEditMode,
            onRemoveButtonClick = actions.onRemoveButtonClick
        )
    }
}

@Composable
private fun MessagePasswordInfo(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Column {
            Text(
                text = stringResource(id = R.string.set_message_password_info_message),
                style = ProtonTheme.typography.bodyMediumWeak
            )
            HyperlinkText(
                textResource = R.string.set_message_password_info_link,
                textStyle = ProtonTheme.typography.bodyMediumNorm,
                linkTextColor = ProtonTheme.colors.interactionBrandDefaultNorm
            )
        }
    }
}

@Composable
private fun MessagePasswordButtons(shouldShowEditingButtons: Boolean, onRemoveButtonClick: () -> Unit) {
    if (shouldShowEditingButtons) {
        MessagePasswordSpacer(height = ProtonDimens.Spacing.Large)
        ProtonOutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProtonDimens.DefaultButtonMinHeight),
            onClick = onRemoveButtonClick
        ) {
            Text(
                text = stringResource(id = R.string.set_message_password_button_remove_password),
                style = ProtonTheme.typography.bodyLargeNorm,
                color = ProtonTheme.colors.interactionBrandDefaultNorm
            )
        }
    }
}

@Composable
private fun MessagePasswordSpacer(modifier: Modifier = Modifier, height: Dp = ProtonDimens.Spacing.ExtraLarge) {
    Spacer(modifier = modifier.height(height))
}

object SetMessagePasswordContent {
    data class Actions(
        val validatePassword: (String) -> Unit,
        val onApplyButtonClick: (String, String?) -> Unit,
        val onRemoveButtonClick: () -> Unit,
        val onBackClick: () -> Unit
    )
}

@Preview
@Composable
fun MessagePasswordPreview() {
    SetMessagePasswordScreen(
        modifier = Modifier,
        state = SetMessagePasswordState.Data(
            initialMessagePasswordValue = "",
            initialMessagePasswordHintValue = "",
            hasMessagePasswordError = false,
            isInEditMode = false,
            exitScreen = Effect.empty(),
            error = Effect.empty()
        ),
        SetMessagePasswordContent.Actions(
            onBackClick = {},
            validatePassword = {},
            onApplyButtonClick = { _, _ -> },
            onRemoveButtonClick = {}
        )
    )
}
