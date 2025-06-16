/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package ch.protonmail.android.design.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Display
val ProtonTypography.displayLargeNorm: TextStyle
    @Composable get() = displayLarge.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.displayMediumNorm: TextStyle
    @Composable get() = displayMedium.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.displaySmallNorm: TextStyle
    @Composable get() = displaySmall.copy(color = ProtonTheme.colors.textNorm)

// Headline
val ProtonTypography.headlineLargeNorm: TextStyle
    @Composable get() = headlineLarge.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.headlineMediumNorm: TextStyle
    @Composable get() = headlineMedium.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.headlineSmallNorm: TextStyle
    @Composable get() = headlineSmall.copy(color = ProtonTheme.colors.textNorm)

// Title
val ProtonTypography.titleLargeNorm: TextStyle
    @Composable get() = titleLarge.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.titleMediumNorm: TextStyle
    @Composable get() = titleMedium.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.titleSmallNorm: TextStyle
    @Composable get() = titleSmall.copy(color = ProtonTheme.colors.textNorm)

// Label
val ProtonTypography.labelLargeNorm: TextStyle
    @Composable get() = labelLarge.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.labelLargeInverted: TextStyle
    @Composable get() = labelLarge.copy(color = ProtonTheme.colors.textInverted)

val ProtonTypography.labelMediumNorm: TextStyle
    @Composable get() = labelMedium.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.labelSmallNorm: TextStyle
    @Composable get() = labelSmall.copy(color = ProtonTheme.colors.textNorm)

// Body
val ProtonTypography.bodyLargeNorm: TextStyle
    @Composable get() = bodyLarge.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.bodyLargeHint: TextStyle
    @Composable get() = bodyLarge.copy(color = ProtonTheme.colors.textHint)

val ProtonTypography.bodyLargeWeak: TextStyle
    @Composable get() = bodyLarge.copy(color = ProtonTheme.colors.textWeak)

val ProtonTypography.bodyLargeInverted: TextStyle
    @Composable get() = bodyLarge.copy(color = ProtonTheme.colors.textInverted)

val ProtonTypography.bodyMediumNorm: TextStyle
    @Composable get() = bodyMedium.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.bodyMediumWeak: TextStyle
    @Composable get() = bodyMedium.copy(color = ProtonTheme.colors.textWeak)

val ProtonTypography.bodyMediumInverted: TextStyle
    @Composable get() = bodyMedium.copy(color = ProtonTheme.colors.textInverted)

val ProtonTypography.bodySmallNorm: TextStyle
    @Composable get() = bodySmall.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.bodySmallWeak: TextStyle
    @Composable get() = bodySmall.copy(color = ProtonTheme.colors.textWeak)

val ProtonTypography.bodySmallHint: TextStyle
    @Composable get() = bodySmall.copy(color = ProtonTheme.colors.textHint)


@Immutable
data class ProtonTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleLargeMedium: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle
) {
    // Default constructor with fallback to the Default object
    constructor() : this(
        displayLarge = Default.displayLarge,
        displayMedium = Default.displayMedium,
        displaySmall = Default.displaySmall,
        headlineLarge = Default.headlineLarge,
        headlineMedium = Default.headlineMedium,
        headlineSmall = Default.headlineSmall,
        titleLarge = Default.titleLarge,
        titleLargeMedium = Default.titleLargeMedium,
        titleMedium = Default.titleMedium,
        titleSmall = Default.titleSmall,
        labelLarge = Default.labelLarge,
        labelMedium = Default.labelMedium,
        labelSmall = Default.labelSmall,
        bodyLarge = Default.bodyLarge,
        bodyMedium = Default.bodyMedium,
        bodySmall = Default.bodySmall
    )

    companion object {
        val Default = ProtonTypography(
            displayLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp
            ),
            displayMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp
            ),
            displaySmall = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            titleLargeMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            ),
            titleSmall = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
            labelLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
            labelMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
            labelSmall = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            ),
            bodySmall = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            )
        )
    }
}

internal fun ProtonTypography.toMaterial3ThemeTypography() = Typography(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall
)

val LocalTypography = staticCompositionLocalOf { ProtonTypography() }
