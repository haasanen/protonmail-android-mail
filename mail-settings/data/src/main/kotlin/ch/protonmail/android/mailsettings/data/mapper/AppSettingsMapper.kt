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

import ch.protonmail.android.mailcommon.data.mapper.LocalAppSettings
import ch.protonmail.android.mailsettings.data.mapper.LocalMapperThemeConstants.defaultThemeFallback
import ch.protonmail.android.mailsettings.data.mapper.LocalMapperThemeConstants.themeAppearanceLookup
import ch.protonmail.android.mailsettings.domain.model.AppLanguage
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.model.Theme
import timber.log.Timber
import uniffi.proton_mail_uniffi.AppAppearance
import uniffi.proton_mail_uniffi.AppSettingsDiff
import uniffi.proton_mail_uniffi.AutoLock.Always
import uniffi.proton_mail_uniffi.AutoLock.Minutes

private object LocalMapperThemeConstants {

    val themeAppearanceLookup =
        mapOf(
            Theme.DARK to AppAppearance.DARK_MODE,
            Theme.LIGHT to AppAppearance.LIGHT_MODE,
            Theme.SYSTEM_DEFAULT to AppAppearance.SYSTEM
        )
    val defaultThemeFallback = AppAppearance.SYSTEM
}

// When creating a diff we should leave unchanged values empty
fun Theme.toLocalAppDiff() = AppSettingsDiff(
    autoLock = null,
    useCombineContacts = null,
    useAlternativeRouting = null,
    appearance = themeAppearanceLookup.getOrElse(this, {
        Timber.e("invalid Theme provided - mapping not currently supported in rust $this")
        defaultThemeFallback
    })
)

fun AppAppearance.toTheme() = themeAppearanceLookup.entries.first { it.value == this }.key

fun LocalAppSettings.toAppSettings(customLanguage: AppLanguage? = null) = AppSettings(
    hasAutoLock = autoLock is Always || autoLock is Minutes,
    hasAlternativeRouting = useAlternativeRouting,
    theme = appearance.toTheme(),
    customAppLanguage = customLanguage?.langName,
    hasDeviceContactsEnabled = useCombineContacts // This should be device contacts, not combined contacts.
)

