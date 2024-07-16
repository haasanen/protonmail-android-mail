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

import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import uniffi.proton_mail_common.LocalLabelId
import uniffi.proton_mail_common.LocalLabelWithCount

fun LabelId.toLocalLabelId(): LocalLabelId = this.id.toULong()
fun LocalLabelId.toLabelId(): LabelId = LabelId(this.toString())
fun uniffi.proton_api_mail.LabelType.toLabelType(): LabelType {
    return when (this) {
        uniffi.proton_api_mail.LabelType.LABEL -> LabelType.MessageLabel
        uniffi.proton_api_mail.LabelType.SYSTEM -> LabelType.SystemFolder
        uniffi.proton_api_mail.LabelType.CONTACT_GROUP -> LabelType.ContactGroup
        uniffi.proton_api_mail.LabelType.FOLDER -> LabelType.MessageFolder
        else -> LabelType.MessageLabel
    }
}

fun LabelType.toRustLabelType(): uniffi.proton_api_mail.LabelType {
    return when (this) {
        LabelType.MessageLabel -> uniffi.proton_api_mail.LabelType.LABEL
        LabelType.SystemFolder -> uniffi.proton_api_mail.LabelType.SYSTEM
        LabelType.ContactGroup -> uniffi.proton_api_mail.LabelType.CONTACT_GROUP
        LabelType.MessageFolder -> uniffi.proton_api_mail.LabelType.FOLDER
    }

}
fun LocalLabelWithCount.toLabel(): Label {
    return Label(
        userId = FAKE_USER_ID,
        labelId = this.id.toLabelId(),
        name = this.name,
        type = this.labelType.toLabelType(),
        path = this.path ?: "",
        color = this.color,
        order = this.order.toInt(),
        isNotified = this.notified,
        isExpanded = this.expanded,
        isSticky = this.sticky,
        parentId = this.parentId?.toLabelId()

    )
}
fun LocalLabelWithCount.toLabelWithSystemLabelId(): LabelWithSystemLabelId {
    return LabelWithSystemLabelId(
        Label(
            userId = FAKE_USER_ID,
            labelId = this.id.toLabelId(),
            name = this.name,
            type = this.labelType.toLabelType(),
            path = this.path ?: "",
            color = this.color,
            order = this.order.toInt(),
            isNotified = this.notified,
            isExpanded = this.expanded,
            isSticky = this.sticky,
            parentId = this.parentId?.toLabelId()
        ),
        SystemLabelId.enumOf(this.rid)
    )
}
