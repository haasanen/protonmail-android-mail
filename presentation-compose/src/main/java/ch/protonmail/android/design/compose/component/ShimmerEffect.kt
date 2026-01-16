/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.design.compose.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme

/**
 * A modifier that applies a shimmer loading effect to the composable.
 * The shimmer animates a gradient highlight across the component horizontally.
 *
 * @param baseColor The base color of the shimmer background. Defaults to theme shade20.
 * @param highlightColor The color of the animated highlight. Defaults to a lighter shade.
 */
@Composable
fun Modifier.shimmerEffect(
    baseColor: Color = ProtonTheme.colors.shade20,
    highlightColor: Color = ProtonTheme.colors.shade10
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(translateAnimation - 200f, 0f),
        end = Offset(translateAnimation, 0f)
    )

    return this.background(shimmerBrush)
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    baseColor: Color = ProtonTheme.colors.shade20,
    highlightColor: Color = ProtonTheme.colors.shade10
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ProtonDimens.Spacing.Small))
            .shimmerEffect(baseColor, highlightColor)
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewShimmerBox() {
    ProtonTheme {
        Column {
            ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 14.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 14.dp))
        }
    }
}
