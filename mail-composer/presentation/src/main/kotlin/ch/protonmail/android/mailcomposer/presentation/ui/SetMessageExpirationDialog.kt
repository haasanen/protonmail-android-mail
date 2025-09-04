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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeOption
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeUiModel

@Composable
fun SetMessageExpirationDialog(
    expirationTime: ExpirationTimeUiModel,
    onTimePicked: (ExpirationTimeUiModel) -> Unit,
    onDismiss: () -> Unit
) {

    val selectedItem = remember { mutableStateOf(expirationTime) }
    val showCustomExpirationTimeDialog = remember { mutableStateOf(false) }

    when {
        showCustomExpirationTimeDialog.value -> CustomExpirationDateTimePicker(
            onTimePicked = onTimePicked,
            onDismiss = onDismiss
        )

        else -> ExpirationTimeOptionsDialog(
            onTimePicked = {
                if (it.selectedOption == ExpirationTimeOption.Custom) {
                    showCustomExpirationTimeDialog.value = true
                } else {
                    onTimePicked(it)
                }
            },
            onDismiss = onDismiss,
            selectedItem
        )
    }
}

@Preview
@Composable
fun PreviewSetExpirationTimeDialog() {
    SetMessageExpirationDialog(
        expirationTime = ExpirationTimeUiModel(ExpirationTimeOption.OneHour),
        onTimePicked = {},
        onDismiss = {}
    )
}
