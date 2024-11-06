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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak

internal const val USERNAME_FIELD_TAG = "USERNAME_FIELD_TAG"
internal const val EMAIL_FIELD_TAG = "EMAIL_FIELD_TAG"
internal const val PHONE_FIELD_TAG = "PHONE_FIELD_TAG"

@Composable
fun CreateUsernameScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: (String) -> Unit = {},
    viewModel: CreateUsernameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreateUsernameScreen(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onUsernameSubmitted = { viewModel.submit(it) },
        onCreateExternalClicked = { viewModel.submit(it) },
        onCreateInternalClicked = { viewModel.submit(it) },
        onErrorMessage = onErrorMessage,
        onSuccess = {
            onSuccess(it)
            viewModel.submit(CreateUsernameAction.SetNavigationDone)
        },
        state = state
    )
}

@Composable
fun CreateUsernameScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onUsernameSubmitted: (CreateUsernameAction.Submit) -> Unit = {},
    onCreateExternalClicked: (CreateUsernameAction.CreateExternalAccount) -> Unit = {},
    onCreateInternalClicked: (CreateUsernameAction.CreateInternalAccount) -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: (String) -> Unit = {},
    state: CreateUsernameState
) {
    LaunchedEffect(state) {
        when (state) {
            is CreateUsernameState.Error -> onErrorMessage(state.message)
            is CreateUsernameState.FormError -> onErrorMessage(state.message)
            is CreateUsernameState.Success -> onSuccess(state.username)
            else -> Unit
        }
    }
    ChooseUsernameScaffold(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onUsernameSubmitted = onUsernameSubmitted,
        onCreateExternalClicked = onCreateExternalClicked,
        onCreateInternalClicked = onCreateInternalClicked,
        state = state
    )
}

@Composable
fun ChooseUsernameScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onUsernameSubmitted: (CreateUsernameAction.Submit) -> Unit = {},
    onCreateExternalClicked: (CreateUsernameAction.CreateExternalAccount) -> Unit,
    onCreateInternalClicked: (CreateUsernameAction.CreateInternalAccount) -> Unit,
    @DrawableRes protonLogo: Int = R.drawable.ic_logo_proton,
    @StringRes titleText: Int = R.string.auth_signup_title,
    state: CreateUsernameState
) {
    val inputError = state is CreateUsernameState.FormError
    val isLoading = state.isLoading
    val accountType = state.accountType
    val domains = if (state is CreateUsernameState.Idle) state.domains else null

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onCloseClicked) {
                        Icon(
                            painterResource(id = R.drawable.ic_proton_close),
                            contentDescription = stringResource(id = R.string.auth_login_close)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .padding(top = ProtonDimens.SmallSpacing)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    modifier = Modifier
                        .height(64.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(protonLogo),
                    contentDescription = null,
                    alignment = Alignment.Center
                )

                Text(
                    text = stringResource(titleText),
                    style = ProtonTypography.Default.headline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = ProtonDimens.MediumSpacing)
                )

                when (accountType) {
                    AccountType.Internal ->
                        CreateInternalForm(
                            modifier = modifier,
                            enabled = !isLoading,
                            onUsernameSubmitted = onUsernameSubmitted,
                            onCreateExternalClicked = onCreateExternalClicked,
                            usernameError = if (inputError) stringResource(R.string.auth_signup_username_assistive_text) else null,
                            isLoading = isLoading,
                            domains = domains
                        )

                    AccountType.External ->
                        CreateExternalForm(
                            modifier = modifier,
                            enabled = !isLoading,
                            onExternalEmailSubmitted = onUsernameSubmitted,
                            onCreateInternalClicked = onCreateInternalClicked,
                            emailError = if (inputError) stringResource(R.string.auth_signup_email_assistive_text) else null
                        )
                }
            }
        }
    }
}

