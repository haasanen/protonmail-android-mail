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

package ch.protonmail.android.legacymigration.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm

@Composable
fun MigrationLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ProtonTheme.colors.shade10),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.padding(ProtonDimens.Spacing.MediumLight)) {
                CircularProgressIndicator(
                    strokeWidth = ProtonDimens.BorderSize.Large,
                    color = ProtonTheme.colors.brandNorm,
                    modifier = Modifier
                        .size(ProtonDimens.IconSize.MediumLarge)
                )
            }

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Huge))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Jumbo)
            ) {
                Text(
                    text = stringResource(R.string.loading_mailbox),
                    style = ProtonTheme.typography.bodyLargeNorm,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))
                Text(
                    text = stringResource(R.string.loading_mailbox_subtitle),
                    style = ProtonTheme.typography.bodyLargeNorm,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(device = Devices.PHONE, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light mode")
@Preview(device = Devices.PHONE, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Night mode")
@Composable
private fun MigrationScreenPreview() {
    ProtonTheme {
        MigrationLoadingScreen()
    }
}
