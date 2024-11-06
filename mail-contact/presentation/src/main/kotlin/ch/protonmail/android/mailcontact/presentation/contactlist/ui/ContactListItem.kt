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

package ch.protonmail.android.mailcontact.presentation.contactlist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.mailcommon.presentation.compose.Avatar
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.utils.ContactFeatureFlags.ContactDetails
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcontact.presentation.previewdata.ContactListPreviewData

@Composable
internal fun ContactListItem(
    modifier: Modifier = Modifier,
    contact: ContactListItemUiModel.Contact,
    actions: ContactListScreen.Actions
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                enabled = ContactDetails.value,
                onClick = {
                    actions.onContactSelected(contact.id)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarUiModel = contact.avatar,
            onClick = { }
        )
        Column(
            modifier = Modifier.padding(
                start = ProtonDimens.ListItemTextStartPadding,
                top = ProtonDimens.ListItemTextStartPadding,
                bottom = ProtonDimens.ListItemTextStartPadding,
                end = ProtonDimens.DefaultSpacing
            )
        ) {
            Text(
                text = contact.name,
                style = ProtonTheme.typography.body1Regular,
                color = ProtonTheme.colors.textWeak
            )
            Text(
                text = contact.emailSubtext.string(),
                style = ProtonTheme.typography.body2Regular,
                color = ProtonTheme.colors.textHint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactListItemPreview() {
    ContactListItem(
        contact = ContactListPreviewData.contactSampleData,
        actions = ContactListScreen.Actions.Empty
    )
}
