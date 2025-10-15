/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.mailsettings.presentation.R
import me.proton.core.compose.theme.ProtonDimens

@Composable
internal fun ResetToDefaultConfirmationDialog(
    onCancelClicked: () -> Unit,
    onConfirmClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProtonAlertDialog(
        modifier = modifier,
        titleResId = R.string.mail_settings_custom_toolbar_reset,
        text = {
            Column {
                ProtonAlertDialogText(
                    text = stringResource(id = R.string.mail_settings_custom_toolbar_reset_title)
                )
                Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))
            }
        },
        dismissButton = {
            ProtonAlertDialogButton(R.string.presentation_alert_cancel) {
                onCancelClicked()
            }
        },
        confirmButton = {
            ProtonAlertDialogButton(R.string.mail_settings_custom_toolbar_reset_confirm) {
                onConfirmClicked()
            }
        },
        onDismissRequest = { onCancelClicked() }
    )
}
