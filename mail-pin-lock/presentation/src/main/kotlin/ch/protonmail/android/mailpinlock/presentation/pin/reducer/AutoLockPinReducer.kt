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
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinStepUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockSuccessfulOperationUiMapper
import javax.inject.Inject

class AutoLockPinReducer @Inject constructor(
    private val stepUiMapper: AutoLockPinStepUiMapper,
    private val successfulOperationUiMapper: AutoLockSuccessfulOperationUiMapper,
    private val errorsUiMapper: AutoLockPinErrorUiMapper
) {

    fun newStateFrom(currentState: AutoLockPinState, operation: AutoLockPinEvent) =
        currentState.toNewStateFromEvent(operation)

    @Suppress("ComplexMethod")
    private fun AutoLockPinState.toNewStateFromEvent(event: AutoLockPinEvent): AutoLockPinState {
        return when (this) {
            is AutoLockPinState.Loading -> when (event) {
                is AutoLockPinEvent.Data.Loaded -> event.toDataState()
                else -> this
            }

            is AutoLockPinState.DataLoaded -> when (event) {
                is AutoLockPinEvent.Update.MovedToStep -> moveToStep(this, event.step)
                is AutoLockPinEvent.Update.OperationAborted -> abortOperation(this)
                is AutoLockPinEvent.Update.OperationCompleted -> completeOperation(this)
                is AutoLockPinEvent.Update.VerificationCompleted -> completeVerification(this)
                is AutoLockPinEvent.Update.Error -> handleError(this, event)
                AutoLockPinEvent.Update.SignOutRequested -> handleSignOutRequested(this)
                AutoLockPinEvent.Update.SignOutCanceled -> handleSignOutCanceled(this)
                AutoLockPinEvent.Update.SignOutConfirmed -> handleSignOutConfirmed(this)
                else -> this
            }
        }
    }

    private fun handleError(
        state: AutoLockPinState.DataLoaded,
        event: AutoLockPinEvent.Update.Error
    ): AutoLockPinState.DataLoaded = state.copy(
        pinInsertionState = state.pinInsertionState.copy(
            error = errorsUiMapper.toUiModel(event),
            triggerError = Effect.of(Unit)
        )
    )

    private fun abortOperation(state: AutoLockPinState.DataLoaded) =
        state.copy(closeScreenEffect = Effect.Companion.of(Unit))

    private fun completeOperation(state: AutoLockPinState.DataLoaded): AutoLockPinState.DataLoaded {
        val closeEffect = Effect.Companion.of(Unit)
        val snackbarEffect = successfulOperationUiMapper.toTextUiModel(state.pinInsertionState.startingStep)
            ?.let { Effect.Companion.of(it) }
            ?: Effect.Companion.empty()
        return state.copy(closeScreenEffect = closeEffect, snackbarSuccessEffect = snackbarEffect)
    }

    private fun completeVerification(state: AutoLockPinState.DataLoaded): AutoLockPinState.DataLoaded =
        state.copy(closeScreenEffect = Effect.Companion.of(Unit))

    private fun moveToStep(state: AutoLockPinState.DataLoaded, step: PinInsertionStep): AutoLockPinState {
        val confirmButtonUiModel = stepUiMapper.toConfirmButtonUiModel(isEnabled = false, step)
        val topBarUiModel = stepUiMapper.toTopBarUiModel(step)
        val descriptionUiModel = stepUiMapper.toDescriptionUiModel(step)

        val newTopBarState = AutoLockPinState.TopBarState(topBarUiModel)
        val newConfirmButtonState = AutoLockPinState.ConfirmButtonState(confirmButtonUiModel)
        val newPinInsertionState = AutoLockPinState.PinInsertionState(
            descriptionUiModel = descriptionUiModel,
            startingStep = state.pinInsertionState.startingStep,
            step = step,
            remainingAttempts = null,
            triggerError = Effect.empty()
        )

        return state.copy(
            pinInsertionState = newPinInsertionState,
            topBarState = newTopBarState,
            confirmButtonState = newConfirmButtonState
        )
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
        state.copy(closeScreenEffect = Effect.Companion.of(Unit))

    private fun AutoLockPinEvent.Data.Loaded.toDataState(): AutoLockPinState.DataLoaded {
        val topBarUiModel = stepUiMapper.toTopBarUiModel(step)
        val descriptionUiModel = stepUiMapper.toDescriptionUiModel(step)
        val confirmButtonUiModel = stepUiMapper.toConfirmButtonUiModel(isEnabled = false, step)
        val signOutUiModel = stepUiMapper.toSignOutUiModel(step)

        return AutoLockPinState.DataLoaded(
            topBarState = AutoLockPinState.TopBarState(topBarUiModel),
            pinInsertionState = AutoLockPinState.PinInsertionState(
                descriptionUiModel = descriptionUiModel,
                startingStep = step,
                step = step,
                remainingAttempts = remainingAttempts,
                error = errorsUiMapper.toUiErrorWithRemainingAttemptsAtLoad(this),
                triggerError = Effect.empty()
            ),
            confirmButtonState = AutoLockPinState.ConfirmButtonState(confirmButtonUiModel),
            signOutButtonState = AutoLockPinState.SignOutButtonState(signOutUiModel),
            closeScreenEffect = Effect.Companion.empty(),
            snackbarSuccessEffect = Effect.Companion.empty()
        )
    }
}
