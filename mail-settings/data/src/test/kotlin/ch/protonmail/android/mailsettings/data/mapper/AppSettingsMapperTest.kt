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

package ch.protonmail.android.mailsettings.data.mapper

import ch.protonmail.android.mailsettings.domain.model.AppLanguage
import ch.protonmail.android.mailsettings.domain.model.Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uniffi.proton_mail_uniffi.AppAppearance
import uniffi.proton_mail_uniffi.AppProtection
import uniffi.proton_mail_uniffi.AppSettings
import uniffi.proton_mail_uniffi.AutoLock
import kotlin.test.assertNull

class AppSettingsMapperTest {

    private val localAppSettings = AppSettings(
        appearance = AppAppearance.DARK_MODE,
        AppProtection.PIN,
        AutoLock.Always,
        useCombineContacts = false,
        useAlternativeRouting = false
    )


    @Test
    fun `map AppAppearance to theme`() {
        assertEquals(Theme.DARK, AppAppearance.DARK_MODE.toTheme())
        assertEquals(Theme.LIGHT, AppAppearance.LIGHT_MODE.toTheme())
        assertEquals(Theme.SYSTEM_DEFAULT, AppAppearance.SYSTEM.toTheme())
    }

    @Test
    fun `when map LocalAppSettings then App Theme is mapped`() {
        assertEquals(Theme.DARK, localAppSettings.toAppSettings().theme)
        assertEquals(
            Theme.LIGHT,
            localAppSettings.copy(appearance = AppAppearance.LIGHT_MODE)
                .toAppSettings()
                .theme
        )

        assertEquals(
            Theme.SYSTEM_DEFAULT,
            localAppSettings.copy(appearance = AppAppearance.SYSTEM)
                .toAppSettings()
                .theme
        )
    }

    @Test
    fun `when map LocalAppSettings then Autolock is mapped`() {
        assertTrue(
            localAppSettings.copy(autoLock = AutoLock.Always)
                .toAppSettings()
                .hasAutoLock
        )

        assertTrue(
            localAppSettings.copy(autoLock = AutoLock.Minutes(UByte.MIN_VALUE))
                .toAppSettings()
                .hasAutoLock
        )
        assertFalse(
            localAppSettings.copy(autoLock = AutoLock.Never)
                .toAppSettings()
                .hasAutoLock
        )
    }

    @Test
    fun `when map LocalAppSettings AND no language set then language is null`() {
        assertNull(
            localAppSettings
                .toAppSettings(customLanguage = null)
                .customAppLanguage
        )
    }

    @Test
    fun `when map LocalAppSettings AND language set THEN language is mapped`() {
        assertEquals(
            "Français",
            localAppSettings
                .toAppSettings(customLanguage = AppLanguage.FRENCH)
                .customAppLanguage
        )
    }
}
