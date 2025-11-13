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

package ch.protonmail.android.mailonboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailonboarding.presentation.R

@Composable
fun AccountsTooltip(modifier: Modifier = Modifier, anchorBounds: Rect?) {

    TooltipBox(modifier, anchorBounds) {
        Row(
            modifier = Modifier
                .clickable(enabled = false, onClick = {})
                .fillMaxWidth()
        ) {

            SparkleIcon()

            ToolTipText(Modifier.weight(2f, true))

            CloseIcon()
        }
    }
}

@Composable
private fun TooltipBox(
    modifier: Modifier = Modifier,
    anchorBounds: Rect?,
    content: @Composable BoxScope.() -> Unit
) {
    val offsetY = anchorBounds?.bottom ?: 0f
    val tooltipShape = TooltipShape(offset = 120)

    Box(
        modifier = modifier
            .padding(ProtonDimens.Spacing.Standard)
            .fillMaxWidth()
            .offset { IntOffset(0, offsetY.toInt()) }
            .border(
                width = 1.dp,
                color = ProtonTheme.colors.borderNorm,
                shape = tooltipShape
            )
            .shadow(
                elevation = ProtonDimens.ShadowElevation.Lifted,
                shape = tooltipShape,
                ambientColor = ProtonTheme.colors.shadowRaised,
                spotColor = ProtonTheme.colors.shadowRaised
            )
            .background(
                color = ProtonTheme.colors.backgroundNorm,
                shape = tooltipShape
            )
            .clickable(true, onClick = {}),
        content = content
    )
}

@Composable
private fun SparkleIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(ProtonDimens.Spacing.Medium)
            .background(
                color = ProtonTheme.colors.interactionBrandWeakNorm,
                shape = ProtonTheme.shapes.large
            )
    ) {
        Icon(
            modifier = modifier
                .padding(ProtonDimens.Spacing.Standard)
                .size(ProtonDimens.IconSize.Medium),
            painter = painterResource(R.drawable.sparkles),
            contentDescription = null,
            tint = ProtonTheme.colors.iconAccent
        )
    }
}

@Composable
private fun ToolTipText(modifier: Modifier = Modifier) {
    Column(
        modifier
            .padding(vertical = ProtonDimens.Spacing.Medium)
    ) {
        Text(
            text = stringResource(R.string.accounts_new_location_tooltip),
            style = ProtonTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)

        )

        Spacer(Modifier.size(ProtonDimens.Spacing.Small))

        Text(
            text = stringResource(R.string.accounts_new_location_tooltip_description),
            style = ProtonTheme.typography.bodyMediumWeak
        )

        Spacer(Modifier.size(ProtonDimens.Spacing.Medium))
    }
}

@Composable
private fun CloseIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(ProtonDimens.Spacing.Medium)
            .background(
                color = ProtonTheme.colors.shade40.copy(
                    alpha = .6f
                ),
                shape = RoundedCornerShape(percent = 100)
            )
            .clickable(true, onClick = {})
    ) {
        Icon(
            modifier = Modifier
                .padding(ProtonDimens.Spacing.Tiny)
                .size(ProtonDimens.IconSize.Medium),
            painter = painterResource(R.drawable.ic_proton_close_filled),
            contentDescription = null,
            tint = ProtonTheme.colors.shade60
        )
    }
}

@Preview
@Composable
fun PreviewAccountsTooltip() {
    AccountsTooltip(
        anchorBounds = null
    )
}
