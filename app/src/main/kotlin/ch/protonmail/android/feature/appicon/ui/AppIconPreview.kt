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

package ch.protonmail.android.feature.appicon.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.feature.appicon.model.AppIconUiModel
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import coil.compose.AsyncImage

@Composable
fun AppIconPreview(
    modifier: Modifier = Modifier,
    preset: AppIconUiModel,
    showBorder: Boolean = false
) {
    val borderModifier = if (showBorder) {
        Modifier.border(
            width = 1.dp,
            color = ProtonTheme.colors.separatorNorm,
            shape = RoundedCornerShape(ProtonDimens.Spacing.ExtraLarge)
        )
    } else {
        Modifier
    }

    AsyncImage(
        model = preset.iconPreviewResId,
        contentDescription = NO_CONTENT_DESCRIPTION,
        modifier = modifier
            .then(borderModifier)
            .clip(RoundedCornerShape(20.dp)),
        contentScale = ContentScale.Crop
    )
}
