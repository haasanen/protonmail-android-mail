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

import androidx.lifecycle.ViewModel
import ch.protonmail.android.mailsettings.presentation.settings.appicon.mapper.AppIconDataMapper
import ch.protonmail.android.mailsettings.presentation.settings.appicon.model.AppIconUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

@HiltViewModel
internal class AppIconSettingsViewModel @Inject constructor(
    private val appIconManager: AppIconManager,
    private val appIconDataMapper: AppIconDataMapper
) : ViewModel() {

    fun getCurrentAppIcon(): AppIconUiModel {
        val appIcon = appIconManager.getCurrentIconData()
        return appIconDataMapper.toUiModel(appIcon)
    }

    fun getAvailableIcons(): ImmutableList<AppIconUiModel> =
        appIconManager.getAvailableIcons().map { appIconDataMapper.toUiModel(it) }.toImmutableList()

    fun setNewAppIcon(newIcon: AppIconUiModel) {
        appIconManager.setNewAppIcon(newIcon.data)
    }
}
