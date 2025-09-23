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

package ch.protonmail.android.feature.appicon

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.R
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class AppIconResourceIconManagerImplTest(
    @Suppress("unused") private val testName: String,
    private val iconId: AppIconData.AppIconId,
    @DrawableRes private val expectedResId: Int
) {

    @Test
    fun `returns the correct id for icon resource`() {
        // When
        val iconResourceManager = AppIconResourceManagerImpl()
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
                R.mipmap.ic_launcher
            ),
            arrayOf(
                "dark icon",
                AppIconData.AppIconId.Dark,
                R.mipmap.ic_launcher_dark
            ),
            arrayOf(
                "retro icon",
                AppIconData.AppIconId.Retro,
                R.mipmap.ic_launcher_retro
            ),
            arrayOf(
                "calculator icon",
                AppIconData.AppIconId.Weather,
                R.mipmap.ic_launcher_weather
            ),
            arrayOf(
                "notes icon",
                AppIconData.AppIconId.Notes,
                R.mipmap.ic_launcher_notes
            ),
            arrayOf(
                "weather icon",
                AppIconData.AppIconId.Calculator,
                R.mipmap.ic_launcher_calculator
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
        val iconResourceManager = AppIconResourceManagerImpl()
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
                R.string.app_name
            ),
            arrayOf(
                "dark icon",
                AppIconData.AppIconId.Dark,
                R.string.app_icon_name_dark
            ),
            arrayOf(
                "retro icon",
                AppIconData.AppIconId.Retro,
                R.string.app_icon_name_retro
            ),
            arrayOf(
                "weather icon",
                AppIconData.AppIconId.Weather,
                R.string.app_icon_name_weather
            ),
            arrayOf(
                "notes icon",
                AppIconData.AppIconId.Notes,
                R.string.app_icon_name_notes
            ),
            arrayOf(
                "calculator icon",
                AppIconData.AppIconId.Calculator,
                R.string.app_icon_name_calculator
            )
        )
    }
}
