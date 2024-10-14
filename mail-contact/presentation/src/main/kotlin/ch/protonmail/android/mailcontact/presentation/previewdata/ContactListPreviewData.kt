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
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.maillabel.domain.model.LabelId
import kotlin.random.Random

object ContactListPreviewData {

    val headerSampleData = ContactListItemUiModel.Header(
        value = "A"
    )

    val contactSampleData = ContactListItemUiModel.Contact(
        id = ContactId("Id"),
        name = "Name",
        emailSubtext = TextUiModel("Email, +2"),
        avatar = AvatarUiModel.ParticipantAvatar("JD", "test@proton.me", null, Color.Blue)
    )

    val contactGroupSampleData = ContactListItemUiModel.ContactGroup(
        labelId = LabelId("Id"),
        name = "Name",
        memberCount = Random.nextInt(1, 100),
        color = Color.Blue
    )
}
