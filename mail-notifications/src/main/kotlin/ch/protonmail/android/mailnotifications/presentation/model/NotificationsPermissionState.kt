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

package ch.protonmail.android.mailnotifications.presentation.model

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailnotifications.R

sealed interface NotificationsPermissionState {

    data object Granted : NotificationsPermissionState
    data class RequiresInteraction(val stateType: NotificationsPermissionStateType) : NotificationsPermissionState
    data object NoAction : NotificationsPermissionState
    data object Denied : NotificationsPermissionState
}

sealed class NotificationsPermissionStateType(val uiModel: NotificationsPermissionRequestUiModel) {
    data object FirstTime : NotificationsPermissionStateType(
        uiModel = NotificationsPermissionRequestUiModel(
            title = TextUiModel.TextRes(R.string.notification_permissions_bottomsheet_first_time_title),
            description = TextUiModel.TextRes(R.string.notification_permissions_bottomsheet_first_time_subtitle)
        )
    )
    data object SecondTime : NotificationsPermissionStateType(
        uiModel = NotificationsPermissionRequestUiModel(
            title = TextUiModel.TextRes(R.string.notification_permissions_bottomsheet_second_time_title),
            description = TextUiModel.TextRes(R.string.notification_permissions_bottomsheet_second_time_subtitle)
        )
    )
}

data class NotificationsPermissionRequestUiModel(
    val title: TextUiModel,
    val description: TextUiModel
)
