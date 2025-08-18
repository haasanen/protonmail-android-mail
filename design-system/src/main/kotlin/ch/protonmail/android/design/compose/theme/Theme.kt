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

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun ProtonTheme(
    isDark: Boolean = isNightMode(),
    localInspectionMode: Boolean = LocalInspectionMode.current,
    colors: ProtonColors = if (isDark) ProtonColors.Dark else ProtonColors.Light,
    typography: ProtonTypography = ProtonTypography.Default,
    shapes: ProtonShapes = ProtonShapes(),
    content: @Composable () -> Unit
) {
    val rememberedColors = remember { colors.copy() }.apply { updateColorsFrom(colors) }

    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        LocalTypography provides typography,
        LocalShapes provides shapes,
        LocalContentColor provides rememberedColors.textNorm,
        LocalInspectionMode provides localInspectionMode
    ) {
        androidx.compose.material3.MaterialTheme(
            typography = typography.toMaterial3ThemeTypography(),
            colorScheme = rememberedColors.toMaterial3ThemeColors(),
            content = content
        )
    }
}

@Composable
fun ProtonSidebarTheme(content: @Composable () -> Unit) {
    val isDark: Boolean = isNightMode()
    val protonColors: ProtonColors = if (isDark) ProtonColors.Dark else ProtonColors.Light
    val sidebarColors = protonColors.let {
        it.copy(
            backgroundNorm = it.sidebarBackground,
            interactionWeakNorm = it.sidebarInteractionPressed,
            interactionWeakPressed = it.sidebarInteractionPressed,
            separatorNorm = it.sidebarSeparator,
            textNorm = it.sidebarTextNorm,
            textWeak = it.sidebarTextWeak,
            textSelected = it.sidebarTextSelected,
            iconNorm = it.sidebarIconNorm,
            iconWeak = it.sidebarIconWeak,
            iconSelected = it.sidebarIconSelected,
            interactionBrandDefaultPressed = it.sidebarInteractionPressed
        )
    }

    ProtonTheme(
        isDark = isDark,
        colors = sidebarColors,
        content = content
    )
}

@Composable
fun ProtonInvertedTheme(content: @Composable () -> Unit) {
    val isDark: Boolean = isNightMode()
    val protonColors: ProtonColors = if (isDark) ProtonColors.Dark else ProtonColors.Light
    val invertedColors = protonColors.let {
        it.copy(
            backgroundNorm = it.backgroundInvertedNorm,
            backgroundSecondary = it.backgroundInvertedSecondary
        )
    }

    ProtonTheme(
        isDark = isDark,
        colors = invertedColors,
        content = content
    )
}

@Composable
fun isNightMode() = when (AppCompatDelegate.getDefaultNightMode()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> isSystemInDarkTheme()
}

object ProtonTheme {

    val colors: ProtonColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: ProtonTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shapes: ProtonShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}
