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

package me.proton.android.core.auth.presentation.signup.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.android.core.auth.presentation.signup.CreatePasswordAction
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.Closed
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.Creating
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.Error
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.Idle
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.Success
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.ValidationError.ConfirmPasswordMissMatch
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.ValidationError.Other
import me.proton.android.core.auth.presentation.signup.CreatePasswordState.ValidationError.PasswordEmpty
import me.proton.android.core.auth.presentation.signup.SignUpState
import me.proton.android.core.auth.presentation.signup.viewmodel.SignUpViewModel
import me.proton.core.compose.component.HyperlinkText
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun CreatePasswordScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit,
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: (String) -> Unit = { },
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        viewModel.perform(CreatePasswordAction.CreatePasswordClosed(back = true))
    }

    when (state) {
        is Closed -> onBackClicked()
        else -> Unit
    }

    CreatePasswordContent(
        modifier = modifier,
        onBackClicked = {
            viewModel.perform(CreatePasswordAction.CreatePasswordClosed(back = true))
        },
        onPasswordSubmitted = { viewModel.perform(it) },
        onErrorMessage = onErrorMessage,
        onSuccess = { route -> onSuccess(route) },
        state = state
    )
}

@Composable
fun CreatePasswordContent(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onPasswordSubmitted: (CreatePasswordAction.Perform) -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: (String) -> Unit = {},
    state: SignUpState = Idle
) {
    val isLoading = state is Creating

    LaunchedEffect(state) {
        when (state) {
            is Error -> onErrorMessage(state.message)
            is Success -> onSuccess(state.route)
            else -> Unit
        }
    }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordError = when (state) {
        is PasswordEmpty -> stringResource(R.string.auth_signup_validation_password)
        is Other -> state.message ?: stringResource(R.string.auth_signup_validation_password_input_invalid)
        else -> null
    }

    val confirmPasswordError = when (state) {
        is ConfirmPasswordMissMatch -> stringResource(R.string.auth_signup_validation_passwords_do_not_match)
        is Other -> state.message ?: stringResource(R.string.auth_signup_validation_password_input_invalid)
        else -> null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    NavigationBackButton(onBackClicked = onBackClicked)
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(ProtonDimens.DefaultSpacing)
            ) {
                Text(
                    style = LocalTypography.current.headline,
                    text = stringResource(id = R.string.auth_signup_create_password)
                )

                PasswordField(
                    password = password,
                    onPasswordChanged = { password = it },
                    enabled = !isLoading,
                    errorText = passwordError,
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )

                PasswordConfirmationField(
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChanged = { confirmPassword = it },
                    enabled = !isLoading,
                    errorText = confirmPasswordError,
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )

                NextButton(
                    isLoading = isLoading,
                    onClick = {
                        onPasswordSubmitted(
                            CreatePasswordAction.Perform(
                                password = password,
                                confirmPassword = confirmPassword
                            )
                        )
                    },
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )

                TermsPolicyFooter()
            }
        }
    }
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChanged: (String) -> Unit,
    enabled: Boolean,
    errorText: String?,
    modifier: Modifier = Modifier
) {
    ProtonPasswordOutlinedTextFieldWithError(
        text = password,
        onValueChanged = onPasswordChanged,
        enabled = enabled,
        singleLine = true,
        label = { Text(text = stringResource(id = R.string.auth_signup_password)) },
        errorText = errorText,
        modifier = modifier
    )
}

@Composable
private fun PasswordConfirmationField(
    confirmPassword: String,
    onConfirmPasswordChanged: (String) -> Unit,
    enabled: Boolean,
    errorText: String?,
    modifier: Modifier = Modifier
) {
    ProtonPasswordOutlinedTextFieldWithError(
        text = confirmPassword,
        onValueChanged = onConfirmPasswordChanged,
        enabled = enabled,
        singleLine = true,
        label = { Text(text = stringResource(id = R.string.auth_signup_repeat_password)) },
        errorText = errorText,
        modifier = modifier
    )
}

@Composable
private fun NextButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProtonSolidButton(
        contained = false,
        loading = isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(ProtonDimens.DefaultButtonMinHeight),
        onClick = onClick
    ) {
        Text(text = stringResource(id = R.string.auth_signup_next))
    }
}

@Composable
fun TermsPolicyFooter() {
    LearnMoreText(text = R.string.auth_signup_terms_privacy_conditions_footer)
}

@Composable
fun LearnMoreText(@StringRes text: Int) {
    HyperlinkText(
        modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
        fullText = stringResource(
            id = text,
            stringResource(id = R.string.auth_signup_terms_learn_more),
            stringResource(id = R.string.auth_signup_privacy_policy_learn_more)
        ),
        hyperLinks = mutableMapOf(
            Pair(
                stringResource(id = R.string.auth_signup_terms_learn_more),
                stringResource(id = R.string.sign_up_url_terms_and_conditions)
            ),
            Pair(
                stringResource(id = R.string.auth_signup_privacy_policy_learn_more),
                stringResource(id = R.string.sign_up_url_privacy_policy)
            )
        ),
        textStyle = LocalTypography.current.body2Regular
    )
}


@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun CreatePasswordScreenPreview() {
    ProtonTheme {
        CreatePasswordContent()
    }
}
