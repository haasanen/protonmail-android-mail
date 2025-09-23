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

package ch.protonmail.android.mailsettings.presentation.appsettings.usecase

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.presentation.settings.appicon.AppIconManager
import ch.protonmail.android.mailsettings.presentation.settings.appicon.AppIconResourceManager
import javax.inject.Inject

internal class GetAppIconDescription @Inject constructor(
    private val appIconManager: AppIconManager,
    private val appIconResourceManager: AppIconResourceManager
) {

    operator fun invoke(): TextUiModel {
        val iconId = appIconManager.getCurrentIconData().id
        val descriptionRes = appIconResourceManager.getDescriptionStringRes(iconId)

        return TextUiModel.TextRes(descriptionRes)
    }
}
