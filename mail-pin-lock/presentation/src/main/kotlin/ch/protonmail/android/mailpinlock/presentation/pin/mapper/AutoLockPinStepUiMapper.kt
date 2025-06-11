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

import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.ConfirmButtonUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.DescriptionUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep
import ch.protonmail.android.mailpinlock.presentation.pin.SignOutUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.TopBarUiModel
import javax.inject.Inject

class AutoLockPinStepUiMapper @Inject constructor() {

    fun toTopBarUiModel(step: PinInsertionStep): TopBarUiModel {
        val stringRes = when (step) {
            PinInsertionStep.PinInsertion -> R.string.mail_pinlock_settings_new_pin_topbar
            PinInsertionStep.PinConfirmation -> R.string.mail_pinlock_settings_confirm_pin_topbar
            PinInsertionStep.PinVerification -> R.string.mail_pinlock_settings_verify_pin_topbar
        }

        return TopBarUiModel(
            step != PinInsertionStep.PinVerification,
            stringRes
        )
    }

    fun toDescriptionUiModel(step: PinInsertionStep): DescriptionUiModel {
        val (title, description) = when (step) {
            PinInsertionStep.PinInsertion -> Pair(
                R.string.mail_pinlock_settings_new_pin_title,
                R.string.mail_pinlock_settings_new_pin_description
            )

            PinInsertionStep.PinConfirmation -> Pair(
                R.string.mail_pinlock_settings_confirm_pin_title,
                R.string.mail_pinlock_settings_confirm_pin_description
            )

            PinInsertionStep.PinVerification -> Pair(
                R.string.mail_pinlock_settings_verify_pin_title,
                R.string.mail_pinlock_settings_verify_pin_description
            )
        }

        return DescriptionUiModel(title, description)
    }

    fun toConfirmButtonUiModel(isEnabled: Boolean, step: PinInsertionStep): ConfirmButtonUiModel {
        val stringRes = when (step) {
            PinInsertionStep.PinInsertion,
            PinInsertionStep.PinVerification -> R.string.mail_settings_pin_insertion_button_confirm

            PinInsertionStep.PinConfirmation -> R.string.mail_settings_pin_insertion_button_create
        }

        return ConfirmButtonUiModel(isEnabled, stringRes)
    }

    fun toSignOutUiModel(step: PinInsertionStep): SignOutUiModel {
        val isEnabled = step == PinInsertionStep.PinVerification
        return SignOutUiModel(
            isDisplayed = isEnabled,
            isRequested = false
        )
    }
}
