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

package ch.protonmail.android.mailcontact.presentation.contactgroupdetails

import androidx.compose.ui.graphics.Color
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.AvatarUiModel
import javax.inject.Inject

class ContactGroupDetailsUiModelMapper @Inject constructor(
    private val colorMapper: ColorMapper
) {

    fun toUiModel(contactGroup: ContactMetadata.ContactGroup) = ContactGroupDetailsUiModel(
        color = colorMapper.toColor(contactGroup.color).getOrElse { Color.Unspecified },
        name = contactGroup.name,
        memberCount = contactGroup.members.size,
        members = contactGroup.members.map { it.toUiModel() }
    )

    private fun ContactEmail.toUiModel() = ContactGroupMemberUiModel(
        id = this.id,
        avatarUiModel = this.avatarInformation.toUiModel(),
        name = this.name,
        emailAddress = this.email
    )

    private fun AvatarInformation.toUiModel() = AvatarUiModel.Initials(
        value = this.initials,
        color = colorMapper.toColor(this.color).getOrElse { Color.Unspecified }
    )
}
