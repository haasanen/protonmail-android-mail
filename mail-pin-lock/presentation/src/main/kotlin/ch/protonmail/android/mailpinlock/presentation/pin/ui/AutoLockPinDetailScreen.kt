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

package ch.protonmail.android.mailpinlock.presentation.pin.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens.AutoLockPinScreen.SpacerSize
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState

@Composable
fun AutoLockPinInsertionScreen(
    state: AutoLockPinState.DataLoaded,
    actions: AutoLockPinDetailScreen.Actions,
    signOutActions: AutoLockPinDetailScreen.SignOutActions,
    modifier: Modifier = Modifier
) {

    BackHandler(true) {
        actions.onBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentHeight()
    ) {
        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f, fill = false)
                .padding(horizontal = ProtonDimens.Spacing.Large)
                .padding(horizontal = ProtonDimens.Spacing.Huge),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoLockPinLockIcon()
            AutoLockPinDotsGrid(state.pinInsertionState.pinInsertionUiModel.currentPin.digits.size)
            AutoLockPinErrorMessageText(effect = state.pinInsertionErrorEffect)

            Spacer(modifier = Modifier.size(SpacerSize))

            AutoLockPinKeyboardGrid(
                showBiometricPin = state.biometricPinState.shouldDisplayButton,
                actions = AutoLockPinKeyboardGrid.Actions(
                    onBiometricPinClick = actions.onBiometricsClick,
                    onDigitAdded = actions.onDigitAdded,
                    onBackSpaceClick = actions.onBackspaceClick
                )
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))

            AutoLockPinSignOutItem(
                state = state.signOutButtonState,
                actions = signOutActions
            )
        }

        VirtualKeyboardConfirmButton(
            confirmButtonUiModel = state.confirmButtonState.confirmButtonUiModel,
            onClick = actions.onConfirmation
        )
    }
}

object AutoLockPinDetailScreen {
    data class Actions(
        val onBack: () -> Unit,
        val onShowSuccessSnackbar: (snackbarText: String) -> Unit,
        val onDigitAdded: (Int) -> Unit,
        val onBackspaceClick: () -> Unit,
        val onBiometricsClick: () -> Unit,
        val onConfirmation: () -> Unit
    )

    data class SignOutActions(
        val onSignOut: () -> Unit,
        val onSignOutConfirmed: () -> Unit,
        val onSignOutCanceled: () -> Unit
    )
}
