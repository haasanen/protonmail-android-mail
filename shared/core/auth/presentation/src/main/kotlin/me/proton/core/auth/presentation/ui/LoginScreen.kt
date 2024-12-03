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

package me.proton.core.auth.presentation.ui

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.auth.presentation.R
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
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.presentation.R as CoreR

internal const val PASSWORD_FIELD_TAG = "PASSWORD_FIELD_TAG" // gitleaks:allow
internal const val USERNAME_FIELD_TAG = "USERNAME_FIELD_TAG"

@Composable
public fun LoginScreen(
    @DrawableRes protonLogo: Int,
    @StringRes protonNameContentDescription: Int,
    @StringRes titleText: Int,
    @StringRes subtitleText: Int,
    onLoginClicked: () -> Unit,
    onCloseClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LoginScreen(
        modifier = modifier,
        actions = LoginScreen.Actions(
            onCloseClicked = onCloseClicked,
            onHelpClicked = onHelpClicked,
            onLoginClicked = onLoginClicked
        ),
        protonLogoWithNamePainter = painterResource(id = protonLogo),
        protonNameContentDescription = stringResource(id = protonNameContentDescription),
        titleText = stringResource(id = titleText),
        subtitleText = stringResource(id = subtitleText)
    )
}

@Composable
public fun LoginScreen(
    modifier: Modifier = Modifier,
    actions: LoginScreen.Actions,
    protonLogoWithNamePainter: Painter,
    protonNameContentDescription: String,
    titleText: String,
    subtitleText: String
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = actions.onCloseClicked) {
                        Icon(
                            painterResource(id = CoreR.drawable.ic_proton_close),
                            contentDescription = stringResource(id = R.string.auth_login_close)
                        )
                    }
                },
                actions = {
                    ProtonTextButton(
                        onClick = actions.onHelpClicked
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
            Column(modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)) {
                Image(
                    modifier = Modifier
                        .height(64.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = protonLogoWithNamePainter,
                    contentDescription = protonNameContentDescription,
                    alignment = Alignment.Center
                )

                Text(
                    text = titleText,
                    style = ProtonTypography.Default.headline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = ProtonDimens.MediumSpacing)
                )

                Text(
                    text = subtitleText,
                    style = ProtonTypography.Default.defaultSmallWeak,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = ProtonDimens.SmallSpacing)
                )

                LoginScreenForm(
                    enabled = true,
                    onLoginClicked = actions.onLoginClicked,
                    usernameError = stringResource(id = R.string.auth_login_assistive_text)
                )
            }
        }
    }
}

@Composable
private fun LoginScreenForm(
    onLoginClicked: () -> Unit,
    usernameError: String,
    enabled: Boolean
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        ProtonOutlinedTextFieldWithError(
            text = username,
            onValueChanged = { username = it },
            enabled = enabled,
            errorText = usernameError,
            label = { Text(text = stringResource(id = R.string.auth_username)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DefaultSpacing)
                .testTag(USERNAME_FIELD_TAG)
        )

        ProtonOutlinedTextFieldWithError(
            text = password,
            onValueChanged = { password = it },
            enabled = enabled,
            label = { Text(text = stringResource(id = R.string.auth_password)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DefaultSpacing)
                .testTag(PASSWORD_FIELD_TAG)
        )

        ProtonSolidButton(
            contained = false,
            onClick = onLoginClicked,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_login))
        }
    }
}

public data object LoginScreen {
    public data class Actions(
        val onCloseClicked: () -> Unit,
        val onHelpClicked: () -> Unit,
        val onLoginClicked: () -> Unit
    ) {
        public companion object {
            public fun empty(): Actions =
                Actions(
                    onCloseClicked = {},
                    onHelpClicked = {},
                    onLoginClicked = {}
                )
        }
    }
}


@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginScreenPreview() {
    ProtonTheme {
        LoginScreen(
            actions = LoginScreen.Actions.empty(),
            protonLogoWithNamePainter = painterResource(CoreR.drawable.ic_logo_proton),
            protonNameContentDescription = stringResource(R.string.app_name_mail),
            titleText = stringResource(R.string.auth_sign_in),
            subtitleText = stringResource(R.string.auth_account_details)
        )
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginScreenFormPreview() {
    ProtonTheme {
        LoginScreenForm(
            enabled = true,
            onLoginClicked = {},
            usernameError = stringResource(id = R.string.auth_login_assistive_text)
        )
    }
}

