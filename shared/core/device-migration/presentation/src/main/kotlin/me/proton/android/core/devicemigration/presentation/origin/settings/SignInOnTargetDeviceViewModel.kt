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

package me.proton.android.core.devicemigration.presentation.origin.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import me.proton.android.core.devicemigration.presentation.origin.usecase.IsQrLoginAvailableOnOrigin
import me.proton.core.compose.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
public class SignInOnTargetDeviceViewModel @Inject internal constructor(
    private val isQrLoginAvailableOnOrigin: IsQrLoginAvailableOnOrigin
) : BaseViewModel<SignInOnTargetDeviceAction, SignInOnTargetDeviceState>(
    initialAction = SignInOnTargetDeviceAction.Load,
    initialState = SignInOnTargetDeviceState.Visible(isEnabled = false)
) {

    override fun onAction(action: SignInOnTargetDeviceAction): Flow<SignInOnTargetDeviceState> = when (action) {
        SignInOnTargetDeviceAction.Load -> onLoad()
    }

    override suspend fun FlowCollector<SignInOnTargetDeviceState>.onError(throwable: Throwable) {
        emit(SignInOnTargetDeviceState.Hidden)
    }

    private fun onLoad(): Flow<SignInOnTargetDeviceState> = flow {
        if (isQrLoginAvailableOnOrigin()) {
            emit(SignInOnTargetDeviceState.Visible(isEnabled = true))
        } else {
            emit(SignInOnTargetDeviceState.Hidden)
        }
    }
}
