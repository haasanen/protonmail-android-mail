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

import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.testdata.user.UserIdTestData
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import me.proton.core.domain.entity.UserId

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

    val contacts = listOf(
        contact1,
        contact2
    )

    fun buildContactWith(
        userId: UserId = UserIdTestData.userId,
        contactId: ContactId = ContactIdTestData.contactId1,
        contactEmails: List<ContactEmail>,
        name: String? = null
    ) = ContactMetadata.Contact(
        id = contactId,
        name = name ?: "contact name",
        emails = contactEmails,
        avatar = AvatarInformationSample.avatarSample
    )

    fun buildContactEmailWith(
        userId: UserId = UserIdTestData.userId,
        contactEmailId: ContactEmailId = ContactIdTestData.contactEmailId1,
        contactId: ContactId = ContactIdTestData.contactId1,
        name: String,
        address: String
    ) = ContactEmail(
        userId = userId,
        id = contactEmailId,
        name = name,
        email = address,
        defaults = 0,
        order = 0,
        contactId = contactId,
        canonicalEmail = address,
        labelIds = emptyList(),
        isProton = null,
        lastUsedTime = 0
    )
}

