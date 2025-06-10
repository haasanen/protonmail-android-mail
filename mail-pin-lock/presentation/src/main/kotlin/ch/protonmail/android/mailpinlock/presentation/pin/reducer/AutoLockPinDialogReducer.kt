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

package ch.protonmail.android.mailpinlock.presentation.pin.reducer

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockDialogState
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockPinDialogEvent
import javax.inject.Inject

class AutoLockPinDialogReducer @Inject constructor(
    private val errorsUiMapper: AutoLockPinErrorUiMapper
) {

    fun newStateFrom(currentState: AutoLockDialogState, operation: AutoLockPinDialogEvent) = when (operation) {
        is AutoLockPinDialogEvent.Error -> handleError(currentState, operation)
        AutoLockPinDialogEvent.Success -> handleSuccess(currentState)
    }


    private fun handleError(state: AutoLockDialogState, event: AutoLockPinDialogEvent.Error): AutoLockDialogState {
        val error = if (event.remainingAttempts != null) {
            errorsUiMapper.toUiErrorWithRemainingAttempts(event.error, event.remainingAttempts)
        } else {
            TextUiModel.TextRes(R.string.mail_settings_pin_insertion_error_unknown)
        }
        return state.copy(error = error)
    }

    private fun handleSuccess(state: AutoLockDialogState) = state.copy(successEffect = Effect.Companion.of(Unit))
}
