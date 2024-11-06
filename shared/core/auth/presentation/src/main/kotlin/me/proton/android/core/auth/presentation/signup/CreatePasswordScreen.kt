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

package me.proton.android.core.auth.presentation.signup

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
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
    onSuccess: (String) -> Unit = {},
    viewModel: CreatePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreatePasswordScreen(
        modifier = modifier,
        onBackClicked = onBackClicked,
        onPasswordSubmitted = { viewModel.submit(it) },
        onErrorMessage = onErrorMessage,
        onSuccess = {
            onSuccess(it)
            viewModel.submit(CreatePasswordAction.SetNavigationDone)
        },
        state = state
    )
}

@Composable
fun CreatePasswordScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onPasswordSubmitted: (CreatePasswordAction.Submit) -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: (String) -> Unit = {},
    state: CreatePasswordState = CreatePasswordState.Idle
) {
    LaunchedEffect(state) {
        when (state) {
            is CreatePasswordState.FormError -> onErrorMessage(state.message)
            is CreatePasswordState.Success -> onSuccess(state.password)
            else -> Unit
        }
    }

    val isLoading = state is CreatePasswordState.Loading
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val passwordError = (state as? CreatePasswordState.FormError)?.message
    val confirmPasswordError = (state as? CreatePasswordState.FormError)?.message

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painterResource(id = me.proton.core.presentation.R.drawable.ic_proton_close),
                            contentDescription = stringResource(id = R.string.auth_login_close)
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
        ) {
            Column(
                modifier = Modifier.padding(ProtonDimens.DefaultSpacing),
            ) {
                Text(
                    style = LocalTypography.current.headline,
                    text = stringResource(id = R.string.auth_signup_create_password)
                )

                ProtonPasswordOutlinedTextFieldWithError(
                    text = password,
                    onValueChanged = { password = it },
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.auth_signup_password)) },
                    errorText = passwordError,
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )

                ProtonPasswordOutlinedTextFieldWithError(
                    text = confirmPassword,
                    onValueChanged = { confirmPassword = it },
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.auth_signup_repeat_password)) },
                    errorText = confirmPasswordError,
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )
                ProtonSolidButton(
                    contained = false,
                    loading = isLoading,
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight),
                    onClick = { onPasswordSubmitted(CreatePasswordAction.Submit(password, confirmPassword)) }
                ) {
                    Text(text = stringResource(id = R.string.auth_signup_next))
                }

                TermsPolicyFooter()
            }
        }
    }
}

@Composable
fun TermsPolicyFooter() {
    LearnMoreText(text = R.string.auth_signup_terms_privacy_conditions_footer)
}

@Composable
fun LearnMoreText(
    @StringRes text: Int,
) {
    HyperlinkText(
        modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
        fullText = stringResource(
            id = text,
            stringResource(id = R.string.auth_signup_terms_learn_more),
            stringResource(id = R.string.auth_signup_privacy_policy_learn_more)
        ),
        hyperLinks = mutableMapOf(
            stringResource(id = R.string.auth_signup_terms_learn_more) to stringResource(id = R.string.sign_up_url_terms_and_conditions),
            stringResource(id = R.string.auth_signup_privacy_policy_learn_more) to stringResource(id = R.string.sign_up_url_privacy_policy),
            ),
        textStyle = LocalTypography.current.body2Regular
    )
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun CreatePasswordScreenPreview() {
    ProtonTheme {
        CreatePasswordScreen()
    }
}
