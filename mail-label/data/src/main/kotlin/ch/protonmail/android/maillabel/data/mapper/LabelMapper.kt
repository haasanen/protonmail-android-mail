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
import ch.protonmail.android.mailcommon.domain.mapper.LocalSystemLabel
import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import timber.log.Timber
import uniffi.proton_mail_uniffi.LabelDescription

fun LabelId.toLocalLabelId(): LocalLabelId = this.id.toULong()
fun LocalLabelId.toLabelId(): LabelId = LabelId(this.toString())
fun LabelDescription.toLabelType(): LabelType {
    return when (this) {
        is LabelDescription.Label -> LabelType.MessageLabel
        is LabelDescription.System -> LabelType.SystemFolder
        is LabelDescription.ContactGroup -> LabelType.ContactGroup
        is LabelDescription.Folder -> LabelType.MessageFolder
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
        labelId = this.localId.toLabelId(),
        name = this.name,
        type = this.labelDescription.toLabelType(),
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
    val systemLabelDescription = this.labelDescription
    if (systemLabelDescription !is LabelDescription.System) {
        Timber.w("rust-label: Mapping a non-system labelId to a system one. This is illegal.")
        throw IllegalStateException("Mapping a non-system label to system")
    }
    return LabelWithSystemLabelId(
        Label(
            userId = FAKE_USER_ID,
            labelId = this.localId.toLabelId(),
            name = this.name,
            type = systemLabelDescription.toLabelType(),
            path = this.path ?: "",
            color = this.color.value,
            order = this.displayOrder.toInt(),
            isNotified = this.notify,
            isExpanded = this.expanded,
            isSticky = this.sticky,
            parentId = this.localParentId?.toLabelId()
        ),
        systemLabelDescription.v1?.toSystemLabel() ?: SystemLabelId.AllMail
    )
}

fun LocalSystemLabel.toSystemLabel() = when (this) {
    LocalSystemLabel.INBOX -> SystemLabelId.Inbox
    LocalSystemLabel.ALL_DRAFTS -> SystemLabelId.AllDrafts
    LocalSystemLabel.ALL_SENT -> SystemLabelId.AllSent
    LocalSystemLabel.TRASH -> SystemLabelId.Trash
    LocalSystemLabel.SPAM -> SystemLabelId.Spam
    LocalSystemLabel.ALL_MAIL -> SystemLabelId.AllMail
    LocalSystemLabel.ARCHIVE -> SystemLabelId.Archive
    LocalSystemLabel.SENT -> SystemLabelId.Sent
    LocalSystemLabel.DRAFTS -> SystemLabelId.Drafts
    LocalSystemLabel.OUTBOX -> SystemLabelId.Outbox
    LocalSystemLabel.STARRED -> SystemLabelId.Starred
    LocalSystemLabel.SCHEDULED -> SystemLabelId.AllScheduled
    LocalSystemLabel.ALMOST_ALL_MAIL -> SystemLabelId.AlmostAllMail
    LocalSystemLabel.SNOOZED -> SystemLabelId.Snoozed
    LocalSystemLabel.CATEGORY_SOCIAL,
    LocalSystemLabel.CATEGORY_PROMOTIONS,
    LocalSystemLabel.CATERGORY_UPDATES,
    LocalSystemLabel.CATEGORY_FORUMS,
    LocalSystemLabel.CATEGORY_DEFAULT -> {
        Timber.w("rust-label: mapping from unknown system label ID $this. Fallback to all mail")
        SystemLabelId.AllMail
    }
}
