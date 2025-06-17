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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailcomposer.presentation.R
import kotlin.time.Instant

@Composable
fun ScheduleSendTimePickerBottomSheetContent(
    onClose: () -> Unit,
    onScheduleSendConfirmed: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {

    val timeTextFieldState = rememberTextFieldState()
    val dateTextFieldState = rememberTextFieldState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .scrollable(
                rememberScrollableState(consumeScrollDelta = { 0f }),
                orientation = Orientation.Vertical
            )
    ) {
        TopBar(
            onClose = onClose,
            onSendClicked = {
                // Gather time from date and time pickers and schedule send
            },
            modifier = Modifier.padding(
                start = ProtonDimens.Spacing.Small,
                top = ProtonDimens.Spacing.Medium,
                end = ProtonDimens.Spacing.Medium,
                bottom = ProtonDimens.Spacing.Medium
            )
        )

       Column(
            modifier = modifier
                .fillMaxWidth()
                .background(ProtonTheme.colors.backgroundInvertedNorm)
                .padding(ProtonDimens.Spacing.Large)
        ) {

            ScheduleSendTime(
                textFieldState = timeTextFieldState,
                onPickCustomTime = {
                    // Show dialog to allow picking a custom time
                }
            )

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

            ScheduleSendDatePicker(dateTextFieldState)
        }
    }
}

@Composable
private fun ScheduleSendTime(
    textFieldState: TextFieldState,
    onPickCustomTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.extraLarge)
            .clickable(
                role = Role.Button,
                onClick = { onPickCustomTime() }
            )
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(ProtonDimens.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.composer_schedule_send_custom_time_label),
            style = ProtonTheme.typography.bodyMediumNorm,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

        BasicTextField(
            modifier = Modifier
                .background(
                    color = ProtonTheme.colors.backgroundInvertedDeep,
                    shape = ProtonTheme.shapes.medium
                )
                .size(80.dp, 40.dp)
                .padding(ProtonDimens.Spacing.Small),
            state = textFieldState,
            textStyle = ProtonTheme.typography.bodyLargeNorm
        )
    }
}

@Composable
private fun ScheduleSendDatePicker(textFieldState: TextFieldState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.extraLarge)
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(ProtonDimens.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.composer_schedule_send_custom_date_label),
            style = ProtonTheme.typography.bodyMediumNorm,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

        BasicTextField(
            modifier = Modifier
                .background(
                    color = ProtonTheme.colors.backgroundInvertedDeep,
                    shape = ProtonTheme.shapes.medium
                )
                .size(80.dp, 40.dp)
                .padding(ProtonDimens.Spacing.Small),
            state = textFieldState,
            textStyle = ProtonTheme.typography.bodyLargeNorm
        )
    }
}

@Composable
private fun TopBar(
    onClose: () -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_cross),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,

            )
        }

        Text(
            modifier = Modifier
                .background(ProtonTheme.colors.backgroundInvertedNorm),
            text = stringResource(R.string.composer_schedule_send_content_description),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.titleMediumNorm,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = onSendClicked,
            modifier = Modifier,
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
                text = stringResource(R.string.send_button_title),
                style = ProtonTheme.typography.titleSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewScheduleSendTimePickerBottomSheet() {
    ProtonTheme {
        ScheduleSendTimePickerBottomSheetContent(
            onClose = {},
            onScheduleSendConfirmed = {}
        )
    }
}
