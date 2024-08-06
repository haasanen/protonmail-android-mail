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

package ch.protonmail.android.maillabel.data.mapper

import ch.protonmail.android.mailcommon.domain.mapper.LocalLabel
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelType
import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType

fun LabelId.toLocalLabelId(): LocalLabelId = this.id.toULong()
fun LocalLabelId.toLabelId(): LabelId = LabelId(this.toString())
fun LocalLabelType.toLabelType(): LabelType {
    return when (this) {
        LocalLabelType.LABEL -> LabelType.MessageLabel
        LocalLabelType.SYSTEM -> LabelType.SystemFolder
        LocalLabelType.CONTACT_GROUP -> LabelType.ContactGroup
        LocalLabelType.FOLDER -> LabelType.MessageFolder
        else -> LabelType.MessageLabel
    }
}

fun LabelType.toRustLabelType(): LocalLabelType {
    return when (this) {
        LabelType.MessageLabel -> LocalLabelType.LABEL
        LabelType.SystemFolder -> LocalLabelType.SYSTEM
        LabelType.ContactGroup -> LocalLabelType.CONTACT_GROUP
        LabelType.MessageFolder -> LocalLabelType.FOLDER
    }

}
fun LocalLabel.toLabel(): Label {
    return Label(
        userId = FAKE_USER_ID,
        labelId = this.localId?.toLabelId() ?: LabelId("0"),
        name = this.name,
        type = this.labelType.toLabelType(),
        path = this.path ?: "",
        color = this.color.value,
        order = this.displayOrder.toInt(),
        isNotified = this.notify,
        isExpanded = this.expanded,
        isSticky = this.sticky,
        parentId = this.localParentId?.toLabelId()

    )
}
fun LocalLabel.toLabelWithSystemLabelId(): LabelWithSystemLabelId {
    return LabelWithSystemLabelId(
        Label(
            userId = FAKE_USER_ID,
            labelId = this.localId?.toLabelId() ?: LabelId("0"),
            name = this.name,
            type = this.labelType.toLabelType(),
            path = this.path ?: "",
            color = this.color.value,
            order = this.displayOrder.toInt(),
            isNotified = this.notify,
            isExpanded = this.expanded,
            isSticky = this.sticky,
            parentId = this.localParentId?.toLabelId()
        ),
        SystemLabelId.enumOf(this.remoteId?.value?.value ?: "0")
    )
}
