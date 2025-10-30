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
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.maildetail.presentation.model.BlockSenderDialogState
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel

@Composable
fun BlockSenderDialog(
    state: BlockSenderDialogState,
    onDismiss: () -> Unit,
    onConfirm: (MessageIdUiModel?, String) -> Unit
) {
    if (state is BlockSenderDialogState.Shown) {
        when (state) {
            is BlockSenderDialogState.Shown.ShowConfirmation -> BlockSenderDialog(
                email = state.email,
                contactId = state.contactId,
                onDismiss = onDismiss,
                onConfirm = { onConfirm(state.messageId, state.email) }
            )
        }
    }
}

@Composable
private fun BlockSenderDialog(
    email: String,
    contactId: ContactId?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ProtonAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ProtonAlertDialogButton(
                titleResId = R.string.contact_actions_block_contact_dialog_button_block,
                textColor = ProtonTheme.colors.notificationError
            ) { onConfirm() }
        },
        dismissButton = {
            ProtonAlertDialogButton(R.string.contact_actions_block_contact_dialog_button_cancel) { onDismiss() }
        },
        title = stringResource(
            id = contactId?.let { R.string.contact_actions_block_contact }
                ?: R.string.contact_actions_block_address
        ),
        text = {
            ProtonAlertDialogText(
                text = stringResource(
                    id = R.string.contact_actions_block_contact_dialog_text,
                    email
                )
            )
        }
    )
}
