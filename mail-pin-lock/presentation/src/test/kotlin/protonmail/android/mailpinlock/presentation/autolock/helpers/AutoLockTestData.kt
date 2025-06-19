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

package protonmail.android.mailpinlock.presentation.autolock.helpers

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState
import ch.protonmail.android.mailpinlock.presentation.pin.ConfirmButtonUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.DescriptionUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep.PinInsertion
import ch.protonmail.android.mailpinlock.presentation.pin.SignOutUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.TopBarUiModel

internal object AutoLockTestData {

    val BaseTopBarUiModel = TopBarUiModel(
        true,
        R.string.mail_pinlock_settings_new_pin_topbar
    )
    val BaseTopBarState = AutoLockPinState.TopBarState(BaseTopBarUiModel)

    val BasePinInsertionState = AutoLockPinState.PinInsertionState(
        startingStep = PinInsertion,
        step = PinInsertion,
        descriptionUiModel = DescriptionUiModel(
            R.string.mail_pinlock_settings_new_pin_title,
            R.string.mail_pinlock_settings_new_pin_description
        ),
        remainingAttempts = 10,
        error = null
    )

    val BaseConfirmPinState = AutoLockPinState.PinInsertionState(
        startingStep = PinInsertionStep.PinInsertion,
        step = PinInsertionStep.PinConfirmation,
        descriptionUiModel = DescriptionUiModel(
            R.string.mail_pinlock_settings_confirm_pin_title,
            R.string.mail_pinlock_settings_confirm_pin_description
        ),
        remainingAttempts = null,
        error = null
    )

    val BaseVerificationPinState = AutoLockPinState.PinInsertionState(
        startingStep = PinInsertionStep.PinVerification,
        step = PinInsertionStep.PinVerification,
        descriptionUiModel = DescriptionUiModel(
            R.string.mail_pinlock_settings_verify_pin_title,
            R.string.mail_pinlock_settings_verify_pin_description
        ),
        remainingAttempts = 10,
        error = null
    )

    val BaseConfirmButtonUiModel =
        ConfirmButtonUiModel(
            isEnabled = false,
            R.string.mail_settings_pin_insertion_next_button
        )
    val BaseConfirmButtonState = AutoLockPinState.ConfirmButtonState(
        BaseConfirmButtonUiModel
    )

    val BaseSignOutUiModel =
        SignOutUiModel(isDisplayed = false, isRequested = false)

    val BaseSignOutState = AutoLockPinState.SignOutButtonState(
        BaseSignOutUiModel
    )

    val BaseLoadedState = AutoLockPinState.DataLoaded(
        topBarState = BaseTopBarState,
        pinInsertionState = BasePinInsertionState,
        confirmButtonState = BaseConfirmButtonState,
        signOutButtonState = BaseSignOutState,
        Effect.empty(),
        Effect.empty()
    )

    val SignOutShownUiModel = SignOutUiModel(
        isDisplayed = true,
        isRequested = true
    )

    val SignOutRequestedUiModel = SignOutUiModel(
        isDisplayed = false,
        isRequested = true
    )

    const val OneRemainingAttempt = 1
    const val NineRemainingAttempts = 9
    const val DefaultRemainingAttempts = 10
}
