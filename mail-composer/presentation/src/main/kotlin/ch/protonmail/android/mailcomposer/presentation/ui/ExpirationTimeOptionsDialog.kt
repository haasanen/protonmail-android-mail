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

package ch.protonmail.android.mailcomposer.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.component.ProtonDialogTitle
import ch.protonmail.android.design.compose.component.ProtonRawListItem
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcommon.presentation.ui.isLandscape
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeOption
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeUiModel

@Composable
fun ExpirationTimeOptionsDialog(
    onTimePicked: (ExpirationTimeUiModel) -> Unit,
    onDismiss: () -> Unit,
    selectedItem: ExpirationTimeUiModel
) {
    ProtonAlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {},
        title = { ExpirationTimeTitle() },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                ExpirationTimeOption.entries.forEach { item ->
                    SelectableExpirationTimeItem(
                        item = item,
                        isSelected = selectedItem.selectedOption == item,
                        onSelected = { onTimePicked(it) }
                    )
                }
            }
        }
    )
}

@Composable
private fun ExpirationTimeTitle() {
    Column {
        ProtonDialogTitle(titleResId = R.string.composer_expiration_time_bottom_sheet_title)

        if (!isLandscape()) {
            Spacer(Modifier.size(ProtonDimens.Spacing.Large))

            ProtonAlertDialogText(textResId = R.string.composer_expiration_time_bottom_sheet_description)
        }
    }
}

@Composable
private fun SelectableExpirationTimeItem(
    item: ExpirationTimeOption,
    isSelected: Boolean,
    onSelected: (ExpirationTimeUiModel) -> Unit
) {

    ProtonRawListItem(
        modifier = Modifier
            .selectable(selected = isSelected) { onSelected(ExpirationTimeUiModel(item)) }
            .height(ProtonDimens.ListItemHeight)
            .fillMaxWidth()
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Text(
            modifier = Modifier
                .padding(start = ProtonDimens.Spacing.Large)
                .weight(1f),
            text = stringResource(id = item.textResId),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
