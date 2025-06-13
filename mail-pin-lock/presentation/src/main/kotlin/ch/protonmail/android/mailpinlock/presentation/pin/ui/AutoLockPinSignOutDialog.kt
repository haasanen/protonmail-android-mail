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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.component.ProtonTextButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.pin.AutoLockPinState

@Composable
fun AutoLockPinSignOutItem(state: AutoLockPinState.SignOutButtonState, actions: AutoLockPinScreen.SignOutActions) {
    if (state.signOutUiModel.isDisplayed) {
        ProtonTextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProtonDimens.Spacing.Standard),
            onClick = {
                actions.onSignOut()
            }
        ) {
            Text(
                text = stringResource(id = R.string.mail_settings_pin_insertion_signout_text),
                style = ProtonTheme.typography.titleMedium,
                color = ProtonTheme.colors.brandNorm,
                textAlign = TextAlign.Center
            )
        }
    }

    if (state.signOutUiModel.isRequested) {
        ProtonAlertDialog(
            onDismissRequest = actions.onSignOutCanceled,
            confirmButton = {
                ProtonAlertDialogButton(R.string.mail_settings_pin_insertion_signout_dialog_confirm) {
                    actions.onSignOutConfirmed()
                }
            },
            dismissButton = {
                ProtonAlertDialogButton(R.string.mail_settings_pin_insertion_signout_dialog_cancel) {
                    actions.onSignOutCanceled()
                }
            },
            title = stringResource(id = R.string.mail_settings_pin_insertion_signout_dialog_title),
            text = { ProtonAlertDialogText(R.string.mail_settings_pin_insertion_signout_dialog_description) }
        )
    }
}
