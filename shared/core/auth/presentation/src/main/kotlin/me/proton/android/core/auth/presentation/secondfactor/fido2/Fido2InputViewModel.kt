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

package me.proton.android.core.auth.presentation.secondfactor.fido2

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import me.proton.android.core.auth.presentation.secondfactor.fido2.Fido2InputAction.Close
import me.proton.android.core.auth.presentation.secondfactor.fido2.Fido2InputAction.Load
import me.proton.android.core.auth.presentation.secondfactor.fido2.Fido2InputState.Idle
import me.proton.core.compose.viewmodel.BaseViewModel
import javax.inject.Inject

class Fido2InputViewModel @Inject constructor() : BaseViewModel<Fido2InputAction, Fido2InputState>(
    initialAction = Load(),
    initialState = Idle
) {

    override fun onAction(action: Fido2InputAction): Flow<Fido2InputState> {
        return when (action) {
            is Load -> onLoad()
            is Close -> onClose()
            is Fido2InputAction.Authenticate -> onValidateAndAuthenticate(action)
        }
    }

    override suspend fun FlowCollector<Fido2InputState>.onError(throwable: Throwable) {
        emit(Fido2InputState.Error(throwable.message))
    }

    private fun onLoad() = flow<Fido2InputState> { }
    private fun onClose() = flow<Fido2InputState> { }
    private fun onValidateAndAuthenticate(action: Fido2InputAction) = flow<Fido2InputState> { }
}
