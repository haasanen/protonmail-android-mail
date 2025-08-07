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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import ch.protonmail.android.design.compose.component.ProtonRawListItem
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeOption
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeUiModel

@Composable
fun SetExpirationTimeBottomSheetContent(
    expirationTime: ExpirationTimeUiModel,
    onDoneClick: (ExpirationTimeUiModel) -> Unit
) {

    val selectedItem = remember { mutableStateOf(expirationTime) }

    Column {
        Row(
            modifier = Modifier
                .padding(ProtonDimens.Spacing.Large)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.composer_expiration_time_bottom_sheet_title),
                style = ProtonTheme.typography.titleMediumNorm
            )
            Text(
                modifier = Modifier.clickable(role = Role.Button) {
                    onDoneClick(selectedItem.value)
                },
                text = stringResource(id = R.string.composer_expiration_time_bottom_sheet_done),
                style = ProtonTheme.typography.bodyLargeNorm,
                color = ProtonTheme.colors.interactionBrandDefaultNorm
            )
        }

        HorizontalDivider()

        Column {

            ExpirationTimeOption.entries.forEach { item ->
                SelectableExpirationTimeItem(
                    item = item,
                    isSelected = selectedItem.value.selectedOption == item,
                    onSelected = { selectedItem.value = it }
                )
            }
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
            .padding(horizontal = ProtonDimens.Spacing.Large)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = item.textResId),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_checkmark),
                contentDescription = NO_CONTENT_DESCRIPTION,
                tint = ProtonTheme.colors.interactionBrandDefaultNorm
            )
        }
    }
}
