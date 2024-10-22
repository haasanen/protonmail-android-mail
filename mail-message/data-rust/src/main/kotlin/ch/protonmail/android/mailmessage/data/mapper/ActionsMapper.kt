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

package ch.protonmail.android.mailmessage.data.mapper

import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelAsAction
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.maillabel.data.mapper.toSystemLabel
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import timber.log.Timber
import uniffi.proton_mail_uniffi.AllBottomBarMessageActions
import uniffi.proton_mail_uniffi.BottomBarActions
import uniffi.proton_mail_uniffi.GeneralActions
import uniffi.proton_mail_uniffi.IsSelected
import uniffi.proton_mail_uniffi.MessageAction
import uniffi.proton_mail_uniffi.MessageAvailableActions
import uniffi.proton_mail_uniffi.MovableSystemFolder
import uniffi.proton_mail_uniffi.MovableSystemFolderAction
import uniffi.proton_mail_uniffi.MoveAction
import uniffi.proton_mail_uniffi.ReplyAction

fun List<LocalLabelAsAction>.toLabelAsActions(): LabelAsActions {
    val labels = this.map { it.toLabel() }
    val selectedLabelIds = this
        .filter { it.isSelected == IsSelected.SELECTED }
        .map { it.labelId.toLabelId() }
    val partiallySelectedLabelIds = this
        .filter { it.isSelected == IsSelected.PARTIAL }
        .map { it.labelId.toLabelId() }

    return LabelAsActions(labels, selectedLabelIds, partiallySelectedLabelIds)
}

private fun LocalLabelAsAction.toLabel() = Label(
    labelId = this.labelId.toLabelId(),
    parentId = null,
    name = this.name,
    type = LabelType.MessageLabel,
    path = "",
    color = this.color.value,
    order = 0,
    isNotified = null,
    isExpanded = null,
    isSticky = null
)

fun List<MoveAction.SystemFolder>.toMailLabels() = this.map { systemAction ->
    MailLabel.System(
        id = MailLabelId.System(systemAction.v1.localId.toLabelId()),
        systemLabelId = systemAction.v1.name.toSystemLabel(),
        order = 0
    )
}

fun MessageAvailableActions.toAvailableActions(): AvailableActions {
    return AvailableActions(
        this.replyActions.replyActionsToActions(),
        this.messageActions.messageActionsToActions().filterNotNull(),
        this.moveActions.systemFolderActionsToActions().filterNotNull(),
        this.generalActions.generalActionsToActions()
    )
}

fun List<ReplyAction>.replyActionsToActions() = this.map { replyAction ->
    when (replyAction) {
        ReplyAction.REPLY -> Action.Reply
        ReplyAction.REPLY_ALL -> Action.ReplyAll
        ReplyAction.FORWARD -> Action.Forward
    }
}

fun List<MovableSystemFolderAction>.systemFolderActionsToActions() = this.map { moveAction ->
    when (moveAction.name) {
        MovableSystemFolder.TRASH -> Action.Trash
        MovableSystemFolder.SPAM -> Action.Spam
        MovableSystemFolder.ARCHIVE -> Action.Archive
        MovableSystemFolder.INBOX -> {
            Timber.i("rust-message: Found unhandled action while mapping: $moveAction")
            null
        }
    }
}

fun List<GeneralActions>.generalActionsToActions() = this.map { generalAction ->
    when (generalAction) {
        GeneralActions.VIEW_MESSAGE_IN_LIGHT_MODE -> Action.ViewInLightMode
        GeneralActions.VIEW_MESSAGE_IN_DARK_MODE -> Action.ViewInDarkMode
        GeneralActions.SAVE_AS_PDF -> Action.SavePdf
        GeneralActions.PRINT -> Action.Print
        GeneralActions.VIEW_HEADERS -> Action.ViewHeaders
        GeneralActions.VIEW_HTML -> Action.ViewHtml
        GeneralActions.REPORT_PHISHING -> Action.ReportPhishing
    }
}

private fun List<MessageAction>.messageActionsToActions() = this.map { messageAction ->
    when (messageAction) {
        MessageAction.STAR -> Action.Star
        MessageAction.UNSTAR -> Action.Unstar
        MessageAction.LABEL_AS -> Action.Label
        MessageAction.MARK_READ -> Action.MarkRead
        MessageAction.MARK_UNREAD -> Action.MarkUnread
        MessageAction.DELETE -> Action.Delete
        MessageAction.PIN,
        MessageAction.UNPIN -> {
            Timber.i("rust-message: Found unhandled action while mapping: $messageAction")
            null
        }
    }
}

fun AllBottomBarMessageActions.toAllBottomBarActions(): AllBottomBarActions {
    return AllBottomBarActions(
        this.hiddenBottomBarActions.bottombarActionsToActions().filterNotNull(),
        this.visibleBottomBarActions.bottombarActionsToActions().filterNotNull()
    )
}

private fun List<BottomBarActions>.bottombarActionsToActions() = this.map { bottombarAction ->
    when (bottombarAction) {
        BottomBarActions.LabelAs -> Action.Label
        BottomBarActions.MarkRead -> Action.MarkRead
        BottomBarActions.MarkUnread -> Action.MarkUnread
        BottomBarActions.More -> Action.More
        BottomBarActions.MoveTo,
        is BottomBarActions.MoveToSystemFolder -> Action.Move
        BottomBarActions.PermanentDelete -> Action.Delete
        BottomBarActions.Star -> Action.Star
        BottomBarActions.Unstar -> Action.Unstar
        BottomBarActions.NotSpam -> {
            Timber.i("rust-message: Found unhandled action while mapping: $bottombarAction")
            null
        }
    }
}

