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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcontact.presentation.contactlist.ContactListState
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
                    ContactListItemCard(
                        contact = contact,
                        isFirstInGroup = isFirst,
                        isLastInGroup = isLast,
                        showDivider = !isLast,
                        isSwipable = true,
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
