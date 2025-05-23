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

package ch.protonmail.android.mailsettings.domain.model

data class AppSettings(
    val hasAutoLock: Boolean,
    val hasAlternativeRouting: Boolean,
    val customAppLanguage: String?,
    val hasDeviceContactsEnabled: Boolean,
    val theme: Theme
) {

    companion object {

        private val DEFAULT_THEME = Theme.SYSTEM_DEFAULT
        private val DEFAULT_CUSTOM_LANGUAGE: String? = null
        private const val DEFAULT_HAS_ALTERNATIVE_ROUTING = true
        private const val DEFAULT_HAS_AUTOLOCK = false
        private const val DEFAULT_HAS_DEVICE_CONTACTS_ENABLED = false

        fun default() = AppSettings(
            DEFAULT_HAS_AUTOLOCK,
            DEFAULT_HAS_ALTERNATIVE_ROUTING,
            DEFAULT_CUSTOM_LANGUAGE,
            DEFAULT_HAS_DEVICE_CONTACTS_ENABLED,
            DEFAULT_THEME
        )
    }
}
