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

package ch.protonmail.android.design.compose.component.appbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtonTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit),
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = ProtonTheme.colors.backgroundNorm,
    contentColor: Color = ProtonTheme.colors.textNorm,
    minHeight: Dp = ProtonDimens.DefaultTopBarMinHeight
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .wrapContentHeight(Alignment.CenterVertically)
            ) {
                title()
            }
        },
        modifier = modifier
            .heightIn(min = minHeight),
        navigationIcon = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentHeight(Alignment.CenterVertically)
            ) {
                navigationIcon()
            }
        },
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
@OptIn(ExperimentalMaterial3Api::class)
fun ProtonMediumTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit),
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = ProtonTheme.colors.backgroundNorm,
    contentColor: Color = ProtonTheme.colors.textNorm,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    MediumTopAppBar(
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
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProtonLargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit),
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = ProtonTheme.colors.backgroundNorm,
    contentColor: Color = ProtonTheme.colors.textNorm
) {
    LargeTopAppBar(
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        )
    }
}
