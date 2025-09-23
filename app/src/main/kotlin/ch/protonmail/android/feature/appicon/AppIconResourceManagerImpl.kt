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

import ch.protonmail.android.R
import ch.protonmail.android.mailsettings.presentation.settings.appicon.AppIconResourceManager
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData.AppIconId
import javax.inject.Inject

class AppIconResourceManagerImpl @Inject constructor() : AppIconResourceManager {

    override fun getDescriptionStringRes(id: AppIconId): Int {
        return when (id) {
            AppIconId.Default -> R.string.app_name
            AppIconId.Dark -> R.string.app_icon_name_dark
            AppIconId.Retro -> R.string.app_icon_name_retro
            AppIconId.Weather -> R.string.app_icon_name_weather
            AppIconId.Notes -> R.string.app_icon_name_notes
            AppIconId.Calculator -> R.string.app_icon_name_calculator
        }
    }

    override fun getIconRes(id: AppIconId): Int {
        return when (id) {
            AppIconId.Default -> R.mipmap.ic_launcher
            AppIconId.Dark -> R.mipmap.ic_launcher_dark
            AppIconId.Retro -> R.mipmap.ic_launcher_retro
            AppIconId.Weather -> R.mipmap.ic_launcher_weather
            AppIconId.Notes -> R.mipmap.ic_launcher_notes
            AppIconId.Calculator -> R.mipmap.ic_launcher_calculator
        }
    }
}
