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

package ch.protonmail.android.mailbugreport.presentation.ui.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailbugreport.presentation.R

@Composable
internal fun ExitConfirmationDialog(
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProtonAlertDialog(
        modifier = modifier,
        titleResId = R.string.report_a_problem_title,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.report_a_problem_leave_dialog_description_1),
                    style = ProtonTheme.typography.bodyMediumWeak
                )
                Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Standard))
                Text(
                    text = stringResource(R.string.report_a_problem_leave_dialog_description_2),
                    style = ProtonTheme.typography.bodyMediumWeak
                )
            }
        },
        dismissButton = {
            ProtonAlertDialogButton(
                title = stringResource(R.string.report_a_problem_leave_dialog_cancel_btn)
            ) { onDismissClicked() }
        },
        confirmButton = {
            ProtonAlertDialogButton(
                title = stringResource(R.string.report_a_problem_leave_dialog_close_btn)
            ) { onConfirmClicked() }
        },
        onDismissRequest = {}
    )
}
