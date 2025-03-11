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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListState
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.model.GroupedContactListItemsUiModel
import ch.protonmail.android.mailcontact.presentation.previewdata.ContactListPreviewData.contactGroupSampleData
import ch.protonmail.android.mailcontact.presentation.previewdata.ContactListPreviewData.contactSampleData

@Composable
internal fun ContactListScreenContent(
    modifier: Modifier = Modifier,
    state: ContactListState.Loaded.Data,
    actions: ContactListScreen.Actions
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ProtonDimens.Spacing.Large,
            end = ProtonDimens.Spacing.Large
        )
    ) {
        state.groupedContacts.forEachIndexed { groupIndex, groupedContactsUiModel ->
            val contacts = groupedContactsUiModel.contacts
            contacts.forEachIndexed { index, contact ->
                val isFirst = index == 0
                val isLast = index == contacts.lastIndex

                item {
                    ContactListItem(
                        contact = contact,
                        isFirstInGroup = isFirst,
                        isLastInGroup = isLast,
                        showDivider = !isLast,
                        actions = actions
                    )

                    if (isLast && groupIndex < state.groupedContacts.lastIndex) {
                        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactListItem(
    contact: ContactListItemUiModel,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    showDivider: Boolean,
    actions: ContactListScreen.Actions
) {
    // Define the shape based on position in group
    val shape = when {
        isFirstInGroup && isLastInGroup -> ProtonTheme.shapes.large
        isFirstInGroup -> ProtonTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        )
        isLastInGroup -> ProtonTheme.shapes.large.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp)
        )
        else -> RectangleShape
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundNorm
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(ProtonDimens.Spacing.Standard)) {
                when (contact) {
                    is ContactListItemUiModel.ContactGroup -> ContactListGroupItem(
                        contactGroup = contact,
                        actions = actions
                    )

                    is ContactListItemUiModel.Contact -> SwipeableContactListItem(
                        contact = contact,
                        actions = actions
                    )
                }
            }

            if (showDivider) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = ProtonDimens.BorderSize.Default,
                    color = ProtonTheme.colors.separatorNorm
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun ContactListScreenContentPreview() {
    val sampleGroupedContacts = listOf(
        GroupedContactListItemsUiModel(
            contacts = listOf(contactSampleData, contactGroupSampleData, contactSampleData)
        ),
        GroupedContactListItemsUiModel(
            contacts = listOf(contactSampleData, contactSampleData)
        )
    )

    val sampleState = ContactListState.Loaded.Data(
        groupedContacts = sampleGroupedContacts
    )

    ContactListScreenContent(
        state = sampleState,
        actions = ContactListScreen.Actions.Empty
    )
}
