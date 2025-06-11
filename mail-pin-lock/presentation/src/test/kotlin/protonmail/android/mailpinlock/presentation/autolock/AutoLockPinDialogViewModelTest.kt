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

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.autolock.AutoLockPin
import ch.protonmail.android.mailcommon.domain.model.autolock.VerifyAutoLockPinError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.domain.AutoLockRepository
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.DialogType
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import ch.protonmail.android.mailpinlock.presentation.pin.reducer.AutoLockPinDialogReducer
import ch.protonmail.android.mailpinlock.presentation.pin.ui.dialog.AutoLockPinDialogViewModel
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AutoLockPinDialogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val autoLockRepository = mockk<AutoLockRepository>()
    private val dialogReducer = spyk(
        AutoLockPinDialogReducer(AutoLockPinErrorUiMapper())
    )

    private lateinit var viewModel: AutoLockPinDialogViewModel

    @BeforeTest
    fun setup() {
        viewModel = AutoLockPinDialogViewModel(autoLockRepository, dialogReducer)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should do nothing when dialog type is none`() = runTest {
        // When
        viewModel.processPin(DialogType.None)

        // Then
        confirmVerified(autoLockRepository, dialogReducer)
    }

    @Test
    fun `should propagate success on valid verification`() = runTest {
        // Given
        coEvery { autoLockRepository.verifyAutoLockPinCode(autoLockPin) } returns Unit.right()
        viewModel.textFieldState.edit { append(autoLockPin.value) }

        // When + Then
        viewModel.state.test {
            awaitItem()

            viewModel.processPin(DialogType.ChangePin)
            assertEquals(Effect.of(Unit), awaitItem().successEffect)
        }
    }

    @Test
    fun `should propagate an error on unknown attempts number (verify)`() = runTest {
        // Given
        coEvery {
            autoLockRepository.verifyAutoLockPinCode(autoLockPin)
        } returns VerifyAutoLockPinError.IncorrectPin.left()

        coEvery { autoLockRepository.getRemainingAttempts() } returns DataError.Local.Unknown.left()

        val expectedError = TextUiModel.TextRes(R.string.mail_settings_pin_insertion_error_unknown)

        viewModel.textFieldState.edit { append(autoLockPin.value) }

        // When + Then
        viewModel.state.test {
            awaitItem()

            viewModel.processPin(DialogType.ChangePin)
            assertEquals(expectedError, awaitItem().error)
        }
    }

    @Test
    fun `should propagate an error with remaining attempts (verify)`() = runTest {
        // Given
        coEvery {
            autoLockRepository.verifyAutoLockPinCode(autoLockPin)
        } returns VerifyAutoLockPinError.IncorrectPin.left()

        coEvery { autoLockRepository.getRemainingAttempts() } returns 9.right()

        val expectedError = TextUiModel.TextRes(R.string.mail_settings_pin_verification_error_no_match)

        viewModel.textFieldState.edit { append(autoLockPin.value) }

        // When + Then
        viewModel.state.test {
            awaitItem()

            viewModel.processPin(DialogType.ChangePin)
            assertEquals(expectedError, awaitItem().error)
        }
    }

    @Test
    fun `should propagate success on valid removal`() = runTest {
        // Given
        coEvery { autoLockRepository.deleteAutoLockPinCode(autoLockPin) } returns Unit.right()
        viewModel.textFieldState.edit { append(autoLockPin.value) }

        // When + Then
        viewModel.state.test {
            awaitItem()

            viewModel.processPin(DialogType.DisablePin)
            assertEquals(Effect.of(Unit), awaitItem().successEffect)
        }
    }

    @Test
    fun `should propagate success on valid migration`() = runTest {
        // Given
        coEvery { autoLockRepository.deleteAutoLockPinCode(autoLockPin) } returns Unit.right()
        viewModel.textFieldState.edit { append(autoLockPin.value) }

        // When + Then
        viewModel.state.test {
            awaitItem()

            viewModel.processPin(DialogType.MigrateToBiometrics)
            assertEquals(Effect.of(Unit), awaitItem().successEffect)
        }
    }

    @Test
    fun `should propagate an error with remaining attempts (removal)`() = runTest {
        // Given
        coEvery {
            autoLockRepository.deleteAutoLockPinCode(autoLockPin)
        } returns VerifyAutoLockPinError.IncorrectPin.left()

        coEvery { autoLockRepository.getRemainingAttempts() } returns 9.right()

        val expectedError = TextUiModel.TextRes(R.string.mail_settings_pin_verification_error_no_match)

        viewModel.textFieldState.edit { append(autoLockPin.value) }

        // When + Then
        viewModel.state.test {
            awaitItem()

            viewModel.processPin(DialogType.DisablePin)
            assertEquals(expectedError, awaitItem().error)
        }
    }

    @Test
    fun `should propagate an error on unknown attempts number (removal)`() = runTest {
        // Given
        coEvery {
            autoLockRepository.deleteAutoLockPinCode(autoLockPin)
        } returns VerifyAutoLockPinError.IncorrectPin.left()

        coEvery { autoLockRepository.getRemainingAttempts() } returns DataError.Local.Unknown.left()

        val expectedError = TextUiModel.TextRes(R.string.mail_settings_pin_insertion_error_unknown)

        viewModel.textFieldState.edit { append(autoLockPin.value) }

        // When + Then
        viewModel.state.test {
            awaitItem()

            viewModel.processPin(DialogType.DisablePin)
            assertEquals(expectedError, awaitItem().error)
        }
    }

    private companion object {

        val autoLockPin = AutoLockPin("1234")
    }
}
