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

package ch.protonmail.android.mailnotifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.design.compose.viewmodel.stopTimeoutMillis
import ch.protonmail.android.mailnotifications.data.model.NotificationsPermissionRequestAttempts
import ch.protonmail.android.mailnotifications.data.repository.NotificationsPermissionRepository
import ch.protonmail.android.mailnotifications.domain.proxy.NotificationManagerCompatProxy
import ch.protonmail.android.mailnotifications.presentation.model.NotificationsPermissionState
import ch.protonmail.android.mailnotifications.presentation.model.PermissionRequestedHolder
import ch.protonmail.android.mailnotifications.presentation.model.NotificationsPermissionStateType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsPermissionViewModel @Inject constructor(
    private val notificationsPermissionRepository: NotificationsPermissionRepository,
    private val notificationManagerCompatProxy: NotificationManagerCompatProxy,
    private val permissionRequestedHolder: PermissionRequestedHolder
) : ViewModel() {

    val state: StateFlow<NotificationsPermissionState> = channelFlow {
        if (notificationManagerCompatProxy.areNotificationsEnabled()) {
            send(NotificationsPermissionState.Granted)
        } else {
            combine(
                permissionRequestedHolder.value,
                notificationsPermissionRepository.observePermissionsRequestsAttempts()
            ) { requested, attempts ->
                if (requested) NotificationsPermissionState.NoAction else attempts.toPermissionsState()
            }.collect { send(it) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = NotificationsPermissionState.NoAction
    )

    fun trackPermissionRequested() {
        viewModelScope.launch {
            permissionRequestedHolder.trackRequest()
            notificationsPermissionRepository.increasePermissionsRequestAttempts()
        }
    }

    private fun NotificationsPermissionRequestAttempts.toPermissionsState(): NotificationsPermissionState {
        return when (value) {
            0 -> NotificationsPermissionState.RequiresInteraction(NotificationsPermissionStateType.FirstTime)
            1 -> NotificationsPermissionState.RequiresInteraction(NotificationsPermissionStateType.SecondTime)
            else -> NotificationsPermissionState.Denied
        }
    }
}
