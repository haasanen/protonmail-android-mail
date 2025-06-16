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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.previewdata.ContactListPreviewData
import ch.protonmail.android.mailcontact.presentation.utils.ContactFeatureFlags

@Composable
internal fun ContactListGroupItem(
    modifier: Modifier = Modifier,
    contactGroup: ContactListItemUiModel.ContactGroup,
    actions: ContactListScreen.Actions
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                enabled = ContactFeatureFlags.ContactGroupDetails.value,
                onClick = {
                    actions.onContactGroupSelected(contactGroup.id)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .sizeIn(
                        minWidth = MailDimens.AvatarSize,
                        minHeight = MailDimens.AvatarSize
                    )
                    .background(
                        color = contactGroup.color,
                        shape = ProtonTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .padding(MailDimens.AvatarIconPadding)
                        .size(MailDimens.AvatarIconSize),
                    painter = painterResource(id = R.drawable.ic_proton_users),
                    tint = Color.White,
                    contentDescription = NO_CONTENT_DESCRIPTION
                )
            }
        }
        Column(
            modifier = Modifier.padding(
                start = ProtonDimens.ListItemTextStartPadding,
                end = ProtonDimens.Spacing.Large
            )
        ) {
            Text(
                text = contactGroup.name,
                style = ProtonTheme.typography.bodyLargeNorm,
                color = ProtonTheme.colors.textWeak
            )
            Text(
                text = pluralStringResource(
                    R.plurals.contact_group_details_member_count,
                    contactGroup.memberCount,
                    contactGroup.memberCount
                ),
                style = ProtonTheme.typography.bodyMediumNorm,
                color = ProtonTheme.colors.textHint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactListGroupItemPreview() {
    ContactListGroupItem(
        contactGroup = ContactListPreviewData.contactGroupSampleData,
        actions = ContactListScreen.Actions.Empty
    )
}
