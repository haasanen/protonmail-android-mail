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

package ch.protonmail.android.mailsettings.presentation.settings.appicon

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconData.AppIconId
import javax.inject.Inject

internal class AppIconResourceManager @Inject constructor() {

    @StringRes
    fun getDescriptionStringRes(id: AppIconId): Int {
        return when (id) {
            AppIconId.Default -> R.string.settings_app_icon_name_default
            else -> R.string.settings_app_icon_name_discreet
        }
    }

    @DrawableRes
    fun getIconRes(id: AppIconId): Int {
        // Return rasterized versions, not mipmaps
        return when (id) {
            AppIconId.Default -> R.drawable.ic_launcher_main_raster
            AppIconId.Weather -> R.drawable.ic_launcher_weather_raster
            AppIconId.Notes -> R.drawable.ic_launcher_notes_raster
            AppIconId.Calculator -> R.drawable.ic_launcher_calculator_raster
        }
    }
}
