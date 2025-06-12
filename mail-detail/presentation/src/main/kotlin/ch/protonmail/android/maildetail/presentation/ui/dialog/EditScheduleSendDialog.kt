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

package ch.protonmail.android.maildetail.presentation.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.EditScheduledMessageDialogState
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel

@Composable
fun EditScheduleSendDialog(
    state: EditScheduledMessageDialogState,
    onDismiss: () -> Unit,
    onConfirm: (MessageIdUiModel) -> Unit
) {
    if (state is EditScheduledMessageDialogState.Shown) {
        when (state) {
            is EditScheduledMessageDialogState.Shown.ShowConfirmation -> EditScheduleSendDialog(
                onDismiss = onDismiss,
                onConfirm = { onConfirm(state.messageId) }
            )
        }
    }
}

@Composable
private fun EditScheduleSendDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ProtonAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ProtonAlertDialogButton(R.string.message_cancel_schedule_send_dialog_confim_button) { onConfirm() }
        },
        dismissButton = {
            ProtonAlertDialogButton(R.string.message_cancel_schedule_send_dialog_cancel_button) { onDismiss() }
        },
        title = stringResource(id = R.string.message_cancel_schedule_send_dialog_title),
        text = { ProtonAlertDialogText(R.string.message_cancel_schedule_send_dialog_message) }
    )
}
