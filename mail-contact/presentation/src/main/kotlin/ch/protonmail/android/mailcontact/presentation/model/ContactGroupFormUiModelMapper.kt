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

package ch.protonmail.android.mailcontact.presentation.model

import androidx.compose.ui.graphics.Color
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import javax.inject.Inject

class ContactGroupFormUiModelMapper @Inject constructor(
    private val colorMapper: ColorMapper
) {

    fun toContactGroupFormUiModel(contactGroup: ContactMetadata.ContactGroup): ContactGroupFormUiModel {
        return ContactGroupFormUiModel(
            id = contactGroup.id,
            name = contactGroup.name,
            color = colorMapper.toColor(contactGroup.color).getOrElse { Color.Black },
            memberCount = contactGroup.members.size,
            members = contactGroup.members.map { contact ->
                ContactGroupFormMember(
                    id = contact.id,
                    initials = contact.avatar.initials,
                    name = contact.name,
                    email = contact.emails.firstOrNull()?.email ?: ""
                )
            }
        )
    }

    fun toContactGroupFormMemberList(contacts: List<ContactMetadata.Contact>): List<ContactGroupFormMember> {
        return contacts.map { contact ->
            ContactGroupFormMember(
                id = contact.id,
                initials = contact.avatar.initials,
                name = contact.name,
                email = contact.emails.firstOrNull()?.email ?: ""
            )
        }
    }
}
