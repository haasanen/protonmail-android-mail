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
import ch.protonmail.android.mailcommon.data.mapper.LocalAutolock
import ch.protonmail.android.mailcommon.data.mapper.LocalProtection
import ch.protonmail.android.mailpinlock.model.AutoLockInterval
import ch.protonmail.android.mailpinlock.model.Protection
import ch.protonmail.android.mailsettings.data.mapper.LocalMapperThemeConstants.defaultThemeFallback
import ch.protonmail.android.mailsettings.data.mapper.LocalMapperThemeConstants.themeAppearanceLookup
import ch.protonmail.android.mailsettings.domain.model.AppLanguage
import ch.protonmail.android.mailsettings.domain.model.AppSettings
import ch.protonmail.android.mailsettings.domain.model.Theme
import timber.log.Timber
import uniffi.proton_mail_uniffi.AppAppearance
import uniffi.proton_mail_uniffi.AppProtection
import uniffi.proton_mail_uniffi.AutoLock.Always
import uniffi.proton_mail_uniffi.AutoLock.Minutes
import uniffi.proton_mail_uniffi.AutoLock.Never
import uniffi.proton_mail_uniffi.AppSettingsDiff as LocalAppSettingsDiff

fun AppSettingsDiff.toAppDiff(): LocalAppSettingsDiff {

    fun setTheme(theme: Theme) = themeAppearanceLookup.getOrElse(theme, {
        Timber.e("invalid Theme provided - mapping not currently supported in rust $this")
        defaultThemeFallback
    })

    fun setAutolockInteval(interval: AutoLockInterval) = when (interval) {
        AutoLockInterval.Immediately -> Always
        AutoLockInterval.Never -> Never
        else -> Minutes(interval.duration.inWholeMinutes.toUByte())
    }

    return LocalAppSettingsDiff(
        autoLock = interval?.let { setAutolockInteval(it) },
        useCombineContacts = combineContacts,
        useAlternativeRouting = alternativeRouting,
        appearance = theme?.let { setTheme(it) }
    )
}

private object LocalMapperThemeConstants {

    val themeAppearanceLookup =
        mapOf(
            Theme.DARK to AppAppearance.DARK_MODE,
            Theme.LIGHT to AppAppearance.LIGHT_MODE,
            Theme.SYSTEM_DEFAULT to AppAppearance.SYSTEM
        )
    val defaultThemeFallback = AppAppearance.SYSTEM
}

fun AppAppearance.toTheme() = themeAppearanceLookup.entries.first { it.value == this }.key

fun LocalAutolock.toAutolockInterval() = when (this) {
    is Never -> AutoLockInterval.Never
    is Always -> AutoLockInterval.Immediately
    is Minutes -> AutoLockInterval.fromMinutes(this.v1.toLong())
}

fun LocalProtection.toProtection() = when (this) {
    AppProtection.NONE -> Protection.None
    AppProtection.BIOMETRICS -> Protection.Biometrics
    AppProtection.PIN -> Protection.Pin
}

fun LocalAppSettings.toAppSettings(customLanguage: AppLanguage? = null) = AppSettings(
    autolockProtection = protection.toProtection(),
    autolockInterval = autoLock.toAutolockInterval(),
    hasAlternativeRouting = useAlternativeRouting,
    theme = appearance.toTheme(),
    customAppLanguage = customLanguage?.langName,
    hasCombinedContactsEnabled = useCombineContacts // This should be device contacts, not combined contacts.
)

