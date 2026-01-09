/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.presentation.ui.bottomsheet.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.mailtrackingprotection.presentation.R

@Composable
internal fun TrackerUrlDialog(
    domain: String,
    url: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = { },
        dismissButton = {
            ProtonAlertDialogButton(R.string.action_close_dialog_button) {
                onDismiss()
            }
        },
        title = domain,
        text = { SelectionContainer { ProtonAlertDialogText(url) } }
    )
}