@Composable
private fun CreateInternalForm(
    modifier: Modifier = Modifier,
    onUsernameSubmitted: (CreateUsernameAction.Submit) -> Unit,
    onCreateExternalClicked: (CreateUsernameAction.CreateExternalAccount) -> Unit,
    @StringRes subtitleText: Int = R.string.auth_signup_subtitle,
    usernameError: String? = null,
    isLoading: Boolean = false,
    enabled: Boolean,
    domains: List<Domain>?
) {
    var username by rememberSaveable { mutableStateOf("") }
    var domain by rememberSaveable { mutableStateOf("") }
    Column {
        Text(
            text = stringResource(subtitleText),
            style = ProtonTypography.Default.defaultSmallWeak,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.SmallSpacing)
        )

        Column(
            modifier = modifier.padding(16.dp)
        ) {
            ProtonOutlinedTextFieldWithError(
                text = username,
                onValueChanged = { username = it },
                enabled = enabled,
                errorText = usernameError,
                label = { Text(text = stringResource(id = R.string.auth_signup_email_username)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DefaultSpacing)
                    .testTag(USERNAME_FIELD_TAG)
            )

            DomainDropDown(
                isLoading = isLoading,
                data = domains ?: emptyList(),
                onInputChanged = { domain = it }
            )

            ProtonSolidButton(
                contained = false,
                enabled = enabled,
                loading = isLoading,
                onClick = { onUsernameSubmitted(CreateUsernameAction.Submit(username, AccountType.Internal)) },
                modifier = Modifier
                    .padding(top = ProtonDimens.MediumSpacing)
                    .height(ProtonDimens.DefaultButtonMinHeight)
            ) {
                Text(text = stringResource(R.string.auth_signup_next))
            }

            Divider(
                modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
                color = LocalColors.current.separatorNorm
            )

            ProtonTextButton(
                contained = false,
                onClick = { onCreateExternalClicked(CreateUsernameAction.CreateExternalAccount) },
                modifier = Modifier
                    .padding(vertical = ProtonDimens.MediumSpacing)
                    .height(ProtonDimens.DefaultButtonMinHeight),
            ) {
                Text(text = stringResource(R.string.auth_signup_use_current_email))
            }

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.auth_signup_internal_footnote),
                style = ProtonTypography.Default.defaultSmallWeak
            )
        }
    }
}


@Composable
private fun CreateExternalForm(
    modifier: Modifier = Modifier,
    onExternalEmailSubmitted: (CreateUsernameAction.Submit) -> Unit,
    onCreateInternalClicked: (CreateUsernameAction.CreateInternalAccount) -> Unit,
    emailError: String? = null,
    enabled: Boolean
) {
    var email by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        ProtonOutlinedTextFieldWithError(
            text = email,
            onValueChanged = { email = it },
            enabled = enabled,
            errorText = emailError,
            label = { Text(text = stringResource(id = R.string.auth_email)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DefaultSpacing)
                .testTag(EMAIL_FIELD_TAG)
        )

        ProtonSolidButton(
            contained = false,
            enabled = enabled,
            loading = !enabled,
            onClick = { onExternalEmailSubmitted(CreateUsernameAction.Submit(email, AccountType.External)) },
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_signup_next))
        }

        Divider(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            color = LocalColors.current.separatorNorm
        )

        ProtonTextButton(
            contained = false,
            onClick = { onCreateInternalClicked(CreateUsernameAction.CreateInternalAccount) },
            modifier = Modifier
                .padding(vertical = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
        ) {
            Text(text = stringResource(R.string.auth_signup_get_encrypted_email))
        }

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.auth_signup_external_footnote),
            style = ProtonTypography.Default.defaultSmallWeak
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun CreateUsernameScreenPreview() {
    ProtonTheme {
        CreateUsernameScreen()
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun CreateInternalPreview() {
    ProtonTheme {
        CreateInternalForm(
            enabled = true,
            onUsernameSubmitted = {},
            onCreateExternalClicked = {},
            usernameError = stringResource(id = R.string.auth_login_assistive_text),
            domains = listOf("protonmail.com", "protonmail.ch")
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun CreateExternalPreview() {
    ProtonTheme {
        CreateExternalForm(
            enabled = true,
            onExternalEmailSubmitted = {},
            onCreateInternalClicked = {},
            emailError = stringResource(id = R.string.auth_login_assistive_text)
        )
    }
}
