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

package ch.protonmail.android.mailcomposer.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.usecase.GetInitials
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import javax.inject.Inject

class ContactSuggestionsMapper @Inject constructor(
    private val getInitials: GetInitials
) {

    fun toUiModel(contacts: List<ContactMetadata>): List<ContactSuggestionUiModel> = contacts.map { contact ->
        when (contact) {
            is ContactMetadata.Contact -> ContactSuggestionUiModel.Contact(
                name = contact.name,
                initial = getInitials(contact.name),
                email = contact.emails.firstOrNull()?.email ?: contact.name
            )
            is ContactMetadata.ContactGroup -> ContactSuggestionUiModel.ContactGroup(
                name = contact.name,
                emails = contact.members.map { it.emails.firstOrNull()?.email ?: it.name }
            )
        }
    }

}
