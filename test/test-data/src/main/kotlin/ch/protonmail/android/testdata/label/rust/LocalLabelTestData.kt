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

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabel
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelType
import uniffi.proton_mail_uniffi.LabelColor

object LocalLabelTestData {
    val localSystemLabelWithCount = LocalLabel(
        localId = 1.toULong(),
        remoteParentId = null,
        name = "Inbox",
        path = "path",
        color = LabelColor("color"),
        labelType = LocalLabelType.SYSTEM,
        displayOrder = 1.toUInt(),
        initializedMsg = false,
        initializedConv = false,
        localParentId = 3.toULong(),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        totalConv = 2.toULong(),
        totalMsg = 0.toULong(),
        unreadConv = 0.toULong(),
        unreadMsg = 0.toULong()
    )

    val localMessageLabelWithCount = LocalLabel(
        localId = 100.toULong(),
        remoteParentId = null,
        name = "CustomMessageLabel",
        path = "path",
        color = LabelColor("color"),
        labelType = LocalLabelType.LABEL,
        displayOrder = 1.toUInt(),
        initializedMsg = false,
        initializedConv = false,
        localParentId = 3.toULong(),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        totalConv = 2.toULong(),
        totalMsg = 0.toULong(),
        unreadConv = 0.toULong(),
        unreadMsg = 0.toULong()
    )

    val localMessageFolderWithCount = LocalLabel(
        localId = 200.toULong(),
        remoteParentId = null,
        name = "CustomMessageFolder",
        path = "path",
        color = LabelColor("color"),
        labelType = LocalLabelType.FOLDER,
        displayOrder = 1.toUInt(),
        initializedMsg = false,
        initializedConv = false,
        localParentId = 3.toULong(),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        totalConv = 2.toULong(),
        totalMsg = 0.toULong(),
        unreadConv = 0.toULong(),
        unreadMsg = 0.toULong()
    )

    @MissingRustApi
    // Not valid anymore since rust doesn't provide a remote Id in the new API.
    // it will expose an enum in the next version which we can use in a similar way
    fun buildSystem(remoteId: String) = LocalLabel(
        localId = 1000.toULong(),
        remoteParentId = null,
        name = "CustomMessageFolder",
        path = "path",
        color = LabelColor("color"),
        labelType = LocalLabelType.FOLDER,
        displayOrder = 1.toUInt(),
        initializedMsg = false,
        initializedConv = false,
        localParentId = 3.toULong(),
        display = false,
        expanded = false,
        notify = false,
        sticky = false,
        totalConv = 2.toULong(),
        totalMsg = 0.toULong(),
        unreadConv = 0.toULong(),
        unreadMsg = 0.toULong()
    )
}
