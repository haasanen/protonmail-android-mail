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

package me.proton.android.core.auth.presentation.secondfactor.fido2

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.auth.presentation.R
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.theme.ProtonDimens

@Suppress("UseComposableActions")
@Composable
fun Fido2InputForm(
    modifier: Modifier = Modifier,
    onError: (String?) -> Unit,
    onSuccess: () -> Unit,
    onClose: () -> Unit,
    @DrawableRes fido2Logo: Int = R.drawable.ic_fido2,
    viewModel: Fido2InputViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentState = state
    LaunchedEffect(currentState) {
        when (currentState) {
            is Fido2InputState.Error -> onError(currentState.error)
            is Fido2InputState.Closed -> onClose()
            Fido2InputState.LoggedIn -> onSuccess()
            else -> Unit
        }
    }

    Fido2InputForm(
        modifier = modifier,
        fido2Logo = fido2Logo,
        state = currentState,
        onAuthenticate = { viewModel.perform(Fido2InputAction.Authenticate()) }
    )
}

@Composable
fun Fido2InputForm(
    modifier: Modifier = Modifier,
    @DrawableRes fido2Logo: Int = R.drawable.ic_fido2,
    onAuthenticate: () -> Unit = {},
    state: Fido2InputState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ProtonDimens.DefaultSpacing),
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))

        Image(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            painter = painterResource(fido2Logo),
            contentDescription = null,
            alignment = Alignment.Center
        )

        AnnotatedLinkText(
            fullText = stringResource(R.string.auth_second_factor_insert_security_key),
            linkText = stringResource(R.string.auth_second_factor_insert_security_key_link)
        )

        ProtonSolidButton(
            onClick = onAuthenticate,
            contained = false,
            modifier = Modifier.height(ProtonDimens.DefaultButtonMinHeight),
            loading = state is Fido2InputState.ReadingSecurityKey
        ) {
            Text(text = stringResource(R.string.auth_second_factor_authenticate))
        }

        Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))
    }
}

@Composable
fun AnnotatedLinkText(
    fullText: String,
    linkText: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        append(fullText)

        withLink(
            LinkAnnotation.Url(
                url = stringResource(R.string.second_factor_security_key_link),
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = ProtonTheme.colors.interactionBrandDefaultNorm,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(linkText)
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun Fido2InputFormPreview() {
    ProtonTheme {
        Fido2InputForm(
            onAuthenticate = {},
            state = Fido2InputState.Idle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Fido2InputFormLoadingPreview() {
    ProtonTheme {
        Fido2InputForm(
            onAuthenticate = {},
            state = Fido2InputState.ReadingSecurityKey
        )
    }
}
