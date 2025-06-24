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

package protonmail.android.mailpinlock.presentation.autolock

import androidx.lifecycle.SavedStateHandle
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.domain.model.autolock.SetAutoLockPinError
import ch.protonmail.android.mailcommon.domain.model.autolock.VerifyAutoLockPinError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPending
import ch.protonmail.android.mailpinlock.domain.AutoLockCheckPendingState
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockInsertionMode
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinViewAction
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinViewModel
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinStepUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockSuccessfulOperationUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.reducer.AutoLockPinReducer
import ch.protonmail.android.mailpinlock.presentation.pin.ui.AutoLockPinScreen
import ch.protonmail.android.mailsession.data.usecase.SignOutAllAccounts
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class AutoLockPinViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val autoLockRepository = mockk<AutoLockRepository>()
    private val autoLockCheckPendingState = spyk<AutoLockCheckPendingState>()
    private val signOutAllAccounts = mockk<SignOutAllAccounts>()
    private val savedStateHandle = mockk<SavedStateHandle>()

    private val reducer = spyk(
        AutoLockPinReducer(
            AutoLockPinStepUiMapper(),
            AutoLockSuccessfulOperationUiMapper(),
            AutoLockPinErrorUiMapper()
        )
    )

    private fun viewModel() = AutoLockPinViewModel(
        autoLockRepository = autoLockRepository,
        autoLockCheckPendingState = autoLockCheckPendingState,
        signOutAllAccounts = signOutAllAccounts,
        reducer = reducer,
        savedStateHandle = savedStateHandle
    )

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should init with the correct insertion mode (create)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)

        // When
        viewModel()

        // Then
        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Data.Loaded(PinInsertionStep.PinInsertion, 10))
        }
    }

    @Test
    fun `should init with the correct insertion mode (verify)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)

        // When
        viewModel()

        // Then
        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Data.Loaded(PinInsertionStep.PinVerification, 10))
        }
    }

    @Test
    fun `should emit abort operation when performing back action (create)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)

        // When
        viewModel().submit(AutoLockPinViewAction.PerformBack)

        // Then
        verify(exactly = 1) {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.OperationAborted)
        }
    }

    @Test
    fun `should emit abort operation when performing back action (confirm)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)

        // When
        viewModel().submit(AutoLockPinViewAction.PerformBack)

        // Then
        verify(exactly = 1) {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.OperationAborted)
        }
    }

    @Test
    fun `should emit nothing when performing back action (verify)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)
        val baseState = getBaseLoadedData(PinInsertionStep.PinVerification)
        every { reducer.newStateFrom(any(), any()) } returns baseState

        // When
        viewModel().submit(AutoLockPinViewAction.PerformBack)

        // Then
        verify(exactly = 0) {
            reducer.newStateFrom(baseState, AutoLockPinEvent.Update.OperationAborted)
            reducer.newStateFrom(baseState, AutoLockPinEvent.Update.OperationCompleted)
        }
    }

    @Test
    fun `should emit error when creating pin with invalid length`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)
        val baseState = getBaseLoadedData(PinInsertionStep.PinInsertion)
        every { reducer.newStateFrom(any(), any()) } returns baseState

        // When
        viewModel().submit(AutoLockPinViewAction.PerformConfirm)

        // Then
        verify {
            reducer.newStateFrom(baseState, AutoLockPinEvent.Update.Error.PinTooShort)
        }
    }

    @Test
    fun `should move to the next step error when creating pin with valid length`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)
        val baseState = getBaseLoadedData(PinInsertionStep.PinInsertion)
        every { reducer.newStateFrom(any(), any()) } returns baseState

        // When
        val viewModel = viewModel()
        viewModel.pinTextFieldState.edit { append("1234") }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then
        verify {
            reducer.newStateFrom(baseState, AutoLockPinEvent.Update.MovedToStep(PinInsertionStep.PinConfirmation))
        }

        // Also check that the TF resets
        assertTrue(viewModel.pinTextFieldState.text.isEmpty())
    }

    @Test
    fun `should error when confirming a different pin`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)

        // When
        val viewModel = viewModel()
        viewModel.pinTextFieldState.edit { append("1234") }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then (first step change)
        assertTrue(viewModel.pinTextFieldState.text.isEmpty())

        // When
        viewModel.pinTextFieldState.edit { append("12344") }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then (second step change)
        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.MovedToStep(PinInsertionStep.PinConfirmation))
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.Error.NotMatchingPins)
        }

        // Also check that the TF resets
        assertTrue(viewModel.pinTextFieldState.text.isNotEmpty())
    }

    @Test
    fun `should save pin when confirming it`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)
        val pin = "1234"
        coEvery { autoLockRepository.setAutoLockPinCode(AutoLockPin(pin)) } returns Unit.right()

        // When
        val viewModel = viewModel()
        viewModel.pinTextFieldState.edit { append(pin) }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then (first step change)
        assertTrue(viewModel.pinTextFieldState.text.isEmpty())

        // When
        viewModel.pinTextFieldState.edit { append(pin) }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then (second step change)
        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.MovedToStep(PinInsertionStep.PinConfirmation))
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.OperationCompleted)
        }
    }

    @Test
    fun `should emit error on pin save when unable to setting it`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.CreatePin)
        val pin = "1234"
        coEvery {
            autoLockRepository.setAutoLockPinCode(AutoLockPin(pin))
        } returns SetAutoLockPinError.PinIsMalformed.left()

        // When
        val viewModel = viewModel()
        viewModel.pinTextFieldState.edit { append(pin) }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then (first step change)
        assertTrue(viewModel.pinTextFieldState.text.isEmpty())

        // When
        viewModel.pinTextFieldState.edit { append(pin) }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then (second step change)
        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.MovedToStep(PinInsertionStep.PinConfirmation))
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.Error.UnknownError)
        }

        assertTrue(viewModel.pinTextFieldState.text.isNotEmpty())
    }

    @Test
    fun `should verify existing pin successfully`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)
        val pin = AutoLockPin("1234")
        coEvery { autoLockRepository.getRemainingAttempts() } returns 10.right()
        coEvery { autoLockRepository.verifyAutoLockPinCode(pin) } returns Unit.right()

        // When
        val viewModel = viewModel()
        viewModel.pinTextFieldState.edit { append(pin.value) }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then
        coVerify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.VerificationCompleted)
            autoLockCheckPendingState.emitCheckPendingState(AutoLockCheckPending(false))
        }
    }

    @Test
    fun `should emit an error on failed pin matching (verify)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)
        val pin = AutoLockPin("1234")
        val expectedError = VerifyAutoLockPinError.IncorrectPin
        val remainingAttempts = 9
        coEvery { autoLockRepository.getRemainingAttempts() } returns remainingAttempts.right()
        coEvery { autoLockRepository.verifyAutoLockPinCode(pin) } returns expectedError.left()

        val expectedEvent = AutoLockPinEvent.Update.Error.Verify(
            error = expectedError,
            remainingAttempts = remainingAttempts
        )

        // When
        val viewModel = viewModel()
        viewModel.pinTextFieldState.edit { append(pin.value) }
        viewModel.submit(AutoLockPinViewAction.PerformConfirm)

        // Then
        verify {
            reducer.newStateFrom(any(), expectedEvent)
        }

        coVerify(exactly = 0) {
            autoLockCheckPendingState.emitCheckPendingState(AutoLockCheckPending(true))
        }
    }

    @Test
    fun `should emit sign out request on verification`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)

        // When
        val viewModel = viewModel()
        viewModel.submit(AutoLockPinViewAction.RequestSignOut)

        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.SignOutRequested)
        }
    }

    @Test
    fun `should emit sign out request and cancellation on verification`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)

        // When
        val viewModel = viewModel()
        viewModel.submit(AutoLockPinViewAction.RequestSignOut)
        viewModel.submit(AutoLockPinViewAction.CancelSignOut)

        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.SignOutRequested)
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.SignOutCanceled)
        }
    }

    @Test
    fun `should emit sign out request and confirmation on verification (success)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)
        coEvery { signOutAllAccounts() } returns Unit.right()

        // When
        val viewModel = viewModel()
        viewModel.submit(AutoLockPinViewAction.RequestSignOut)
        viewModel.submit(AutoLockPinViewAction.ConfirmSignOut)

        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.SignOutRequested)
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.SignOutConfirmed)
        }
    }

    @Test
    fun `should emit sign out request and confirmation on verification (failure)`() = runTest {
        // Given
        expectValidOpeningMode(AutoLockInsertionMode.VerifyPin)
        coEvery { signOutAllAccounts() } returns DataError.Local.Unknown.left()

        // When
        val viewModel = viewModel()
        viewModel.submit(AutoLockPinViewAction.RequestSignOut)
        viewModel.submit(AutoLockPinViewAction.ConfirmSignOut)

        verify {
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.SignOutRequested)
            reducer.newStateFrom(any(), AutoLockPinEvent.Update.Error.UnknownError)
        }
    }

    private fun expectValidOpeningMode(mode: AutoLockInsertionMode) {
        every {
            savedStateHandle.get<String>(AutoLockPinScreen.AutoLockPinModeKey)
        } returns Json.encodeToString<AutoLockInsertionMode>(mode)

        coEvery { autoLockRepository.getRemainingAttempts() } returns 10.right()
    }

    private companion object {

        fun getBaseLoadedData(mode: PinInsertionStep) = AutoLockPinState.DataLoaded(
            mockk(),
            AutoLockPinState.PinInsertionState(
                mockk(),
                startingStep = mode,
                step = mode,
                remainingAttempts = null,
                error = null,
                triggerError = Effect.empty()
            ),
            mockk(),
            mockk(),
            Effect.empty(),
            Effect.empty()
        )
    }
}
