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

package me.proton.android.core.auth.presentation.signup

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
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
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultNorm

data class Country(
    val countryCode: String,
    val callingCode: Int? = null,
    val name: String,
    val flagId: Int = 0
)

@Composable
fun CountryCodeDropDown(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    data: List<Country> = emptyList(),
    onInputChanged: (Country) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(data.firstOrNull()) }

    if (selected == null) {
        return
    }

    ExposedDropdownMenuBox(
        expanded = expanded && !isLoading,
        onExpandedChange = {}
    ) {
        Card(
            modifier = modifier.clickable { expanded = !expanded },
            contentColor = ProtonTheme.colors.textNorm,
            elevation = 0.dp
        ) {
            CountryListItem(
                country = selected,
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
            data.forEach { country ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        selected = country
                        onInputChanged(country)
                    },
                    contentPadding = PaddingValues(0.dp, 0.dp)
                ) {
                    CountryListItem(country = country)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun CountryListItem(
    country: Country?,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        text = {
            Text(
                text = "+${country?.callingCode}",
                style = ProtonTypography.Default.defaultNorm
            )
        },
        trailing = { trailing?.invoke() }
    )
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun CountryDropDownPreview() {
    ProtonTheme {
        CountryCodeDropDown(
            data = listOf(
                Country(
                    countryCode = "CH",
                    callingCode = 1,
                    name = "Switzerland"
                )
            )
        )
    }
}
