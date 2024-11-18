/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package ch.protonmail.android.design.compose.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.captionNorm
import ch.protonmail.android.design.compose.theme.defaultNorm

const val PROTON_OUTLINED_TEXT_INPUT_TAG = "PROTON_OUTLINED_TEXT_INPUT_TAG"
private const val MAX_LINES = 2

@Composable
fun protonOutlineTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ProtonTheme.colors.textNorm,
    unfocusedTextColor = ProtonTheme.colors.textNorm,
    disabledTextColor = ProtonTheme.colors.textDisabled,
    errorTextColor = ProtonTheme.colors.notificationError,

    focusedContainerColor = ProtonTheme.colors.backgroundSecondary,
    unfocusedContainerColor = ProtonTheme.colors.backgroundSecondary,
    disabledContainerColor = ProtonTheme.colors.backgroundSecondary,
    errorContainerColor = ProtonTheme.colors.backgroundSecondary,

    focusedLabelColor = ProtonTheme.colors.textNorm,
    unfocusedLabelColor = ProtonTheme.colors.textHint,
    disabledLabelColor = ProtonTheme.colors.textDisabled,
    errorLabelColor = ProtonTheme.colors.notificationError,

    focusedBorderColor = ProtonTheme.colors.brandNorm,
    unfocusedBorderColor = ProtonTheme.colors.backgroundSecondary,
    disabledBorderColor = ProtonTheme.colors.backgroundSecondary,
    errorBorderColor = ProtonTheme.colors.notificationError,

    cursorColor = ProtonTheme.colors.brandNorm,
    errorCursorColor = ProtonTheme.colors.notificationError,

    focusedLeadingIconColor = ProtonTheme.colors.iconAccent,
    unfocusedLeadingIconColor = ProtonTheme.colors.iconWeak,
    disabledLeadingIconColor = ProtonTheme.colors.iconDisabled,
    errorLeadingIconColor = ProtonTheme.colors.notificationError,

    focusedTrailingIconColor = ProtonTheme.colors.iconAccent,
    unfocusedTrailingIconColor = ProtonTheme.colors.iconWeak,
    disabledTrailingIconColor = ProtonTheme.colors.iconDisabled,
    errorTrailingIconColor = ProtonTheme.colors.notificationError,

    focusedPlaceholderColor = ProtonTheme.colors.textHint,
    unfocusedPlaceholderColor = ProtonTheme.colors.textHint,
    disabledPlaceholderColor = ProtonTheme.colors.textDisabled,
    errorPlaceholderColor = ProtonTheme.colors.notificationError,

    focusedSupportingTextColor = ProtonTheme.colors.textNorm,
    unfocusedSupportingTextColor = ProtonTheme.colors.textHint,
    disabledSupportingTextColor = ProtonTheme.colors.textDisabled,
    errorSupportingTextColor = ProtonTheme.colors.notificationError
)



@Composable
fun ProtonOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = ProtonTheme.shapes.medium,
    colors: TextFieldColors = protonOutlineTextFieldColors()
) = OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    isError = isError,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors
)

@Composable
fun ProtonOutlinedTextFieldWithError(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: (@Composable () -> Unit)? = null,
    maxLines: Int = MAX_LINES,
    placeholder: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    onValueChanged: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChanged,
            colors = protonOutlineTextFieldColors(),
            enabled = enabled,
            isError = errorText != null,
            keyboardOptions = keyboardOptions,
            label = label,
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag(PROTON_OUTLINED_TEXT_INPUT_TAG),
            placeholder = placeholder,
            singleLine = singleLine,
            textStyle = ProtonTheme.typography.defaultNorm,
            visualTransformation = visualTransformation
        )
        Text(
            text = errorText ?: "",
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.Spacing.Small),
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.captionNorm,
            color = ProtonTheme.colors.notificationError
        )
    }
}

@Composable
fun ProtonOutlinedTextFieldWithError(
    textFieldValue: MutableState<TextFieldValue>,
    onValueChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: (@Composable () -> Unit)? = null,
    maxLines: Int = MAX_LINES,
    placeholder: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue.value,
            onValueChange = onValueChanged,
            colors = protonOutlineTextFieldColors(),
            enabled = enabled,
            isError = errorText != null,
            keyboardOptions = keyboardOptions,
            label = label,
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag(PROTON_OUTLINED_TEXT_INPUT_TAG),
            placeholder = placeholder,
            singleLine = singleLine,
            textStyle = ProtonTheme.typography.defaultNorm,
            visualTransformation = visualTransformation
        )
        Text(
            text = errorText ?: "",
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.Spacing.Small),
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.captionNorm,
            color = ProtonTheme.colors.notificationError
        )
    }
}

@Preview
@Composable
fun PreviewOutlinedTextFieldWithProtonColors() {
    OutlinedTextField(
        value = "Some text",
        onValueChange = {},
        colors = protonOutlineTextFieldColors()
    )
}

@Preview
@Composable
fun PreviewOutlinedTextFieldWithError() {
    ProtonOutlinedTextFieldWithError(
        text = "Some text",
        onValueChanged = {},
        errorText = "Validation failed!"
    )
}
