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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalAvatarInformation
import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactSuggestion
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

    val contactSuggestion = LocalContactSuggestion(
        key = ContactIdTestData.contactSuggestionId.id,
        name = "contact being suggested",
        avatarInformation = localAvatar,
        kind = ContactSuggestionKind.ContactItem(
            ContactEmailItem(
                id = Id(7uL),
                email = "contact suggestion email",
                isProton = false,
                lastUsedTime = 0uL
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
                    id = Id(8uL),
                    email = "contactgroup@first.email",
                    isProton = false,
                    lastUsedTime = 0uL
                ),
                ContactEmailItem(
                    id = Id(9uL),
                    email = "contactgroup@second.email",
                    isProton = false,
                    lastUsedTime = 0uL
                )
            )
        )
    )
}
