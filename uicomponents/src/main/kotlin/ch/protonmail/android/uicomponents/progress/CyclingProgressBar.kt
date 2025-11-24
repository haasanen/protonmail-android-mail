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

package ch.protonmail.android.uicomponents.progress

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity

@Composable
fun CyclingProgressBar(
    modifier: Modifier = Modifier,
    isAnimating: Boolean,
    animationDurationMillis: Int = CyclingProgressBarTokens.AnimationDurationMillis,
    barHeight: Dp = CyclingProgressBarTokens.BarHeight,
    barWidthFraction: Float = CyclingProgressBarTokens.BarWidthFraction,
    primaryColor: Color = CyclingProgressBarTokens.PrimaryColor,
    edgeColor: Color = CyclingProgressBarTokens.EdgeColor
) {
    Crossfade(
        targetState = isAnimating
    ) { animating ->
        if (animating) {
            CyclingProgressBarAnimating(
                modifier = modifier,
                animationDurationMillis = animationDurationMillis,
                barHeight = barHeight,
                barWidthFraction = barWidthFraction,
                primaryColor = primaryColor,
                edgeColor = edgeColor
            )
        } else {
            CyclingProgressBarStatic(
                modifier = modifier,
                barHeight = barHeight
            )
        }
    }
}

@Composable
private fun CyclingProgressBarAnimating(
    modifier: Modifier = Modifier,
    animationDurationMillis: Int,
    barHeight: Dp,
    barWidthFraction: Float,
    primaryColor: Color,
    edgeColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition()

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(Color.Transparent)
            .clipToBounds()
    ) {
        val totalWidth = maxWidth
        val barWidth = totalWidth * barWidthFraction

        val xOffset = (totalWidth + barWidth) * phase - barWidth

        val density = LocalDensity.current
        val barWidthPx = with(density) { barWidth.toPx() }
        val xOffsetPx = with(density) { xOffset.toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {

            val gradient = Brush.horizontalGradient(
                colors = listOf(edgeColor, primaryColor, edgeColor),
                startX = xOffsetPx,
                endX = xOffsetPx + barWidthPx
            )

            drawRect(
                brush = gradient,
                topLeft = Offset(x = xOffsetPx, y = 0f),
                size = Size(width = barWidthPx, height = size.height)
            )
        }
    }
}

@Composable
private fun CyclingProgressBarStatic(modifier: Modifier = Modifier, barHeight: Dp) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(Color.Transparent)
    )
}

object CyclingProgressBarTokens {

    const val AnimationDurationMillis: Int = 2_000
    val BarHeight: Dp = 2.dp
    const val BarWidthFraction: Float = 0.58f
    val PrimaryColor: Color = Color(0xFF3CBB3A)
    val EdgeColor: Color = PrimaryColor.copy(alpha = 0f)
}
