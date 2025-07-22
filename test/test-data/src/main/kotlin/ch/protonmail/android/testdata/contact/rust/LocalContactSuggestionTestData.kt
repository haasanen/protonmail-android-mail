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

import ch.protonmail.android.mailcommon.data.mapper.LocalAvatarInformation
import ch.protonmail.android.mailcommon.data.mapper.LocalContactSuggestion
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.testdata.contact.ContactIdTestData
import uniffi.proton_mail_uniffi.ContactEmailItem
import uniffi.proton_mail_uniffi.ContactSuggestionKind
import uniffi.proton_mail_uniffi.Id

object LocalContactSuggestionTestData {

    private val localAvatar = LocalAvatarInformation(
        text = AvatarInformationSample.avatarSample.initials,
        color = AvatarInformationSample.avatarSample.color
    )

    private const val CONTACT_NAME = "contact being suggested"
    val contactSuggestion = LocalContactSuggestion(
        key = ContactIdTestData.contactSuggestionId.id,
        name = CONTACT_NAME,
        avatarInformation = localAvatar,
        kind = ContactSuggestionKind.ContactItem(
            ContactEmailItem(
                id = Id(6uL),
                email = "contact suggestion email",
                isProton = false,
                lastUsedTime = 0uL,
                name = CONTACT_NAME,
                avatarInformation = localAvatar
            )
        )
    )

    val contactGroupSuggestion = LocalContactSuggestion(
        key = ContactIdTestData.contactGroupSuggestionId.id,
        name = "contact group here",
        avatarInformation = localAvatar,
        kind = ContactSuggestionKind.ContactGroup(
            listOf(
                ContactEmailItem(
                    id = Id(ContactIdTestData.contactEmailId1.id.toULong()),
                    email = "contactgroup@first.email",
                    isProton = false,
                    lastUsedTime = 0uL,
                    name = "Contact Group First",
                    avatarInformation = localAvatar
                ),
                ContactEmailItem(
                    id = Id(ContactIdTestData.contactEmailId2.id.toULong()),
                    email = "contactgroup@second.email",
                    isProton = false,
                    lastUsedTime = 0uL,
                    name = "Contact Group Second",
                    avatarInformation = localAvatar
                )
            )
        )
    )
}
