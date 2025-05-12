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

package ch.protonmail.android.mailbugreport.presentation.ui.report

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.VerticalSpacer
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodySmallWeak
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailbugreport.presentation.R

@Composable
internal fun BugReportTextField(
    title: String,
    description: String,
    inputState: TextFieldState,
    shouldShowError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val dividerColor = when {
        shouldShowError -> ProtonTheme.colors.notificationError
        isFocused -> ProtonTheme.colors.iconAccent
        else -> ProtonTheme.colors.textWeak
    }

    Column {
        Text(text = title, style = ProtonTheme.typography.titleMediumNorm)
        VerticalSpacer()

        Text(text = description, style = ProtonTheme.typography.bodySmallWeak)
        VerticalSpacer(height = ProtonDimens.Spacing.Standard)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = ProtonDimens.Spacing.Standard,
                topEnd = ProtonDimens.Spacing.Standard
            ),
            color = ProtonTheme.colors.interactionWeakNorm
        ) {
            BasicTextField(
                inputState,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = ProtonDimens.Spacing.Medium, vertical = ProtonDimens.Spacing.Standard)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = MINIMUM_LINES),
                cursorBrush = SolidColor(ProtonTheme.colors.iconAccent),
                textStyle = ProtonTheme.typography.bodyLargeNorm,
                decorator = { innerTextField ->
                    Box {
                        innerTextField()

                        if (shouldShowError) {
                            Icon(
                                modifier = Modifier.align(Alignment.TopEnd),
                                painter = painterResource(id = R.drawable.mtrl_ic_error),
                                tint = ProtonTheme.colors.notificationError,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = ProtonDimens.Spacing.Tiny,
            color = dividerColor
        )

        if (shouldShowError) {
            VerticalSpacer(height = ProtonDimens.Spacing.Small)

            Text(
                modifier = Modifier.padding(start = ProtonDimens.Spacing.Standard),
                text = stringResource(R.string.report_a_problem_field_length_error),
                style = ProtonTheme.typography.bodySmall,
                color = ProtonTheme.colors.notificationError
            )
        }
    }
}

private const val MINIMUM_LINES = 6
