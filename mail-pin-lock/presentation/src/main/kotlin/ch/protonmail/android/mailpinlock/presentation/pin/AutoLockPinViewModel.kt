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

package ch.protonmail.android.mailpinlock.presentation.pin

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPending
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockInsertionMode
import ch.protonmail.android.mailpinlock.presentation.pin.reducer.AutoLockPinReducer
import ch.protonmail.android.mailpinlock.presentation.pin.ui.AutoLockPinScreen
import ch.protonmail.android.mailsession.data.usecase.SignOutAllAccounts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject

@HiltViewModel
class AutoLockPinViewModel @Inject constructor(
    private val autoLockRepository: AutoLockRepository,
    private val autoLockCheckPendingState: AutoLockCheckPendingState,
    private val signOutAllAccounts: SignOutAllAccounts,
    private val reducer: AutoLockPinReducer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mutableState = MutableStateFlow<AutoLockPinState>(AutoLockPinState.Loading)
    val state = mutableState.asStateFlow()

    internal val pinTextFieldState = TextFieldState("")

    private var temporaryInsertedPin: String? = null

    init {
        val openMode = savedStateHandle.get<String>(AutoLockPinScreen.AutoLockPinModeKey)
            ?.runCatching { deserialize<AutoLockInsertionMode>() }
            ?.getOrNull()

        val step = when (openMode) {
            AutoLockInsertionMode.CreatePin -> PinInsertionStep.PinInsertion
            is AutoLockInsertionMode.VerifyPin -> PinInsertionStep.PinVerification
            else -> PinInsertionStep.PinInsertion
        }

        viewModelScope.launch {
            val remainingAttempts = autoLockRepository.getRemainingAttempts().getOrNull()
            emitNewStateFrom(AutoLockPinEvent.Data.Loaded(step, remainingAttempts))
        }
    }

    fun submit(action: AutoLockPinViewAction) {
        viewModelScope.launch {
            when (action) {
                AutoLockPinViewAction.PerformBack -> onBackPerformed()
                AutoLockPinViewAction.PerformConfirm -> onPerformConfirm()
                AutoLockPinViewAction.RequestSignOut -> onSignOutRequested()
                AutoLockPinViewAction.ConfirmSignOut -> onSignOutConfirmed()
                AutoLockPinViewAction.CancelSignOut -> onSignOutCanceled()
            }
        }
    }

    private fun onBackPerformed() {
        val state = state.value as? AutoLockPinState.DataLoaded ?: return emitNewStateFrom(
            AutoLockPinEvent.Update.OperationAborted
        )

        when (state.pinInsertionState.step) {
            PinInsertionStep.PinInsertion -> emitNewStateFrom(AutoLockPinEvent.Update.OperationAborted)

            PinInsertionStep.PinConfirmation -> emitNewStateFrom(
                AutoLockPinEvent.Update.MovedToStep(PinInsertionStep.PinInsertion)
            )

            PinInsertionStep.PinVerification -> Unit
        }
    }

    private fun handlePinInsertion() {
        if (pinTextFieldState.text.length < MIN_PIN_LENGTH) {
            return emitNewStateFrom(AutoLockPinEvent.Update.Error.PinTooShort)
        }

        temporaryInsertedPin = pinTextFieldState.text.toString()
        pinTextFieldState.edit { delete(0, length) }
        emitNewStateFrom(AutoLockPinEvent.Update.MovedToStep(PinInsertionStep.PinConfirmation))
    }

    private suspend fun handlePinConfirmed() {
        if (temporaryInsertedPin != pinTextFieldState.text.toString()) {
            return emitNewStateFrom(AutoLockPinEvent.Update.Error.NotMatchingPins)
        }

        val autoLockPin = AutoLockPin(pinTextFieldState.text.toString())

        autoLockRepository.setAutoLockPinCode(autoLockPin)
            .onLeft {
                emitNewStateFrom(AutoLockPinEvent.Update.Error.UnknownError)
            }
            .onRight {
                emitNewStateFrom(AutoLockPinEvent.Update.OperationCompleted)
            }
    }

    private suspend fun handlePinVerification() = matchExistingPin {
        emitNewStateFrom(AutoLockPinEvent.Update.VerificationCompleted)
    }

    private suspend inline fun matchExistingPin(continuation: () -> Unit) {
        val autoLockPin = AutoLockPin(pinTextFieldState.text.toString())
        val remainingAttempts = autoLockRepository.getRemainingAttempts().getOrElse {
            return emitNewStateFrom(AutoLockPinEvent.Update.Error.UnknownError)
        }

        autoLockRepository.verifyAutoLockPinCode(autoLockPin).onLeft { it ->
            emitNewStateFrom(
                AutoLockPinEvent.Update.Error.Verify(
                    error = it,
                    remainingAttempts = remainingAttempts
                )
            )
        }.onRight {
            autoLockCheckPendingState.emitCheckPendingState(AutoLockCheckPending(false))
            continuation()
        }
    }

    private suspend fun onPerformConfirm() {
        val state = state.value as? AutoLockPinState.DataLoaded ?: return
        val currentStep = state.pinInsertionState.step

        when (currentStep) {
            PinInsertionStep.PinInsertion -> handlePinInsertion()
            PinInsertionStep.PinConfirmation -> handlePinConfirmed()
            PinInsertionStep.PinVerification -> handlePinVerification()
        }
    }

    private fun onSignOutRequested() {
        emitNewStateFrom(AutoLockPinEvent.Update.SignOutRequested)
    }

    private suspend fun onSignOutConfirmed() {
        signOutAllAccounts().onLeft {
            emitNewStateFrom(AutoLockPinEvent.Update.Error.UnknownError)
        }.onRight {
            emitNewStateFrom(AutoLockPinEvent.Update.SignOutConfirmed)
        }
    }

    private fun onSignOutCanceled() {
        emitNewStateFrom(AutoLockPinEvent.Update.SignOutCanceled)
    }

    private fun emitNewStateFrom(event: AutoLockPinEvent) {
        mutableState.update {
            reducer.newStateFrom(it, event)
        }
    }
}

private const val MIN_PIN_LENGTH = 4
