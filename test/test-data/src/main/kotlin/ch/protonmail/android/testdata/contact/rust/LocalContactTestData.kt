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

import ch.protonmail.android.mailcommon.data.mapper.LocalContactGroupId
import ch.protonmail.android.mailcommon.data.mapper.LocalContactId
import ch.protonmail.android.mailcommon.data.mapper.LocalContactItemTypeContact
import ch.protonmail.android.mailcommon.data.mapper.LocalContactItemTypeGroup
import ch.protonmail.android.mailcommon.data.mapper.LocalGroupedContacts
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
                    email = "alice.johnson@example.com",
                    isProton = false,
                    lastUsedTime = 0uL
                ),
                ContactEmailItem(
                    id = Id(102u),
                    email = "alice.work@example.com",
                    isProton = false,
                    lastUsedTime = 0uL
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
                    email = "bob.smith@example.com",
                    isProton = false,
                    lastUsedTime = 0uL
                )
            )
        )
    )

    val contactGroup1 = LocalContactItemTypeGroup(
        ContactGroupItem(
            id = contactGroupId1,
            name = "A Family",
            avatarColor = "#FFD700",
            contacts = listOf(
                ContactItem(
                    id = Id(106u),
                    name = "family",
                    avatarInformation = AvatarInformation("Fam", "#rrr"),
                    emails = listOf(
                        ContactEmailItem(
                            id = Id(1u),
                            "family@example.com",
                            isProton = false,
                            lastUsedTime = 0uL
                        )
                    )
                )
            )
        )
    )

    val contactGroup2 = LocalContactItemTypeGroup(
        ContactGroupItem(
            id = contactGroupId2,
            name = "B Work Colleagues",
            avatarColor = "#8A2BE2",
            contacts = listOf(
                ContactItem(
                    id = Id(106u),
                    name = "family",
                    avatarInformation = AvatarInformation("Fam", "#rrr"),
                    emails = listOf(
                        ContactEmailItem(
                            id = Id(1u),
                            email = "work@example.com",
                            isProton = false,
                            lastUsedTime = 0uL
                        ),
                        ContactEmailItem(
                            id = Id(1u),
                            email = "team@example.com",
                            isProton = false,
                            lastUsedTime = 0uL
                        )
                    )
                )
            )
        )
    )

    val groupedContactsByA = LocalGroupedContacts(
        groupedBy = "A",
        items = listOf(contact1, contactGroup1)
    )

    val groupedContactsByB = LocalGroupedContacts(
        groupedBy = "B",
        items = listOf(contact2, contactGroup2)
    )
}
