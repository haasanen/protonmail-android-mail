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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.android.core.auth.presentation.R
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
    country: Country?,
    onNavigateToCountryPicker: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onNavigateToCountryPicker),
        contentColor = ProtonTheme.colors.textNorm,
        elevation = 0.dp,
        backgroundColor = ProtonTheme.colors.backgroundSecondary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+${country?.callingCode ?: ""}",
                style = ProtonTypography.Default.defaultNorm,
                color = ProtonTheme.colors.textNorm,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(R.drawable.ic_proton_chevron_down),
                contentDescription = stringResource(R.string.auth_signup_recovery_select_country)
            )
        }
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun CountryDropDownPreview() {
    ProtonTheme {
        CountryCodeDropDown(
            country = Country(
                countryCode = "CH",
                callingCode = 1,
                name = "Switzerland"
            ),
            onNavigateToCountryPicker = {}
        )
    }
}
