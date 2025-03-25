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

package ch.protonmail.android.mailcommon.presentation.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.extension.tintColor
import ch.protonmail.android.design.compose.theme.ProtonDimens

@Composable
fun SmallClickableIcon(
    @DrawableRes iconId: Int,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    iconSize: Dp = ProtonDimens.IconSize.Small
) {
    Icon(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics { tintColor = iconColor }
            .size(iconSize),
        painter = painterResource(id = iconId),
        contentDescription = NO_CONTENT_DESCRIPTION,
        tint = iconColor
    )
}
