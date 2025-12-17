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

package ch.protonmail.android.mailsettings.presentation.appicon

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.appicon.AppIconResourceManager
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class AppIconResourceIconManagerTest(
    @Suppress("unused") private val testName: String,
    private val iconId: AppIconData.AppIconId,
    @DrawableRes private val expectedResId: Int
) {

    @Test
    fun `returns the correct id for icon resource`() {
        // When
        val iconResourceManager = AppIconResourceManager()
        val actual = iconResourceManager.getIconRes(iconId)

        // Then
        assertEquals(expectedResId, actual)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "default icon",
                AppIconData.AppIconId.Default,
                R.drawable.ic_launcher_main_raster
            ),
            arrayOf(
                "calculator icon",
                AppIconData.AppIconId.Weather,
                R.drawable.ic_launcher_weather_raster
            ),
            arrayOf(
                "notes icon",
                AppIconData.AppIconId.Notes,
                R.drawable.ic_launcher_notes_raster
            ),
            arrayOf(
                "weather icon",
                AppIconData.AppIconId.Calculator,
                R.drawable.ic_launcher_calculator_raster
            )
        )
    }
}

@RunWith(Parameterized::class)
internal class AppIconResourceStringManagerImplTest(
    @Suppress("unused") private val testName: String,
    private val iconId: AppIconData.AppIconId,
    @StringRes private val expectedResId: Int
) {

    @Test
    fun `returns the correct id for string resource`() {
        // When
        val iconResourceManager = AppIconResourceManager()
        val actual = iconResourceManager.getDescriptionStringRes(iconId)

        // Then
        assertEquals(expectedResId, actual)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "default icon",
                AppIconData.AppIconId.Default,
                R.string.settings_app_icon_name_default
            ),
            arrayOf(
                "weather icon",
                AppIconData.AppIconId.Weather,
                R.string.settings_app_icon_name_discreet
            ),
            arrayOf(
                "notes icon",
                AppIconData.AppIconId.Notes,
                R.string.settings_app_icon_name_discreet
            ),
            arrayOf(
                "calculator icon",
                AppIconData.AppIconId.Calculator,
                R.string.settings_app_icon_name_discreet
            )
        )
    }
}
