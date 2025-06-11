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

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep
import ch.protonmail.android.mailpinlock.presentation.pin.SignOutUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinStepUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockSuccessfulOperationUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.reducer.AutoLockPinReducer
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import protonmail.android.mailpinlock.presentation.autolock.helpers.AutoLockTestData
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class AutoLockPinReducerTest(private val testName: String, private val testInput: TestInput) {

    private val reducer = AutoLockPinReducer(
        stepUiMapper = stepMapper,
        successfulOperationUiMapper = operationMapper,
        errorsUiMapper = errorMapper
    )

    @Test
    fun `should map the step to the appropriate top bar ui model`() = with(testInput) {
        // When
        val actual = reducer.newStateFrom(state, event)

        // Then
        assertEquals(expected, actual)
    }

    private companion object {

        val stepMapper = AutoLockPinStepUiMapper()
        val operationMapper = AutoLockSuccessfulOperationUiMapper()
        val errorMapper = AutoLockPinErrorUiMapper()

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(
            arrayOf(
                "from loading to base state",
                TestInput(
                    state = AutoLockPinState.Loading,
                    event = AutoLockPinEvent.Data.Loaded(
                        PinInsertionStep.PinInsertion,
                        AutoLockTestData.DefaultRemainingAttempts
                    ),
                    expected = AutoLockTestData.BaseLoadedState
                )
            ),
            arrayOf(
                "from base state to error (> 3 attempts)",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState,
                    event = AutoLockPinEvent.Update.Error.NotMatchingPins,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            error = TextUiModel(R.string.mail_settings_pin_insertion_error_no_match)
                        )
                    )
                )
            ),
            arrayOf(
                "from base state to error (unknown error)",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState,
                    event = AutoLockPinEvent.Update.Error.UnknownError,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            error = TextUiModel(R.string.mail_settings_pin_insertion_error_unknown)
                        )
                    )
                )
            ),
            arrayOf(
                "from base to created successfully (and close)",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState,
                    event = AutoLockPinEvent.Update.OperationCompleted,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        closeScreenEffect = Effect.of(Unit),
                        snackbarSuccessEffect = Effect.of(
                            TextUiModel.TextRes(R.string.mail_settings_pin_insertion_created_success)
                        )
                    )
                )
            ),
            arrayOf(
                "from base to closed (abort operation)",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState,
                    event = AutoLockPinEvent.Update.OperationAborted,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        closeScreenEffect = Effect.of(Unit)
                    )
                )
            ),
            arrayOf(
                "from base to closed (verify operation)",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState,
                    event = AutoLockPinEvent.Update.VerificationCompleted,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        closeScreenEffect = Effect.of(Unit)
                    )
                )
            ),
            arrayOf(
                "from base (pin insertion) to step move (pin confirmation)",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState,
                    event = AutoLockPinEvent.Update.MovedToStep(PinInsertionStep.PinConfirmation),
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        topBarState = AutoLockTestData.BaseTopBarState.copy(
                            stepMapper.toTopBarUiModel(PinInsertionStep.PinConfirmation)
                        ),
                        pinInsertionState = AutoLockTestData.BaseConfirmPinState,
                        confirmButtonState = AutoLockTestData.BaseConfirmButtonState.copy(
                            stepMapper.toConfirmButtonUiModel(isEnabled = false, PinInsertionStep.PinConfirmation)
                        )
                    )
                )
            ),
            arrayOf(
                "from verification to sign out requested",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            step = PinInsertionStep.PinVerification
                        ),
                        signOutButtonState = AutoLockPinState.SignOutButtonState(AutoLockTestData.SignOutShownUiModel)
                    ),
                    event = AutoLockPinEvent.Update.SignOutRequested,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            step = PinInsertionStep.PinVerification
                        ),
                        signOutButtonState = AutoLockPinState.SignOutButtonState(
                            AutoLockTestData.SignOutShownUiModel
                        )
                    )
                )
            ),
            arrayOf(
                "from sign out requested to cancelled",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            step = PinInsertionStep.PinVerification
                        ),
                        signOutButtonState = AutoLockPinState.SignOutButtonState(
                            signOutUiModel = AutoLockTestData.SignOutRequestedUiModel
                        )
                    ),
                    event = AutoLockPinEvent.Update.SignOutCanceled,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            step = PinInsertionStep.PinVerification
                        ),
                        signOutButtonState = AutoLockPinState.SignOutButtonState(
                            SignOutUiModel(isDisplayed = false, isRequested = false)
                        )
                    )
                )
            ),
            arrayOf(
                "from sign out requested to performed",
                TestInput(
                    state = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            step = PinInsertionStep.PinVerification
                        ),
                        signOutButtonState = AutoLockPinState.SignOutButtonState(
                            AutoLockTestData.SignOutRequestedUiModel
                        )
                    ),
                    event = AutoLockPinEvent.Update.SignOutConfirmed,
                    expected = AutoLockTestData.BaseLoadedState.copy(
                        pinInsertionState = AutoLockTestData.BasePinInsertionState.copy(
                            step = PinInsertionStep.PinVerification
                        ),
                        signOutButtonState = AutoLockPinState.SignOutButtonState(
                            SignOutUiModel(isDisplayed = false, isRequested = true)
                        ),
                        closeScreenEffect = Effect.of(Unit)
                    )
                )
            )
        )
    }

    data class TestInput(
        val state: AutoLockPinState,
        val event: AutoLockPinEvent,
        val expected: AutoLockPinState
    )
}
