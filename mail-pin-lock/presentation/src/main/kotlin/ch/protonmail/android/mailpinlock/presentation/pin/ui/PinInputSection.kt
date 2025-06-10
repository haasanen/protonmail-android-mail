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

package ch.protonmail.android.mailpinlock.presentation.pin.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.standalone.LocalLockScreenEntryPointIsStandalone

@Composable
internal fun PinInputSection(
    modifier: Modifier = Modifier,
    pinTextFieldState: TextFieldState,
    maxLength: Int? = null,
    error: TextUiModel?
) {
    val isError = error != null
    var shouldShake by remember { mutableStateOf(false) }

    val shakeOffset by animateFloatAsState(
        if (shouldShake) 1f else 0f,
        animationSpec = if (shouldShake) {
            repeatable(
                iterations = 5,
                animation = tween(durationMillis = 100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(durationMillis = 0)
        },
        finishedListener = {
            shouldShake = false
        },
        label = "shake_animation"
    )

    LaunchedEffect(isError) {
        if (isError) {
            shouldShake = true
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = ProtonDimens.Spacing.Standard)
            .offset(x = (shakeOffset * 8).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinSecureInputField(
            modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.ExtraLarge),
            pinTextFieldState = pinTextFieldState,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            maxLength = maxLength,
            isError = isError
        ) {
            if (isError) SupportingErrorText(error)
        }
    }
}

@Composable
internal fun PinSecureInputField(
    modifier: Modifier = Modifier,
    pinTextFieldState: TextFieldState,
    keyboardOptions: KeyboardOptions,
    maxLength: Int? = null,
    isError: Boolean,
    supportingText: @Composable () -> Unit
) {
    val isStandalone = LocalLockScreenEntryPointIsStandalone.current
    var shouldShowContent by remember { mutableStateOf(false) }
    val painter = if (shouldShowContent) {
        painterResource(id = R.drawable.ic_proton_eye_slashed)
    } else {
        painterResource(id = R.drawable.ic_proton_eye_clear)
    }

    val textObfuscationMode = if (shouldShowContent) {
        TextObfuscationMode.Visible
    } else {
        TextObfuscationMode.Hidden
    }

    OutlinedSecureTextField(
        trailingIcon = {
            IconButton(onClick = { shouldShowContent = !shouldShowContent }) {
                Icon(
                    modifier = Modifier.size(ProtonDimens.IconSize.Small),
                    painter = painter,
                    contentDescription = NO_CONTENT_DESCRIPTION
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .then(if (isStandalone) Modifier.size(0.dp) else Modifier),
        inputTransformation = createLengthLimitTransformation(maxLength),
        textObfuscationMode = textObfuscationMode,
        state = pinTextFieldState,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = createPinTextFieldColors(),
        textStyle = createPinTextStyle(),
        isError = isError,
        supportingText = { supportingText() }
    )

    if (isStandalone) {
        val dotTint = if (isError) ProtonTheme.colors.notificationError else ProtonTheme.colors.textNorm

        Column {
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                repeat(pinTextFieldState.text.length) {
                    AutoLockPinDotItem(tint = dotTint)
                }
            }
            if (isError) {
                Spacer(Modifier.height(ProtonDimens.Spacing.Large))
                supportingText()
            }
        }
    }
}

private fun createLengthLimitTransformation(maxLength: Int?) = object : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (maxLength != null && this.length > maxLength) {
            delete(maxLength, length)
        }
    }
}

@Composable
private fun createPinTextFieldColors() = TextFieldDefaults.colors().copy(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedContainerColor = ProtonTheme.colors.backgroundDeep,
    unfocusedContainerColor = ProtonTheme.colors.backgroundDeep,
    errorContainerColor = ProtonTheme.colors.backgroundDeep,
    errorIndicatorColor = ProtonTheme.colors.notificationError
)

@Composable
private fun createPinTextStyle() = TextStyle(
    fontStyle = ProtonTheme.typography.titleLarge.fontStyle,
    fontWeight = ProtonTheme.typography.titleLarge.fontWeight,
    fontSize = ProtonTheme.typography.titleLarge.fontSize
)

@Composable
private fun SupportingErrorText(error: TextUiModel) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = error.string(),
        style = ProtonTheme.typography.bodySmallNorm,
        color = ProtonTheme.colors.notificationError,
        textAlign = TextAlign.Center
    )
}
