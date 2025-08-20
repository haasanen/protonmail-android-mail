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

package ch.protonmail.android.design.compose.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import ch.protonmail.android.design.compose.theme.ProtonTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens

@Composable
fun ProtonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = SwitchDefaults.colors(
        // CHECKED (enabled)
        checkedThumbColor = Color.White,
        checkedTrackColor = ProtonTheme.colors.iconAccent,
        checkedBorderColor = ProtonTheme.colors.iconAccent,
        checkedIconColor = Color.Unspecified,

        // UNCHECKED (enabled)
        uncheckedThumbColor = ProtonTheme.colors.iconHint,
        uncheckedTrackColor = ProtonTheme.colors.backgroundNorm,
        uncheckedBorderColor = ProtonTheme.colors.iconHint,
        uncheckedIconColor = Color.Unspecified,

        // CHECKED (disabled)
        disabledCheckedThumbColor = Color.White,
        disabledCheckedTrackColor = ProtonTheme.colors.iconDisabled,
        disabledCheckedBorderColor = ProtonTheme.colors.iconDisabled,
        disabledCheckedIconColor = Color.Unspecified,

        // UNCHECKED (disabled)
        disabledUncheckedThumbColor = ProtonTheme.colors.iconDisabled,
        disabledUncheckedTrackColor = ProtonTheme.colors.backgroundNorm,
        disabledUncheckedBorderColor = ProtonTheme.colors.iconDisabled,
        disabledUncheckedIconColor = Color.Unspecified
    )

    Switch(
        checked = checked,
        onCheckedChange = if (enabled) onCheckedChange else null,
        enabled = enabled,
        modifier = modifier,
        colors = colors
    )
}

@Composable
private fun PreviewLabeledRow(
    label: String,
    checked: Boolean,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.Large, vertical = ProtonDimens.Spacing.Standard),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = ProtonTheme.typography.bodyMedium)
        ProtonSwitch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = {}
        )
    }
}

@Composable
private fun PreviewProtonSwitchMatrix() {
    Column(modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Medium)) {
        PreviewLabeledRow(label = "Checked • Enabled", checked = true, enabled = true)
        PreviewLabeledRow(label = "Checked • Disabled", checked = true, enabled = false)
        PreviewLabeledRow(label = "Unchecked • Enabled", checked = false, enabled = true)
        PreviewLabeledRow(label = "Unchecked • Disabled", checked = false, enabled = false)
    }
}

@Preview(
    name = "ProtonSwitch — All (Light)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun PreviewProtonSwitchLightTheme() {
    ProtonTheme {
        Surface { PreviewProtonSwitchMatrix() }
    }
}

@Preview(
    name = "ProtonSwitch — All (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewProtonSwitchDarkTheme() {
    ProtonTheme {
        Surface { PreviewProtonSwitchMatrix() }
    }
}
