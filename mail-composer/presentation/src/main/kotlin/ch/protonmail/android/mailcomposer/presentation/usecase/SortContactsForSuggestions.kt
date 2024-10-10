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

package ch.protonmail.android.mailcomposer.presentation.usecase

import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.DeviceContact
import me.proton.core.util.kotlin.takeIfNotBlank
import javax.inject.Inject

class SortContactsForSuggestions @Inject constructor() {

    operator fun invoke(
        contacts: List<ContactMetadata>,
        deviceContacts: List<DeviceContact>,
        maxContactAutocompletionCount: Int
    ): List<ContactSuggestionUiModel> {

        val fromContacts = contacts.asSequence().flatMap { contact ->
            when (contact) {
                is ContactMetadata.Contact -> contact.emails.map {
                    contact.copy(
                        emails = listOf(it)
                    )
                }
                is ContactMetadata.ContactGroup -> contact.emails.map {
                    contact.copy(
                        emails = listOf(it)
                    )
                }
            }

        }.sortedBy {
            val lastUsedTimeDescending = Long.MAX_VALUE - it.emails.first().lastUsedTime

            // LastUsedTime, name, email
            "$lastUsedTimeDescending ${it.name} ${it.emails.first().email ?: ""}"
        }.map { contact ->
            val contactEmail = contact.emails.first()
            ContactSuggestionUiModel.Contact(
                name = contactEmail.name.takeIfNotBlank()
                    ?: contact.name.takeIfNotBlank()
                    ?: contactEmail.email,
                email = contactEmail.email
            )
        }

        val fromDeviceContacts = deviceContacts.asSequence().map {
            ContactSuggestionUiModel.Contact(
                name = it.name,
                email = it.email
            )
        }

        return (fromContacts + fromDeviceContacts)
            .take(maxContactAutocompletionCount)
            .toList()

    }

}
