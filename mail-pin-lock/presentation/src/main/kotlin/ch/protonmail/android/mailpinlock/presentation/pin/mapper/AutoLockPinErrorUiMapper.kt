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

package ch.protonmail.android.mailpinlock.presentation.pin.mapper

import ch.protonmail.android.mailcommon.domain.model.autolock.VerifyAutoLockPinError
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinEvent
import javax.inject.Inject

class AutoLockPinErrorUiMapper @Inject constructor() {

    fun toUiModel(error: AutoLockPinEvent.Update.Error): TextUiModel {
        return when (error) {
            is AutoLockPinEvent.Update.Error.NotMatchingPins ->
                TextUiModel(R.string.mail_settings_pin_insertion_error_no_match)

            is AutoLockPinEvent.Update.Error.UnknownError ->
                TextUiModel(R.string.mail_settings_pin_insertion_error_unknown)

            is AutoLockPinEvent.Update.Error.Verify ->
                toUiErrorWithRemainingAttempts(error.error, error.remainingAttempts)

            is AutoLockPinEvent.Update.Error.PinTooShort ->
                TextUiModel(R.string.mail_settings_pin_insertion_error_too_short)
        }
    }

    fun toUiErrorWithRemainingAttempts(error: VerifyAutoLockPinError, remainingAttempts: Int): TextUiModel {
        return when {
            remainingAttempts <= AttemptsThresholdWarningLimit ->
                TextUiModel.PluralisedText(
                    R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold,
                    remainingAttempts
                )

            else -> {
                val textRes = when (error) {
                    VerifyAutoLockPinError.IncorrectPin -> R.string.mail_settings_pin_verification_error_no_match
                    is VerifyAutoLockPinError.Other -> R.string.mail_settings_pin_insertion_error_unknown
                    VerifyAutoLockPinError.TooFrequentAttempts ->
                        R.string.mail_settings_pin_verification_error_too_many_frequent

                    VerifyAutoLockPinError.TooManyAttempts -> R.string.mail_settings_pin_verification_error_too_many
                }

                TextUiModel(textRes)
            }
        }
    }

    fun toUiErrorWithRemainingAttemptsAtLoad(state: AutoLockPinEvent.Data.Loaded): TextUiModel? =
        state.remainingAttempts?.let { remainingAttempts ->
            when {
                remainingAttempts <= AttemptsThresholdWarningLimit ->
                    TextUiModel.PluralisedText(
                        R.plurals.mail_settings_pin_insertion_error_wrong_code_threshold,
                        remainingAttempts
                    )

                else -> null
            }
        }

    private companion object {

        const val AttemptsThresholdWarningLimit = 3
    }
}
