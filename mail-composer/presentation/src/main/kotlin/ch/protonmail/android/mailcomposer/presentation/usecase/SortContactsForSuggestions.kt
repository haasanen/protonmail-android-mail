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

import ch.protonmail.android.mailcommon.domain.coroutines.DefaultDispatcher
import ch.protonmail.android.mailcommon.presentation.usecase.GetInitials
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.DeviceContact
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.util.kotlin.takeIfNotBlank
import javax.inject.Inject

@Deprecated("To be dropped once contact suggestions through rust are in place")
class SortContactsForSuggestions @Inject constructor(
    private val getInitials: GetInitials,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    suspend operator fun invoke(
        contacts: List<ContactMetadata>,
        deviceContacts: List<DeviceContact>,
        maxContactAutocompletionCount: Int
    ): List<ContactSuggestionUiModel> = withContext(dispatcher) {
        // Use a temporary map to store unique contacts based on their email address.
        val temporaryEmailContactMap = mutableMapOf<String, ContactSuggestionUiModel.Contact>()

        val fromContacts = contacts
            .filterIsInstance<ContactMetadata.Contact>()
            .asSequence()
            .flatMap { contact ->
                contact.emails.map {
                    contact.copy(
                        emails = listOf(it)
                    )
                }
            }
            .sortedBy {
                val lastUsedTimeDescending = Long.MAX_VALUE - it.emails.first().lastUsedTime

                // LastUsedTime, name, email
                "$lastUsedTimeDescending ${it.name} ${it.emails.first().email}"
            }
            .mapNotNull { contact ->
                val contactEmail = contact.emails.first()
                val email = contactEmail.email
                if (email in temporaryEmailContactMap) return@mapNotNull null

                ContactSuggestionUiModel.Contact(
                    name = contact.name.takeIfNotBlank()
                        ?: contactEmail.email,
                    initial = getInitials(contact.name).takeIfNotBlank() ?: "?",
                    email = contactEmail.email
                ).also { temporaryEmailContactMap[email] = it }
            }

        val fromContactGroups = contacts
            .filterIsInstance<ContactMetadata.ContactGroup>()
            .asSequence()
            .map { contactGroup ->
                ContactSuggestionUiModel.ContactGroup(
                    name = contactGroup.name,
                    emails = contactGroup.members.flatMap { member ->
                        member.emails.map { it.email }
                    }
                )
            }
            .toList()

        val fromDeviceContacts = deviceContacts.asSequence().mapNotNull { deviceContact ->
            val email = deviceContact.email
            if (email in temporaryEmailContactMap) return@mapNotNull null

            ContactSuggestionUiModel.Contact(
                name = deviceContact.name,
                initial = getInitials(deviceContact.name).takeIfNotBlank() ?: "?",
                email = email
            ).also { temporaryEmailContactMap[email] = it }
        }
        val fromDeviceAndContactGroups = (fromDeviceContacts + fromContactGroups).sortedBy { it.name }

        return@withContext (fromContacts + fromDeviceAndContactGroups)
            .take(maxContactAutocompletionCount)
            .toList()
    }
}

