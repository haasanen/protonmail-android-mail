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

package ch.protonmail.android.mailpinlock.presentation.pin.preview

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState
import ch.protonmail.android.mailpinlock.presentation.pin.ConfirmButtonUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.DescriptionUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.PinInsertionStep
import ch.protonmail.android.mailpinlock.presentation.pin.SignOutUiModel
import ch.protonmail.android.mailpinlock.presentation.pin.TopBarUiModel

internal object AutoLockPinScreenPreviewData {

    val DataLoaded = AutoLockPinState.DataLoaded(
        topBarState = AutoLockPinState.TopBarState(
            topBarStateUiModel = TopBarUiModel(true, R.string.mail_pinlock_settings_new_pin_topbar)
        ),
        pinInsertionState = AutoLockPinState.PinInsertionState(
            descriptionUiModel = DescriptionUiModel(
                R.string.mail_pinlock_settings_new_pin_title,
                R.string.mail_pinlock_settings_new_pin_description
            ),
            startingStep = PinInsertionStep.PinInsertion,
            step = PinInsertionStep.PinInsertion,
            remainingAttempts = null,
            error = null,
            triggerError = Effect.empty()
        ),
        confirmButtonState = AutoLockPinState.ConfirmButtonState(
            confirmButtonUiModel = ConfirmButtonUiModel(true, R.string.mail_settings_pin_insertion_confirm_button)
        ),
        signOutButtonState = AutoLockPinState.SignOutButtonState(
            signOutUiModel = SignOutUiModel(isDisplayed = true, isRequested = false)
        ),
        closeScreenEffect = Effect.empty(),
        snackbarSuccessEffect = Effect.empty()
    )
}
