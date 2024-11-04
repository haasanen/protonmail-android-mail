/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.design.compose.component.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtonTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit),
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = ProtonTheme.colors.backgroundNorm,
    contentColor: Color = ProtonTheme.colors.textNorm
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = topAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor,
            navigationIconContentColor = contentColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor
        )
    )
}

@Composable
@Preview(showSystemUi = true)
fun ProtonTopAppBarPreview() {
    ProtonTheme {
        ProtonTopAppBar(
            title = {
                Text(text = "TopAppBar")
            },
            navigationIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        )
    }
}
