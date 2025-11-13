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

package ch.protonmail.android.mailtooltip.presentation.ui

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcommon.presentation.compose.toPx

class TooltipShape(val offset: Int) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radius = ProtonDimens.CornerRadius.MediumLarge.toPx(density).toFloat()
        val arrowWidth = ProtonDimens.AccountsTooltipArrowWidth.toPx(density).toFloat()
        val arrowHeight = ProtonDimens.AccountsTooltipArrowHeight.toPx(density).toFloat()

        val path = buildPath(radius, size, arrowWidth, arrowHeight)

        return Outline.Generic(path = path)
    }

    private fun buildPath(
        radius: Float,
        size: Size,
        arrowWidth: Float,
        arrowHeight: Float
    ): Path = Path().apply {
        moveTo(radius, 0f)
        lineTo(size.width - offset, 0f)
        lineTo(size.width - offset + arrowWidth / 2, -arrowHeight)
        lineTo(size.width - offset + arrowWidth, 0f)
        arcTo(
            rect = Rect(
                left = size.width - 2 * radius,
                top = 0f,
                right = size.width,
                bottom = 2 * radius
            ),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(size.width, size.height - arrowHeight - radius)
        arcTo(
            rect = Rect(
                left = size.width - 2 * radius,
                top = size.height - arrowHeight - 2 * radius,
                right = size.width,
                bottom = size.height - arrowHeight
            ),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(radius, size.height - arrowHeight)
        arcTo(
            rect = Rect(
                left = 0f,
                top = size.height - arrowHeight - 2 * radius,
                right = 2 * radius,
                bottom = size.height - arrowHeight
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(0f, radius)
        arcTo(
            rect = Rect(
                left = 0f,
                top = 0f,
                right = 2 * radius,
                bottom = 2 * radius
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        close()
    }
}
