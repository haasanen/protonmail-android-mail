/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.maildetail.presentation.ui.rsvpwidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.component.ProtonButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.maildetail.presentation.R

@Composable
fun RsvpWidgetError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = ProtonDimens.Spacing.Large)
            .padding(bottom = ProtonDimens.Spacing.Large)
            .background(
                color = ProtonTheme.colors.backgroundNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .border(
                width = ProtonDimens.OutlinedBorderSize,
                color = ProtonTheme.colors.borderNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(ProtonDimens.Spacing.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(IllustrationSize),
            painter = painterResource(id = R.drawable.illustration_global_unavailable_javascript),
            contentDescription = NO_CONTENT_DESCRIPTION
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

        Text(
            modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Jumbo),
            text = stringResource(id = R.string.rsvp_widget_invite_details_unavailable),
            style = ProtonTheme.typography.titleMediumNorm,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))

        Text(
            modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Jumbo),
            text = stringResource(id = R.string.rsvp_widget_could_not_load),
            style = ProtonTheme.typography.bodyMediumWeak,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        ProtonButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonHeight),
            elevation = null,
            shape = ProtonTheme.shapes.massive,
            border = null,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = ProtonTheme.colors.interactionWeakNorm,
                contentColor = ProtonTheme.colors.textNorm
            )
        ) {
            Text(
                text = stringResource(id = R.string.rsvp_widget_button_retry),
                style = ProtonTheme.typography.bodyLargeNorm
            )
        }
    }
}

@Preview
@Composable
fun RsvpWidgetErrorPreview() {
    RsvpWidgetError(onRetry = {})
}

private val IllustrationSize = 128.dp
private val ButtonHeight = 56.dp
