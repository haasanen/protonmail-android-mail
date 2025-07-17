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

package protonmail.android.mailpinlock.presentation.pin.mapper

import ch.protonmail.android.mailcommon.domain.model.autolock.VerifyAutoLockPinError
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep
import ch.protonmail.android.mailpinlock.presentation.pin.mapper.AutoLockPinErrorUiMapper
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import protonmail.android.mailpinlock.presentation.autolock.helpers.AutoLockTestData
import kotlin.test.assertEquals

@RunWith(Enclosed::class)
internal class AutoLockPinErrorUiMapperTest {

    @RunWith(Parameterized::class)
    internal class AutoLockPinErrorUiMapperUpdateErrorTest(private val testInput: TestInput) {

        private val autoLockPinSettingsErrorUiMapper = AutoLockPinErrorUiMapper()

        @Test
        fun `should map the update error to the appropriate ui model`() = with(testInput) {
            // When
            val actual = autoLockPinSettingsErrorUiMapper.toUiModel(updateError)

            // Then
            assertEquals(expectedValue, actual)
        }

        private companion object {

            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                TestInput(
                    AutoLockPinEvent.Update.Error.NotMatchingPins,
                    TextUiModel(R.string.mail_settings_pin_insertion_error_no_match)
                ),
                TestInput(
                    AutoLockPinEvent.Update.Error.UnknownError,
                    TextUiModel(R.string.mail_settings_pin_insertion_error_unknown)
                ),
                TestInput(
                    AutoLockPinEvent.Update.Error.Verify(
                        VerifyAutoLockPinError.IncorrectPin,
                        AutoLockTestData.OneRemainingAttempt
                    ),
                    TextUiModel.PluralisedText(
                        R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold,
                        AutoLockTestData.OneRemainingAttempt
                    )
                ),
                TestInput(
                    AutoLockPinEvent.Update.Error.Verify(
                        VerifyAutoLockPinError.IncorrectPin,
                        AutoLockTestData.NineRemainingAttempts
                    ),
                    TextUiModel(R.string.mail_settings_pin_verification_error_no_match)
                )
            )
        }

        data class TestInput(
            val updateError: AutoLockPinEvent.Update.Error,
            val expectedValue: TextUiModel
        )
    }

    @RunWith(Parameterized::class)
    internal class AutoLockPinErrorUiMapperRemainingAttemptsTest(private val testInput: TestInput) {

        private val autoLockPinSettingsErrorUiMapper = AutoLockPinErrorUiMapper()

        @Test
        fun `should map the remaining attempts to the appropriate error ui model`() = with(testInput) {
            // When
            val actual = autoLockPinSettingsErrorUiMapper.toUiModel(
                error = AutoLockPinEvent.Update.Error.Verify(
                    error = VerifyAutoLockPinError.IncorrectPin,
                    remainingAttempts = remainingAttempts
                )
            )

            // Then
            assertEquals(expectedValue, actual)
        }

        companion object {

            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                TestInput(
                    10,
                    TextUiModel(R.string.mail_settings_pin_verification_error_no_match)
                ),
                TestInput(
                    1,
                    TextUiModel.PluralisedText(R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold, 1)
                ),
                TestInput(
                    6,
                    TextUiModel(R.string.mail_settings_pin_verification_error_no_match)
                )
            )
        }

        data class TestInput(
            val remainingAttempts: Int,
            val expectedValue: TextUiModel?
        )
    }

    @RunWith(Parameterized::class)
    internal class AutoLockPinErrorUiMapperLoadedStateRemainingAttemptsTest(private val testInput: TestInput) {

        private val autoLockPinSettingsErrorUiMapper = AutoLockPinErrorUiMapper()

        @Test
        fun `should map the remaining attempts at load state to the appropriate error ui model`() = with(testInput) {
            // When
            val actual = autoLockPinSettingsErrorUiMapper.toUiErrorWithRemainingAttemptsAtLoad(
                state = AutoLockPinEvent.Data.Loaded(
                    step = PinInsertionStep.PinInsertion,
                    remainingAttempts = remainingAttempts
                )
            )

            // Then
            assertEquals(expectedValue, actual)
        }

        companion object {

            @JvmStatic
            @Parameterized.Parameters(name = "{0}")
            fun data() = arrayOf(
                TestInput(
                    10,
                    null
                ),
                TestInput(
                    9,
                    null
                ),
                TestInput(
                    8,
                    null
                ),
                TestInput(
                    7,
                    null
                ),
                TestInput(
                    6,
                    null
                ),
                TestInput(
                    5,
                    null
                ),
                TestInput(
                    4,
                    null
                ),
                TestInput(
                    3,
                    TextUiModel.PluralisedText(R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold, 3)
                ),
                TestInput(
                    2,
                    TextUiModel.PluralisedText(R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold, 2)
                ),
                TestInput(
                    1,
                    TextUiModel.PluralisedText(R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold, 1)
                )
            )
        }

        data class TestInput(
            val remainingAttempts: Int,
            val expectedValue: TextUiModel?
        )
    }
}
