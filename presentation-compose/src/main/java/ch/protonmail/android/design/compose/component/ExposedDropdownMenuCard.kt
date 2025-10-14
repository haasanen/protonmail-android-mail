/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package ch.protonmail.android.design.compose.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.R
import ch.protonmail.android.design.compose.theme.ProtonDimens.BorderSize
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme

@Composable
fun ExposedDropdownMenuCard(
    list: List<String>,
    modifier: Modifier = Modifier,
    onSelectedIndexChanged: (Int) -> Unit = {},
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = ProtonTheme.colors.backgroundSecondary
    ),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = BorderStroke(
        width = BorderSize.Default,
        color = ProtonTheme.colors.borderNorm
    ),
    expandedInitially: Boolean = false,
    item: @Composable (String) -> Unit
) {
    var expanded by remember { mutableStateOf(expandedInitially) }
    var selected by remember { mutableIntStateOf(0) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {}
    ) {
        Card(
            onClick = { expanded = !expanded },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border
        ) {
            ListItem(
                headlineContent = { item(list[selected]) },
                trailingContent = { TrailingIcon(expanded) },
                colors = ListItemDefaults.colors(
                    containerColor = colors.containerColor
                )
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ProtonTheme.colors.backgroundNorm
        ) {
            list.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { item(item) },
                    onClick = {
                        expanded = false
                        selected = index
                        onSelectedIndexChanged(index)
                    }
                )
            }
        }
    }
}

@Composable
internal fun TrailingIcon(expanded: Boolean) {
    Icon(
        painter = when (expanded) {
            false -> painterResource(R.drawable.ic_proton_chevron_down)
            true -> painterResource(R.drawable.ic_proton_chevron_up)
        },
        contentDescription = null
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
private fun ExposedDropdownMenuCardPreview() {
    ProtonTheme {
        ExposedDropdownMenuCard(
            list = listOf("Pay monthly", "Pay annually"),
            modifier = Modifier.padding(Spacing.Medium)
        ) {
            Text(text = it)
        }
    }
}
