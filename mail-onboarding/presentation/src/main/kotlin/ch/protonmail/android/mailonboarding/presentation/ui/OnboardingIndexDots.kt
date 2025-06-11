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

package ch.protonmail.android.mailonboarding.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme

@Composable
fun OnboardingIndexDots(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
    highlightedColor: Color = ProtonTheme.colors.brandNorm,
    defaultColor: Color = ProtonTheme.colors.shade40,
    highlightedWidth: Dp = ProtonDimens.Spacing.ExtraLarge,
    defaultWidth: Dp = ProtonDimens.Spacing.Compact,
    spacing: Dp = ProtonDimens.Spacing.Compact
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(ProtonDimens.Spacing.Standard)
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { pageIndex ->
                val isSelected = pageIndex == currentPage

                val width by animateDpAsState(
                    targetValue = if (isSelected) highlightedWidth else defaultWidth,
                    animationSpec = spring(),
                    label = "dotWidth"
                )

                val color by animateColorAsState(
                    targetValue = if (isSelected) highlightedColor else defaultColor,
                    animationSpec = spring(),
                    label = "dotColor"
                )

                Box(
                    modifier = Modifier
                        .height(ProtonDimens.Spacing.Compact)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
