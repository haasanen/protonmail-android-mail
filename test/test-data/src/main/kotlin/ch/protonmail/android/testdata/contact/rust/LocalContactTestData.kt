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

package ch.protonmail.android.testdata.contact.rust

import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactGroupId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactItemTypeContact
import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactItemTypeGroup
import ch.protonmail.android.mailcommon.datarust.mapper.LocalGroupedContacts
import uniffi.proton_mail_uniffi.AvatarInformation
import uniffi.proton_mail_uniffi.ContactEmailItem
import uniffi.proton_mail_uniffi.ContactGroupItem
import uniffi.proton_mail_uniffi.ContactItem
import uniffi.proton_mail_uniffi.Id

object LocalContactTestData {

    val contactId1 = LocalContactId(100u)
    val contactId2 = LocalContactId(103u)
    val contactGroupId1 = LocalContactGroupId(105u)
    val contactGroupId2 = LocalContactGroupId(107u)

    val contact1 = LocalContactItemTypeContact(
        ContactItem(
            id = contactId1,
            name = "Alice Johnson",
            avatarInformation = AvatarInformation(
                text = "A",
                color = "#FF5733"
            ),
            emails = listOf(
                ContactEmailItem(
                    id = Id(101u),
                    email = "alice.johnson@example.com"
                ),
                ContactEmailItem(
                    id = Id(102u),
                    email = "alice.work@example.com"
                )
            )
        )
    )

    val contact2 = LocalContactItemTypeContact(
        ContactItem(
            id = contactId2,
            name = "Bob Smith",
            avatarInformation = AvatarInformation(
                text = "B",
                color = "#33AFFF"
            ),
            emails = listOf(
                ContactEmailItem(
                    id = Id(104u),
                    email = "bob.smith@example.com"
                )
            )
        )
    )

    val contactGroup1 = LocalContactItemTypeGroup(
        ContactGroupItem(
            id = contactGroupId1,
            name = "A Family",
            avatarColor = "#FFD700",
            emails = listOf(
                ContactEmailItem(
                    id = Id(106u),
                    email = "family@example.com"
                )
            )
        )
    )

    val contactGroup2 = LocalContactItemTypeGroup(
        ContactGroupItem(
            id = contactGroupId2,
            name = "B Work Colleagues",
            avatarColor = "#8A2BE2",
            emails = listOf(
                ContactEmailItem(
                    id = Id(108u),
                    email = "work@example.com"
                ),
                ContactEmailItem(
                    id = Id(109u),
                    email = "team@example.com"
                )
            )
        )
    )

    val groupedContactsByA = LocalGroupedContacts(
        groupedBy = "A",
        item = listOf(contact1, contactGroup1)
    )

    val groupedContactsByB = LocalGroupedContacts(
        groupedBy = "B",
        item = listOf(contact2, contactGroup2)
    )
}
