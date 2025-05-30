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

package me.proton.android.core.auth.presentation.help

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun LoginHelpItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LoginHelpItem(
        icon = painterResource(id = icon),
        title = stringResource(id = title),
        description = description?.let { stringResource(id = it) },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun LoginHelpItem(
    icon: Painter,
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(ProtonDimens.DefaultSpacing)
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(ProtonDimens.DefaultIconSize),
                colorFilter = ColorFilter.tint(ProtonTheme.colors.iconNorm)
            )
            Column(
                modifier = Modifier
                    .padding(start = ProtonDimens.DefaultSpacing)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    modifier = Modifier
                        .height(ProtonDimens.DefaultIconSize)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    text = title,
                    style = LocalTypography.current.bodyMedium
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = LocalTypography.current.bodySmall,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = ProtonDimens.ExtraSmallSpacing)
                    )
                }
            }
        }
    }
}
