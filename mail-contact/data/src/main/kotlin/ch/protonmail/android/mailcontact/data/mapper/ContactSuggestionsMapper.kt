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

package ch.protonmail.android.mailcontact.data.mapper

import ch.protonmail.android.mailcommon.data.mapper.LocalContactSuggestion
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import uniffi.proton_mail_uniffi.ContactEmailItem
import uniffi.proton_mail_uniffi.ContactSuggestionKind
import javax.inject.Inject

class ContactSuggestionsMapper @Inject constructor() {

    fun toContactSuggestions(localContacts: List<LocalContactSuggestion>): List<ContactMetadata> = localContacts.map {
        localContactSuggestionToContactMetadata(it)
    }

    private fun localContactSuggestionToContactMetadata(contact: LocalContactSuggestion) =
        when (val type = contact.kind) {
            is ContactSuggestionKind.DeviceContact -> {
                ContactMetadata.Contact(
                    id = contact.key.toContactId(),
                    name = contact.name,
                    emails = listOf(type.v1.toContactEmail()),
                    avatar = contact.avatarInformation.toAvatarInformation()
                )
            }

            is ContactSuggestionKind.ContactItem -> {
                ContactMetadata.Contact(
                    id = contact.key.toContactId(),
                    name = contact.name,
                    emails = listOf(type.v1.toContactEmail()),
                    avatar = contact.avatarInformation.toAvatarInformation()
                )
            }

            is ContactSuggestionKind.ContactGroup -> {
                ContactMetadata.ContactGroup(
                    id = contact.key.toContactGroupId(),
                    name = contact.name,
                    color = contact.avatarInformation.color,
                    members = type.v1.map { contactEmailItemToContact(it) }
                )
            }
        }

    // The model exposed by rust doesn't map well the ContactMetadata used here
    // In particular, rust "Contact Group" contains only a collection of emails
    // while android's client ContactGroup contains a list of "members"
    // (full Contact models).
    private fun contactEmailItemToContact(contactEmailItem: ContactEmailItem) = ContactMetadata.Contact(
        contactEmailItem.email.toContactId(),
        avatar = AvatarInformation("", ""),
        name = contactEmailItem.email,
        emails = listOf(
            ContactEmail(
                id = contactEmailItem.email.toContactEmailId(),
                email = contactEmailItem.email,
                isProton = false,
                lastUsedTime = 0L
            )
        )
    )

    private fun String.toContactEmailId() = ContactEmailId(this)

    private fun String.toContactId() = ContactId(this)

    private fun String.toContactGroupId() = ContactGroupId(this)
}


