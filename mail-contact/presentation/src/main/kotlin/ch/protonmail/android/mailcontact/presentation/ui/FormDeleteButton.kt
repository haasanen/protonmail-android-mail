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

package ch.protonmail.android.mailcontact.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonButton
import ch.protonmail.android.design.compose.component.protonOutlinedButtonColors
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm

@Composable
fun FormDeleteButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = ButtonDefaults.MinHeight),
        elevation = null,
        shape = ProtonTheme.shapes.medium,
        border = BorderStroke(
            ProtonDimens.OutlinedBorderSize,
            ProtonTheme.colors.notificationError
        ),
        colors = ButtonDefaults.protonOutlinedButtonColors(
            backgroundColor = ProtonTheme.colors.notificationError
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = ProtonDimens.Spacing.ExtraLarge,
                vertical = ProtonDimens.Spacing.Small
            ),
            text = text,
            style = ProtonTheme.typography.bodyLargeNorm,
            color = Color.White
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun FormDeleteButtonPreview() {
    FormDeleteButton(
        text = "Delete label",
        onClick = {}
    )
}
