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

package ch.protonmail.android.testdata.contact

import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata

object ContactTestData {

    val contact1 =
        ContactMetadata.Contact(
            id = ContactIdTestData.contactId1,
            name = "first contact",
            emails = emptyList(),
            avatar = AvatarInformationSample.avatarSample
        )
    val contact2 = ContactMetadata.Contact(
        id = ContactIdTestData.contactId2,
        name = "second contact",
        emails = emptyList(),
        avatar = AvatarInformationSample.avatarSample
    )
    val contactSuggestion = ContactMetadata.Contact(
        id = ContactIdTestData.contactSuggestionId,
        name = "contact being suggested",
        emails = listOf(ContactEmailSample.contactSuggestionEmail),
        avatar = AvatarInformationSample.avatarSample
    )

    val contactSuggestion1 = ContactMetadata.Contact(
        id = ContactIdTestData.contactSuggestionId,
        name = "contact being suggested",
        emails = listOf(ContactEmailSample.contactSuggestionEmail),
        avatar = AvatarInformationSample.contactSuggestion
    )


    val contactGroupSuggestionEmail1 = ContactMetadata.Contact(
        id = ContactIdTestData.contactId3,
        avatar = AvatarInformation(
            color = AvatarInformationSample.avatarSample.color,
            initials = AvatarInformationSample.avatarSample.initials
        ),
        name = "Contact Group First",
        emails = listOf(ContactEmailSample.contactGroupSuggestionEmail1)
    )
    val contactGroupSuggestionEmail2 = ContactMetadata.Contact(
        id = ContactIdTestData.contactId4,
        avatar = AvatarInformation(
            color = AvatarInformationSample.avatarSample.color,
            initials = AvatarInformationSample.avatarSample.initials
        ),
        name = "Contact Group Second",
        emails = listOf(ContactEmailSample.contactGroupSuggestionEmail2)
    )

    val contactGroupSuggestion = ContactMetadata.ContactGroup(
        id = ContactIdTestData.contactGroupSuggestionId,
        name = "contact group here",
        color = AvatarInformationSample.avatarSample.color,
        members = listOf(
            ContactEmailSample.contactGroupSuggestionEmail1,
            ContactEmailSample.contactGroupSuggestionEmail2
        )
    )

    val contacts = listOf(
        contact1,
        contact2
    )

    fun buildContactWith(
        contactId: ContactId = ContactIdTestData.contactId1,
        contactEmails: List<ContactEmail>,
        name: String? = null
    ) = ContactMetadata.Contact(
        id = contactId,
        name = name ?: "contact name",
        emails = contactEmails,
        avatar = AvatarInformationSample.avatarSample
    )

    fun buildContactEmailWith(contactId: ContactId = ContactIdTestData.contactId1, address: String) = ContactEmail(
        id = contactId,
        email = address,
        isProton = false,
        lastUsedTime = 0,
        name = "",
        avatarInformation = AvatarInformation("", "")
    )
}

