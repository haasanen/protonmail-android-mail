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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodySmallWeak
import ch.protonmail.android.design.compose.theme.labelMediumNorm
import ch.protonmail.android.mailcomposer.presentation.R

@Composable
fun PasswordInputField(
    @StringRes titleRes: Int,
    @StringRes supportingTextRes: Int?,
    textFieldState: TextFieldState,
    showTrailingIcon: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val focusManager = LocalFocusManager.current
        var showPassword by rememberSaveable { mutableStateOf(false) }

        PasswordInputFieldLabel(text = stringResource(id = titleRes), isError = isError)

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = textFieldState.text.toString(),
            onValueChange = { value ->
                textFieldState.edit {
                    replace(
                        0,
                        textFieldState.text.length,
                        value
                    )

                }
            },
            shape = RoundedCornerShape(ProtonDimens.LargeCornerRadius),
            colors = getPasswordInputFieldColors(),
            singleLine = true,
            textStyle = ProtonTheme.typography.bodyLargeNorm,
            trailingIcon = {
                if (showTrailingIcon) {
                    PasswordInputFieldTrailingButton(
                        showPassword = showPassword,
                        onClick = { showPassword = !showPassword }
                    )
                }
            },
            isError = isError,
            visualTransformation = if (showTrailingIcon && !showPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        supportingTextRes?.let { PasswordInputFieldSupportingText(textId = it, isError = isError) }
    }
}

@Composable
fun PasswordInputFieldLabel(
    text: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier.padding(bottom = ProtonDimens.Spacing.Standard),
        text = text,
        style = ProtonTheme.typography.labelMediumNorm,
        color = if (isError) ProtonTheme.colors.notificationError else ProtonTheme.colors.textNorm
    )
}

@Composable
fun PasswordInputFieldTrailingButton(
    showPassword: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        val iconId = if (showPassword) R.drawable.ic_proton_eye_slash else R.drawable.ic_proton_eye
        val contentDescriptionId = if (showPassword) {
            R.string.set_message_password_button_hide
        } else {
            R.string.set_message_password_button_show
        }
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = stringResource(id = contentDescriptionId),
            tint = ProtonTheme.colors.iconHint
        )
    }
}

@Composable
fun PasswordInputFieldSupportingText(
    textId: Int,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier.padding(top = ProtonDimens.Spacing.Small),
        text = stringResource(id = textId),
        style = ProtonTheme.typography.bodySmallWeak,
        color = if (isError) ProtonTheme.colors.notificationError else ProtonTheme.colors.textWeak
    )
}

@Composable
fun getPasswordInputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ProtonTheme.colors.textNorm,
    unfocusedTextColor = ProtonTheme.colors.textNorm,
    focusedContainerColor = ProtonTheme.colors.backgroundSecondary,
    unfocusedContainerColor = ProtonTheme.colors.backgroundSecondary,
    focusedBorderColor = ProtonTheme.colors.brandNorm,
    unfocusedBorderColor = Color.Transparent,
    errorBorderColor = ProtonTheme.colors.notificationError
)
