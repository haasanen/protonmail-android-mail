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

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Data
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.BiometricStateChanged
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.Error
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.MovedToStep
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.OperationAborted
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.OperationCompleted
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.PinValueChanged
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.SignOutCanceled
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.SignOutConfirmed
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.SignOutRequested
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent.Update.VerificationCompleted
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockBiometricPinUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockBiometricPromptUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinStepUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockSuccessfulOperationUiMapper
import javax.inject.Inject

class AutoLockPinReducer @Inject constructor(
    private val stepUiMapper: AutoLockPinStepUiMapper,
    private val successfulOperationUiMapper: AutoLockSuccessfulOperationUiMapper,
    private val errorsUiMapper: AutoLockPinErrorUiMapper,
    private val biometricPinUiMapper: AutoLockBiometricPinUiMapper,
    private val biometricPromptUiMapper: AutoLockBiometricPromptUiMapper
) {

    fun newStateFrom(currentState: AutoLockPinState, operation: AutoLockPinEvent) =
        currentState.toNewStateFromEvent(operation)

    @Suppress("ComplexMethod")
    private fun AutoLockPinState.toNewStateFromEvent(event: AutoLockPinEvent): AutoLockPinState {
        return when (this) {
            is AutoLockPinState.Loading -> when (event) {
                is Data.Loaded -> event.toDataState()
                else -> this
            }

            is AutoLockPinState.DataLoaded -> when (event) {
                is BiometricStateChanged -> updateBiometricState(this, event)
                is PinValueChanged -> updatePinValue(this, event)
                is MovedToStep -> moveToStep(this, event.step)
                is OperationAborted -> abortOperation(this)
                is OperationCompleted -> completeOperation(this)
                is VerificationCompleted -> completeVerification(this)
                is Error -> handleError(this, event)
                SignOutRequested -> handleSignOutRequested(this)
                SignOutCanceled -> handleSignOutCanceled(this)
                SignOutConfirmed -> handleSignOutConfirmed(this)
                else -> this
            }
        }
    }

    private fun handleError(state: AutoLockPinState.DataLoaded, event: Error): AutoLockPinState.DataLoaded =
        when (event) {
            is Error.WrongPinCode -> {
                state.copy(
                    pinInsertionState = state.pinInsertionState.copy(remainingAttempts = event.remainingAttempts),
                    pinInsertionErrorEffect = Effect.of(errorsUiMapper.toUiModel(event))
                )
            }

            else -> state.copy(pinInsertionErrorEffect = Effect.of(errorsUiMapper.toUiModel(event)))
        }

    private fun abortOperation(state: AutoLockPinState.DataLoaded) = state.copy(closeScreenEffect = Effect.of(Unit))

    private fun completeOperation(state: AutoLockPinState.DataLoaded): AutoLockPinState.DataLoaded {
        val closeEffect = Effect.of(Unit)
        val snackbarEffect = successfulOperationUiMapper.toTextUiModel(state.pinInsertionState.startingStep)
            ?.let { Effect.of(it) }
            ?: Effect.empty()
        return state.copy(closeScreenEffect = closeEffect, snackbarSuccessEffect = snackbarEffect)
    }

    private fun completeVerification(state: AutoLockPinState.DataLoaded): AutoLockPinState.DataLoaded =
        state.copy(closeScreenEffect = Effect.of(Unit))

    private fun moveToStep(state: AutoLockPinState.DataLoaded, step: PinInsertionStep): AutoLockPinState {
        val confirmButtonUiModel = stepUiMapper.toConfirmButtonUiModel(isEnabled = false, step)
        val topBarUiModel = stepUiMapper.toTopBarUiModel(step)

        val newTopBarState = AutoLockPinState.TopBarState(topBarUiModel)
        val newConfirmButtonState = AutoLockPinState.ConfirmButtonState(confirmButtonUiModel)
        val newPinInsertionState = AutoLockPinState.PinInsertionState(
            startingStep = state.pinInsertionState.startingStep,
            step = step,
            remainingAttempts = PinVerificationRemainingAttempts.Default,
            pinInsertionUiModel = PinInsertionUiModel(InsertedPin.Empty)
        )

        return state.copy(
            pinInsertionState = newPinInsertionState,
            topBarState = newTopBarState,
            confirmButtonState = newConfirmButtonState,
            pinInsertionErrorEffect = Effect.empty()
        )
    }

    private fun updateBiometricState(
        state: AutoLockPinState.DataLoaded,
        event: BiometricStateChanged
    ): AutoLockPinState.DataLoaded {
        return state.copy(
            biometricPinState = biometricPinUiMapper.toUiModel(event.biometricState, state.pinInsertionState.step)
        )
    }

    private fun updatePinValue(
        state: AutoLockPinState.DataLoaded,
        event: PinValueChanged
    ): AutoLockPinState.DataLoaded {
        val newInsertedPin = event.newPin
        val pinInsertionState = state.pinInsertionState.copy(
            step = state.pinInsertionState.step,
            pinInsertionUiModel = PinInsertionUiModel(
                newInsertedPin
            )
        )

        val confirmButtonUiModel =
            state.confirmButtonState.confirmButtonUiModel.copy(isEnabled = newInsertedPin.hasValidLength())
        val confirmButtonState = state.confirmButtonState.copy(confirmButtonUiModel = confirmButtonUiModel)
        return state.copy(pinInsertionState = pinInsertionState, confirmButtonState = confirmButtonState)
    }

    private fun handleSignOutRequested(state: AutoLockPinState.DataLoaded): AutoLockPinState.DataLoaded {
        val uiModel = state.signOutButtonState.signOutUiModel.copy(isRequested = true)
        return state.copy(signOutButtonState = AutoLockPinState.SignOutButtonState(uiModel))
    }

    private fun handleSignOutCanceled(state: AutoLockPinState.DataLoaded): AutoLockPinState.DataLoaded {
        val uiModel = state.signOutButtonState.signOutUiModel.copy(isRequested = false)
        return state.copy(signOutButtonState = AutoLockPinState.SignOutButtonState(uiModel))
    }

    private fun handleSignOutConfirmed(state: AutoLockPinState.DataLoaded): AutoLockPinState.DataLoaded =
        state.copy(closeScreenEffect = Effect.of(Unit))

    private fun Data.Loaded.toDataState(): AutoLockPinState.DataLoaded {
        val biometricPinState = biometricPinUiMapper.toUiModel(initialBiometricsState, step)
        val showBiometricPromptEffect = biometricPromptUiMapper.toUiModel(initialBiometricsState)
        val pinInsertionUiModel =
            PinInsertionUiModel(InsertedPin.Empty)
        val topBarUiModel = stepUiMapper.toTopBarUiModel(step)
        val confirmButtonUiModel = stepUiMapper.toConfirmButtonUiModel(isEnabled = false, step)
        val signOutUiModel = stepUiMapper.toSignOutUiModel(step)
        val errorEffect = errorsUiMapper.toUiModel(remainingAttempts)?.let { Effect.of(it) } ?: Effect.empty()

        return AutoLockPinState.DataLoaded(
            topBarState = AutoLockPinState.TopBarState(topBarUiModel),
            pinInsertionState = AutoLockPinState.PinInsertionState(
                startingStep = step,
                step = step,
                remainingAttempts = remainingAttempts,
                pinInsertionUiModel = pinInsertionUiModel
            ),
            confirmButtonState = AutoLockPinState.ConfirmButtonState(confirmButtonUiModel),
            signOutButtonState = AutoLockPinState.SignOutButtonState(signOutUiModel),
            biometricPinState = biometricPinState,
            showBiometricPromptEffect = showBiometricPromptEffect,
            closeScreenEffect = Effect.empty(),
            pinInsertionErrorEffect = errorEffect,
            snackbarSuccessEffect = Effect.empty()
        )
    }
}
