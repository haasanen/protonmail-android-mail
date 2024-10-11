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
import ch.protonmail.android.mailcommon.presentation.mapper.AvatarInformationMapper
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import javax.inject.Inject

class ContactListItemUiModelMapper @Inject constructor(
    private val contactEmailListMapper: ContactEmailListMapper,
    private val avatarInformationMapper: AvatarInformationMapper,
    private val colorMapper: ColorMapper
) {

    fun toContactListItemUiModel(groupedContactsList: List<GroupedContacts>): List<ContactListItemUiModel> {
        val contacts = arrayListOf<ContactListItemUiModel>()

        groupedContactsList.forEach { groupedContacts ->

            ContactListItemUiModel.Header(value = groupedContacts.groupedBy)

            groupedContacts.contacts.forEach { contact ->
                when (contact) {
                    is ContactMetadata.Contact -> {
                        ContactListItemUiModel.Contact(
                            id = contact.id,
                            name = contact.name,
                            emailSubtext = contactEmailListMapper.toEmailUiModel(contact.emails),
                            avatar = avatarInformationMapper.toUiModel(
                                contact.avatar,
                                contact.emails.firstOrNull()?.email ?: "",
                                null
                            )
                        )
                    }

                    is ContactMetadata.ContactGroup -> {
                        ContactListItemUiModel.ContactGroup(
                            labelId = contact.labelId,
                            name = contact.name,
                            memberCount = contact.emails.size,
                            color = colorMapper.toColor(contact.color).getOrElse { Color.Black }
                        )
                    }

                }
            }
        }

        return contacts
    }
}
