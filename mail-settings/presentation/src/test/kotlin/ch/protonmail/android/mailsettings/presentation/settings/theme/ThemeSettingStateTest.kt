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

package ch.protonmail.android.mailsettings.presentation.settings.theme

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.Theme.DARK
import ch.protonmail.android.mailsettings.domain.model.Theme.LIGHT
import ch.protonmail.android.mailsettings.domain.model.Theme.SYSTEM_DEFAULT
import ch.protonmail.android.mailsettings.presentation.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeSettingStateTest {

    @Test
    fun `Given a uiModel State returns correct theme`() {
        val sut = ThemeSettingsState.Data(
            SYSTEM_DEFAULT,
            expectedThemes
        )
        assertEquals(LIGHT, sut.themeFor(lightTheme))
        assertEquals(DARK, sut.themeFor(darkTheme))
        assertEquals(SYSTEM_DEFAULT, sut.themeFor(systemDefaultTheme))
    }

    @Test
    fun `Given current theme is LIGHT then state returns expected selected Theme uiModel`() {
        val sut = ThemeSettingsState.Data(
            LIGHT,
            expectedThemes
        )
        assertEquals(lightTheme, sut.selectedTheme)
    }

    @Test
    fun `Given current theme is DARK then state returns expected selected Theme uiModel`() {
        val sut = ThemeSettingsState.Data(
            DARK,
            expectedThemes
        )
        assertEquals(darkTheme, sut.selectedTheme)
    }

    @Test
    fun `Given current theme is DEFAULT then state returns expected selected Theme uiModel`() {
        val sut = ThemeSettingsState.Data(
            SYSTEM_DEFAULT,
            expectedThemes
        )
        assertEquals(systemDefaultTheme, sut.selectedTheme)
    }

    companion object {

        private val systemDefaultTheme = TextUiModel(
            R.string.mail_settings_system_default
        )

        private val lightTheme = TextUiModel(
            R.string.mail_settings_theme_light
        )

        private val darkTheme = TextUiModel(
            R.string.mail_settings_theme_dark
        )

        val expectedThemes = mapOf(
            SYSTEM_DEFAULT to systemDefaultTheme,
            LIGHT to lightTheme,
            DARK to darkTheme
        )
    }
}