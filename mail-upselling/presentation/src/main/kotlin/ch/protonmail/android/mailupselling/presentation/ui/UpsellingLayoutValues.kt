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

package ch.protonmail.android.mailupselling.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object UpsellingLayoutValues {

    val titleColor = Color.White
    val subtitleColor = Color.White
    val closeButtonSize = 32.dp
    val closeButtonColor = Color.White
    val closeButtonBackgroundColor = Color.White.copy(alpha = 0.08f)

    val imageHeight = 140.dp

    val BlueInteractionNorm = Color(0xFF6D4AFF)

    val backgroundGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF1D121D),
            Color(0xFF6D4AFF)
        )
    )

    val autoRenewText = Color.White
    val autoRenewTextSize = 12.sp

    const val topSpacingWeight = 0.25f
    const val bottomSpacingWeight = 0.4f

    object SocialProof {

        val height = 76.dp
    }

    object ComparisonTable {

        val highlightBarColor = Color.White.copy(alpha = 0.08f)
        val highlightBarShape = RoundedCornerShape(8.dp)
        val spacerHeight = 1.dp
        val spacerBackgroundColor = Color.White.copy(alpha = 0.12f)

        val titleColumnSize = 16.sp
        val textColor = Color.White

        val itemTextSize = 14.sp
    }

    object EntitlementsList {

        val textColor = Color.White
        val iconColor = Color.White

        val rowDivider = Color.White.copy(alpha = 0.08f)
        val imageSize = 20.dp
    }

    object UpsellingPlanButtonsFooter {

        val backgroundColor = Color.White.copy(alpha = 0.08f)
        val spacerHeight = 1.dp
        val spacerColor = Color.White.copy(alpha = 0.24f)
    }

    object UpsellingPromoButton {

        val iconColorDark = Color(0xFF8A6EFF)
        val iconColorLight = BlueInteractionNorm
        val bgColor = Color(0x4d8a6eff)
    }

    object PaymentButtons {

        val discountTagBackground = Color.White.copy(alpha = 0.12f)
        val discountBadgeShape = RoundedCornerShape(8.dp)

        val discountTagTextColor = Color.White

        val choosePlanColor = Color.White
        val planNameColor = Color.White

        val outlinedCardContainerColor = Color.Black.copy(alpha = 0.2f)
        val outlinedCardContainerSelectedColor = Color.Black.copy(alpha = 0.4f)

        val outlinedCardSelectedBorderStroke = BorderStroke(2.dp, coloredBorderBrush)
    }

    object UpsellOnboarding {
        val bestValueShape = RoundedCornerShape(10.dp)
        val tabIndicatorWidth = 36.dp
        val tabIndicatorHeight = 3.dp

        val tabIndicatorHeightOffset = 1.dp
    }

    object UpsellCards {

        val outlineBorderStoke = BorderStroke(1.dp, coloredBorderBrush)
        val outlineUpsellingBorderStoke = BorderStroke(2.dp, onboardingColoredBrush)
    }
    val coloredBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00FF88),
            Color(0xFF0088FF),
            Color(0xFF8800FF),
            Color(0xFFFF0088)
        )
    )

    private val onboardingColoredBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00FF96),
            Color(0xFF00D9FF),
            Color(0xFF2A7FFF),
            Color(0xFFB114F8),
            Color(0xFFFF4FD8)
        )
    )
}
