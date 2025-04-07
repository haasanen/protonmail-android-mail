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
@file:OptIn(ExperimentalMaterialApi::class)

package me.proton.android.core.auth.presentation.signup.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultNorm

typealias Domain = String

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun DomainDropDown(
    isLoading: Boolean = false,
    data: List<Domain> = emptyList(),
    onInputChanged: (Domain?) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Domain?>(null) }

    if (selected == null) {
        selected = data.firstOrNull()
        onInputChanged(selected)
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
        expanded = expanded && !isLoading,
        onExpandedChange = {}
    ) {
        Card(
            modifier = Modifier.clickable { expanded = !expanded },
            contentColor = ProtonTheme.colors.textNorm,
            elevation = 0.dp
        ) {
            DomainListItem(
                domain = selected,
                trailing = {
                    if (data.size > 1) {
                        TrailingIcon(expanded = expanded)
                    }
                }
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            data.forEach { domain ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        selected = domain
                        onInputChanged(domain)
                    },
                    contentPadding = PaddingValues(0.dp, 0.dp)
                ) {
                    DomainListItem(domain = domain)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun DomainListItem(
    domain: Domain?,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier,
        text = {
            Text(
                text = domain ?: "",
                style = ProtonTypography.Default.defaultNorm
            )
        },
        trailing = { trailing?.invoke() }
    )
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun NonEmptyDomainDropDownPreview() {
    ProtonTheme {
        DomainDropDown(
            data = listOf("@protonmail.com", "@protonmail.ch")
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun EmptyDomainDropDownPreview() {
    ProtonTheme {
        DomainDropDown()
    }
}
