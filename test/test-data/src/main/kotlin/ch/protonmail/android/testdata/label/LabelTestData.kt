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

package ch.protonmail.android.testdata.label

import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import ch.protonmail.android.testdata.user.UserIdTestData
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.NewLabel

object LabelTestData {

    val systemLabel = Label(
        FAKE_USER_ID,
        LabelId("1"),
        parentId = null,
        "Inbox",
        LabelType.SystemFolder,
        "path",
        "color",
        1,
        false,
        false,
        false
    )

    val messageLabel = Label(
        FAKE_USER_ID,
        LabelId("100"),
        parentId = null,
        "CustomMessageLabel",
        LabelType.MessageLabel,
        "path",
        "color",
        1,
        false,
        false,
        false
    )

    val messageFolder = Label(
        FAKE_USER_ID,
        LabelId("200"),
        parentId = null,
        "CustomMessageFolder",
        LabelType.MessageFolder,
        "path",
        "color",
        1,
        false,
        false,
        false
    )

    fun buildLabel(
        id: String,
        userId: UserId = UserIdTestData.userId,
        type: LabelType = LabelType.MessageLabel,
        name: String = id,
        order: Int = id.hashCode(),
        color: String = "",
        parentId: String? = null,
        isNotified: Boolean? = null,
        isExpanded: Boolean? = null
    ) = Label(
        userId = userId,
        labelId = LabelId(id),
        parentId = parentId?.let { LabelId(it) },
        name = name,
        type = type,
        path = id,
        color = color,
        order = order,
        isNotified = isNotified,
        isExpanded = isExpanded,
        isSticky = null
    )

    fun buildNewLabel(
        name: String,
        parentId: String? = null,
        type: LabelType = LabelType.MessageLabel,
        color: String = "#338AF3",
        isNotified: Boolean? = null,
        isExpanded: Boolean? = null,
        isSticky: Boolean? = null
    ) = NewLabel(
        parentId = parentId?.let { LabelId(it) },
        name = name,
        type = type,
        color = color,
        isNotified = isNotified,
        isExpanded = isExpanded,
        isSticky = isSticky
    )
}
