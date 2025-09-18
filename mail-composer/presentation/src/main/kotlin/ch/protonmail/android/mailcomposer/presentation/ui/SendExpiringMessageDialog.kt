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

package ch.protonmail.android.mailcomposer.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailcomposer.presentation.R

@Composable
@Suppress("UseComposableActions")
fun SendExpiringMessageDialog(
    text: TextUiModel,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit,
    onAddPasswordClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProtonAlertDialog(
        modifier = modifier,
        titleResId = R.string.composer_send_expiring_message_to_external_recipients_dialog_title,
        text = {
            Text(
                text = text.string(),
                style = ProtonTheme.typography.bodyMediumWeak
            )
        },
        dismissButton = { },
        confirmButton = {
            Column {
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    titleResId = R.string.composer_send_expiring_message_to_external_recipients_dialog_confirm
                ) { onConfirmClicked() }

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))

                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    titleResId = R.string.composer_send_expiring_message_to_external_recipients_dialog_add_password
                ) { onAddPasswordClicked() }

                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))

                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    titleResId = R.string.composer_send_expiring_message_to_external_recipients_dialog_cancel
                ) { onDismissClicked() }
            }
        },
        onDismissRequest = {}
    )
}
