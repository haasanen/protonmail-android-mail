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

package ch.protonmail.android.maildetail.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.design.compose.R as design

@Composable
fun OpenInProtonCalendarBanner(modifier: Modifier = Modifier, onOpenInProtonCalendarClick: () -> Unit) {
    Button(
        shape = ProtonTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = ProtonTheme.colors.backgroundNorm
        ),
        contentPadding = PaddingValues(ProtonDimens.Spacing.Large),
        border = BorderStroke(MailDimens.DefaultBorder, ProtonTheme.colors.borderNorm),
        modifier = modifier
            .padding(
                start = ProtonDimens.Spacing.Large,
                end = ProtonDimens.Spacing.Large,
                bottom = ProtonDimens.Spacing.Standard
            )
            .fillMaxWidth()
            .shadow(
                elevation = ProtonDimens.ShadowElevation.Small,
                shape = ProtonTheme.shapes.extraLarge,
                spotColor = ProtonTheme.colors.shadowSoft,
                ambientColor = ProtonTheme.colors.shadowSoft
            ),

        onClick = onOpenInProtonCalendarClick
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(MailDimens.ProtonCalendarIconSize)
                    .border(
                        ProtonDimens.OutlinedBorderSize, ProtonTheme.colors.borderNorm,
                        shape = ProtonTheme.shapes.large
                    )
            ) {
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.ic_logo_calendar),
                    contentDescription = NO_CONTENT_DESCRIPTION
                )

            }
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    style = ProtonTheme.typography.bodyMedium,
                    color = ProtonTheme.colors.textAccent,
                    maxLines = 1,
                    text = stringResource(id = R.string.open_on_protoncalendar_banner_title)
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Tiny))
                Text(
                    style = ProtonTheme.typography.bodySmall,
                    color = ProtonTheme.colors.textWeak,
                    maxLines = 1,
                    text = stringResource(id = R.string.open_on_protoncalendar_banner_description)
                )
            }
            Box(modifier = Modifier.size(MailDimens.ProtonCalendarIconSize)) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = design.drawable.ic_proton_arrow_out_square_new),
                    contentDescription = NO_CONTENT_DESCRIPTION,
                    tint = ProtonTheme.colors.iconHint
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OpenInProtonCalendarBannerPreview() {
    ProtonTheme {
        Box {
            OpenInProtonCalendarBanner(onOpenInProtonCalendarClick = {})
        }
    }
}
