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

package ch.protonmail.android.testdata.label.rust

import ch.protonmail.android.mailcommon.domain.mapper.LocalLabel
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.mapper.LocalSystemLabel
import uniffi.proton_mail_uniffi.LabelColor
import uniffi.proton_mail_uniffi.LabelDescription

object LocalLabelTestData {
    val localSystemLabelWithCount = LocalLabel(
        id = LocalLabelId(1uL),
        name = "Inbox",
        path = "path",
        color = LabelColor("color"),
        labelDescription = LabelDescription.System(LocalSystemLabel.INBOX),
        displayOrder = 1.toUInt(),
        parentId = LocalLabelId(2uL),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        total = 2.toULong(),
        unread = 0.toULong()
    )

    val localMessageLabelWithCount = LocalLabel(
        id = LocalLabelId(100uL),
        name = "CustomMessageLabel",
        path = "path",
        color = LabelColor("color"),
        labelDescription = LabelDescription.Label,
        displayOrder = 1.toUInt(),
        parentId = LocalLabelId(3uL),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        total = 2.toULong(),
        unread = 0.toULong()
    )

    val localMessageFolderWithCount = LocalLabel(
        id = LocalLabelId(200uL),
        name = "CustomMessageFolder",
        path = "path",
        color = LabelColor("color"),
        labelDescription = LabelDescription.Folder,
        displayOrder = 1.toUInt(),
        parentId = LocalLabelId(3uL),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        total = 2.toULong(),
        unread = 7.toULong()
    )

    fun buildSystem(localSystemLabel: LocalSystemLabel) = LocalLabel(
        id = LocalLabelId(1000.toULong()),
        name = "SomeSystemFolder",
        path = "path",
        color = LabelColor("color"),
        labelDescription = LabelDescription.System(localSystemLabel),
        displayOrder = 1.toUInt(),
        parentId = LocalLabelId(3uL),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        total = 2.toULong(),
        unread = 7.toULong()
    )
}
