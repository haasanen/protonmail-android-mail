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

import ch.protonmail.android.mailcommon.domain.model.autolock.VerifyAutoLockPinError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.reducer.AutoLockPinDialogReducer
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockDialogState
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockPinDialogEvent
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AutoLockPinDialogReducerTest {

    private val errorsUiMapper = spyk<AutoLockPinErrorUiMapper>()
    private lateinit var reducer: AutoLockPinDialogReducer

    @BeforeTest
    fun setup() {
        reducer = AutoLockPinDialogReducer(errorsUiMapper)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should reduce the success event`() {
        // Given
        val initialState = initialState()
        val expectedState = initialState.copy(successEffect = Effect.of(Unit))

        // When
        val actual = reducer.newStateFrom(initialState, AutoLockPinDialogEvent.Success)

        // Then
        assertEquals(expectedState, actual)
        confirmVerified(errorsUiMapper)
    }

    @Test
    fun `should reduce the error event (null attempts)`() {
        // Given
        val initialState = initialState()
        val expectedError = TextUiModel.TextRes(R.string.mail_settings_pin_insertion_error_unknown)
        val expectedState = initialState.copy(error = expectedError, errorEffect = Effect.of(Unit))

        val error = AutoLockPinDialogEvent.Error(VerifyAutoLockPinError.IncorrectPin, null)

        // When
        val actual = reducer.newStateFrom(initialState, error)

        // Then
        assertEquals(expectedState, actual)
    }

    @Test
    fun `should reduce the error event (non-null attempts)`() {
        // Given
        val initialState = initialState()
        val remainingAttempts = 2
        val expectedError = TextUiModel.PluralisedText(
            R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold,
            remainingAttempts
        )
        val expectedState = initialState.copy(error = expectedError, errorEffect = Effect.of(Unit))

        val error = AutoLockPinDialogEvent.Error(VerifyAutoLockPinError.IncorrectPin, remainingAttempts)

        // When
        val actual = reducer.newStateFrom(initialState, error)

        // Then
        assertEquals(expectedState, actual)
    }

    private companion object {

        fun initialState() = AutoLockDialogState(null, Effect.empty(), Effect.empty())
    }
}
