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

package ch.protonmail.android.mailcontact.presentation.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.defaultWeak
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel

@Composable
fun ContactDeleteConfirmationDialog(
    contactToDelete: ContactListItemUiModel.Contact?,
    onDeleteConfirmed: (ContactId) -> Unit,
    onDismissRequest: () -> Unit
) {
    contactToDelete?.let { contact ->
        ProtonAlertDialog(
            modifier = Modifier.testTag(ContactDeleteConfirmationDialogTestTags.DeleteDialog),
            title = stringResource(R.string.contact_delete_dialog_title, contact.name),
            text = {
                Text(
                    text = stringResource(id = R.string.contact_delete_dialog_text),
                    style = ProtonTheme.typography.defaultWeak
                )
            },
            dismissButton = {
                ProtonAlertDialogButton(
                    titleResId = R.string.contact_delete_dialog_cancel_button,
                    modifier = Modifier.testTag(ContactDeleteConfirmationDialogTestTags.DeleteDialogCancelButton)
                ) {
                    onDismissRequest()
                }
            },
            confirmButton = {
                ProtonAlertDialogButton(
                    titleResId = R.string.contact_delete_dialog_confirm_button,
                    modifier = Modifier.testTag(ContactDeleteConfirmationDialogTestTags.DeleteDialogConfirmButton)
                ) {
                    onDismissRequest()
                    onDeleteConfirmed(contact.id)
                }
            },
            onDismissRequest = onDismissRequest
        )
    }
}

object ContactDeleteConfirmationDialogTestTags {

    const val DeleteDialog = "DeleteContactDialog"
    const val DeleteDialogCancelButton = "DeleteContactDialogCancelButton"
    const val DeleteDialogConfirmButton = "DeleteContactDialogConfirmButton"
}
