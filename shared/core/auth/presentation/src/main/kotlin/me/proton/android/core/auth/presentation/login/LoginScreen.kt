/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("UseComposableActions")

package me.proton.android.core.auth.presentation.login

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.proton.android.core.auth.presentation.LocalClipManager
import me.proton.android.core.auth.presentation.LocalClipManager.OnClipChangedDisposableEffect
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.android.core.auth.presentation.challenge.LOGIN_CHALLENGE_FLOW_NAME
import me.proton.android.core.auth.presentation.challenge.LOGIN_CHALLENGE_USERNAME_FRAME
import me.proton.android.core.auth.presentation.challenge.TextChange
import me.proton.core.challenge.presentation.compose.PayloadController
import me.proton.core.challenge.presentation.compose.payload
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.compose.util.LaunchOnScreenView
import me.proton.core.presentation.R as CoreR

internal const val PASSWORD_FIELD_TAG = "PASSWORD_FIELD_TAG" // gitleaks:allow
internal const val USERNAME_FIELD_TAG = "USERNAME_FIELD_TAG"

@Composable
public fun LoginScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    initialUsername: String? = null,
    onSuccess: (String) -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginScreen(
        modifier = modifier,
        initialUsername = initialUsername,
        onCloseClicked = onCloseClicked,
        onHelpClicked = onHelpClicked,
        onLoginClicked = { viewModel.submit(it) },
        onSuccess = onSuccess,
        onScreenView = viewModel::onScreenView,
        state = state
    )
}

@Composable
public fun LoginScreen(
    modifier: Modifier = Modifier,
    initialUsername: String? = null,
    onCloseClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onLoginClicked: (LoginAction.Login) -> Unit = {},
    onSuccess: (String) -> Unit = {},
    onScreenView: () -> Unit = {},
    state: LoginViewState = LoginViewState.Idle
) {
    LaunchedEffect(state) {
        when (state) {
            is LoginViewState.Error.AlreadyLoggedIn -> onSuccess(state.userId)
            is LoginViewState.LoggedIn -> onSuccess(state.userId)
            is LoginViewState.Awaiting2fa -> onSuccess(state.userId)
            is LoginViewState.Awaiting2Pass -> onSuccess(state.userId)
            is LoginViewState.AwaitingNewPass -> onSuccess(state.userId)
            else -> Unit
        }
    }

    LaunchOnScreenView(enqueue = onScreenView)

    LoginScaffold(
        modifier = modifier,
        initialUsername = initialUsername,
        onCloseClicked = onCloseClicked,
        onHelpClicked = onHelpClicked,
        onLoginClicked = onLoginClicked,
        isUsernameError = state is LoginViewState.Error.Validation,
        isLoading = state.isLoading
    )
}

@Composable
public fun LoginScaffold(
    modifier: Modifier = Modifier,
    initialUsername: String? = null,
    onCloseClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onLoginClicked: (LoginAction.Login) -> Unit = {},
    @DrawableRes protonLogo: Int = R.drawable.ic_logo_proton,
    @StringRes titleText: Int = R.string.auth_sign_in,
    @StringRes subtitleText: Int = R.string.auth_account_details,
    isUsernameError: Boolean = false,
    isLoading: Boolean = false
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onCloseClicked,
                        enabled = !isLoading
                    ) {
                        Icon(
                            painterResource(id = CoreR.drawable.ic_proton_close),
                            contentDescription = stringResource(id = R.string.auth_login_close)
                        )
                    }
                },
                actions = {
                    ProtonTextButton(
                        onClick = onHelpClicked,
                        enabled = !isLoading
                    ) {
                        Text(
                            text = stringResource(id = R.string.auth_login_help),
                            color = ProtonTheme.colors.textAccent,
                            style = ProtonTheme.typography.defaultStrongNorm
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

                Text(
                    text = stringResource(subtitleText),
                    style = ProtonTypography.Default.defaultSmallWeak,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = ProtonDimens.SmallSpacing)
                )

                LoginForm(
                    enabled = !isLoading,
                    onLoginClicked = onLoginClicked,
                    usernameError = if (isUsernameError) stringResource(R.string.auth_login_assistive_text) else null,
                    initialUsername = initialUsername
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    onLoginClicked: (LoginAction.Login) -> Unit,
    usernameError: String? = null,
    enabled: Boolean,
    initialUsername: String? = null
) {
    val scope = rememberCoroutineScope()
    var username by rememberSaveable { mutableStateOf(initialUsername ?: "") }
    var password by rememberSaveable { mutableStateOf("") }
    val usernameChanges = remember { MutableStateFlow(TextChange()) }
    val usernameHasFocus = remember { mutableStateOf(false) }
    val usernamePayloadController = remember { PayloadController() }
    val usernameTextCopies = remember { MutableStateFlow("") }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    LocalClipManager.current?.OnClipChangedDisposableEffect {
        if (usernameHasFocus.value) usernameTextCopies.value = it
    }

    LaunchedEffect(Unit) {
        if (!initialUsername.isNullOrEmpty()) {
            passwordFocusRequester.requestFocus()
        } else {
            usernameFocusRequester.requestFocus()
        }
    }

    fun onSubmit() = scope.launch {
        val usernameFrameDetails = usernamePayloadController.flush()
        onLoginClicked(LoginAction.Login(username, usernameFrameDetails, password))
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        ProtonOutlinedTextFieldWithError(
            text = username,
            onValueChanged = {
                usernameChanges.value = usernameChanges.value.roll(it)
                username = it
            },
            enabled = enabled,
            errorText = usernameError,
            label = { Text(text = stringResource(id = R.string.auth_username)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .onFocusChanged { usernameHasFocus.value = it.hasFocus }
                .fillMaxWidth()
                .padding(top = DefaultSpacing)
                .focusRequester(usernameFocusRequester)
                .payload(
                    flow = LOGIN_CHALLENGE_FLOW_NAME,
                    frame = LOGIN_CHALLENGE_USERNAME_FRAME,
                    onTextChanged = usernameChanges.map { it.toPair() },
                    onTextCopied = usernameTextCopies,
                    onFrameUpdated = {},
                    payloadController = usernamePayloadController
                )
                .semantics { contentType = ContentType.Username }
                .testTag(USERNAME_FIELD_TAG)
        )

        ProtonPasswordOutlinedTextFieldWithError(
            text = password,
            onValueChanged = { password = it },
            enabled = enabled,
            label = { Text(text = stringResource(id = R.string.auth_password)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = KeyboardActions { onSubmit() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DefaultSpacing)
                .focusRequester(passwordFocusRequester)
                .semantics { contentType = ContentType.Password }
                .testTag(PASSWORD_FIELD_TAG)
        )

        ProtonSolidButton(
            contained = false,
            enabled = enabled,
            loading = !enabled,
            onClick = ::onSubmit,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_login))
        }
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginScreenPreview() {
    ProtonTheme {
        LoginScreen()
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginScreenFormPreview() {
    ProtonTheme {
        LoginForm(
            enabled = true,
            onLoginClicked = {},
            usernameError = stringResource(id = R.string.auth_login_assistive_text)
        )
    }
}
