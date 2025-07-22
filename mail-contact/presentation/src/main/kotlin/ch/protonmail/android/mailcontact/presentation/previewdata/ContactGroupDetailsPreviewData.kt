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

package ch.protonmail.android.mailcontact.presentation.previewdata

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.AvatarUiModel
import ch.protonmail.android.mailcontact.presentation.contactgroupdetails.ContactGroupDetailsState
import ch.protonmail.android.mailcontact.presentation.contactgroupdetails.ContactGroupDetailsUiModel
import ch.protonmail.android.mailcontact.presentation.contactgroupdetails.ContactGroupMemberUiModel

object ContactGroupDetailsPreviewData {

    val contactGroupDetailsState = ContactGroupDetailsState.Data(
        uiModel = ContactGroupDetailsUiModel(
            color = Color.Blue,
            name = "Proton Mail",
            memberCount = 3,
            members = listOf(
                ContactGroupMemberUiModel(
                    id = ContactId("1"),
                    avatarUiModel = AvatarUiModel.Initials(
                        value = "P",
                        color = Color.Magenta
                    ),
                    name = "Proton 1",
                    emailAddress = "proton1@protonmail.com"
                ),
                ContactGroupMemberUiModel(
                    id = ContactId("2"),
                    avatarUiModel = AvatarUiModel.Initials(
                        value = "P",
                        color = Color.Yellow
                    ),
                    name = "Proton 2",
                    emailAddress = "proton2@protonmail.com"
                ),
                ContactGroupMemberUiModel(
                    id = ContactId("3"),
                    avatarUiModel = AvatarUiModel.Initials(
                        value = "P",
                        color = Color.Green
                    ),
                    name = "Proton 3",
                    emailAddress = "proton3@protonmail.com"
                )
            )
        )
    )
}
