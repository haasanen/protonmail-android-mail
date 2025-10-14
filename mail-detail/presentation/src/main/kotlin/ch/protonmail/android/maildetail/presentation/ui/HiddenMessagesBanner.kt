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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.ProtonSwitch
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.HiddenMessagesBannerState

@Composable
fun HiddenMessagesBanner(
    state: HiddenMessagesBannerState.Shown,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(
                start = ProtonDimens.Spacing.Large,
                end = ProtonDimens.Spacing.Large,
                bottom = ProtonDimens.Spacing.Massive
            )
            .shadow(
                elevation = ProtonDimens.ShadowElevation.Soft,
                shape = ProtonTheme.shapes.large,
                ambientColor = ProtonTheme.colors.shadowSoft,
                spotColor = ProtonTheme.colors.shadowSoft
            )
            .border(
                width = MailDimens.DefaultBorder,
                color = ProtonTheme.colors.separatorNorm,
                shape = ProtonTheme.shapes.large
            )
            .background(
                color = ProtonTheme.colors.backgroundNorm,
                shape = ProtonTheme.shapes.large
            )
            .padding(
                horizontal = ProtonDimens.Spacing.Large,
                vertical = ProtonDimens.Spacing.ModeratelyLarge
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_proton_trash),
            contentDescription = NO_CONTENT_DESCRIPTION,
            tint = ProtonTheme.colors.iconNorm
        )
        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.ModeratelyLarge))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            text = stringResource(id = state.message),
            style = ProtonTheme.typography.bodyMediumNorm
        )
        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.ModeratelyLarge))
        ProtonSwitch(
            checked = state.isSwitchTurnedOn,
            onCheckedChange = { onCheckedChange() }
        )
    }
}
