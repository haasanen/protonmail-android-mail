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

package ch.protonmail.android.uicomponents.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.component.ProtonRawListItem
import ch.protonmail.android.design.compose.component.ProtonSwitch
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.textNorm
import ch.protonmail.android.uicomponents.thenIf

@Composable
fun SettingsToggleItem(
    modifier: Modifier = Modifier,
    name: String,
    value: Boolean,
    hint: String? = null,
    isFieldEnabled: Boolean = true,
    upsellingIcon: @Composable () -> Unit = {},
    onToggle: (Boolean) -> Unit = {}
) {
    Column(
        modifier = modifier
            .thenIf(isFieldEnabled) {
                toggleable(
                    value = value,
                    enabled = isFieldEnabled,
                    role = Role.Switch
                ) { onToggle(!value) }
            }
            .padding(horizontal = ProtonDimens.Spacing.Large),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ProtonRawListItem(
            modifier = Modifier
                .sizeIn(minHeight = ProtonDimens.ListItemHeight)
                .padding(vertical = ProtonDimens.Spacing.Large),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                    color = ProtonTheme.colors.textNorm(),
                    style = ProtonTheme.typography.bodyLargeNorm
                )
                upsellingIcon()
            }
            ProtonSwitch(
                checked = value,
                onCheckedChange = {},
                enabled = isFieldEnabled
            )
        }
        hint?.let {
            Text(
                modifier = Modifier.offset(y = toggleItemNegativeOffset),
                text = hint,
                color = ProtonTheme.colors.textHint,
                style = ProtonTheme.typography.bodyMediumNorm
            )
        }
    }
}

private val toggleItemNegativeOffset = (-10).dp
