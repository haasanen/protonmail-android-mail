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

package ch.protonmail.android.mailcomposer.presentation.ui.suggestions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel

@Composable
fun ContactSuggestionsList(
    currentText: String,
    contactSuggestionItems: List<ContactSuggestionUiModel>,
    modifier: Modifier,
    actions: ContactSuggestionsList.Actions
) {

    BackHandler {
        actions.onContactSuggestionsDismissed()
    }

    LazyColumn(modifier = modifier) {
        items(contactSuggestionItems.size) { index ->
            val item = contactSuggestionItems[index]

            when (item) {
                is ContactSuggestionUiModel.Data.Contact,
                is ContactSuggestionUiModel.Data.ContactGroup -> ContactSuggestionItemElement(
                    currentText = currentText,
                    item = item,
                    onClick = { actions.onContactSuggestionSelected(item) }
                )

                ContactSuggestionUiModel.DeviceContacts -> DeviceContactsEntry(
                    onClick = actions.onRequestContactsPermission,
                    onDenyClick = actions.onDeniedContactsPermission
                )
            }

            HorizontalDivider(color = ProtonTheme.colors.backgroundInvertedBorder)
        }
    }
}

object ContactSuggestionsList {

    data class Actions(
        val onContactSuggestionsDismissed: () -> Unit,
        val onContactSuggestionSelected: (contact: ContactSuggestionUiModel.Data) -> Unit,
        val onRequestContactsPermission: () -> Unit,
        val onDeniedContactsPermission: () -> Unit
    ) {

        companion object {

            val Empty = Actions(

                onContactSuggestionsDismissed = {},
                onContactSuggestionSelected = {},
                onRequestContactsPermission = {},
                onDeniedContactsPermission = {}
            )
        }
    }
}
