/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.data.mapper

import ch.protonmail.android.mailcommon.data.mapper.LocalMobileAction
import ch.protonmail.android.mailcommon.domain.model.Action
import uniffi.proton_mail_uniffi.MobileAction

fun LocalMobileAction.toAction() = when (this) {
    MobileAction.Archive -> Action.Archive
    MobileAction.Forward -> Action.Forward
    MobileAction.Label -> Action.Label
    MobileAction.Move -> Action.Move
    MobileAction.Print -> Action.Print
    MobileAction.Remind -> Action.Remind
    MobileAction.Reply -> Action.Reply
    MobileAction.ReportPhishing -> Action.ReportPhishing
    MobileAction.SaveAttachments -> Action.SaveAttachments
    MobileAction.SenderEmails -> Action.SenderEmails
    MobileAction.Snooze -> Action.Snooze
    MobileAction.Spam -> Action.Spam
    MobileAction.ToggleLight -> Action.ViewInLightMode
    MobileAction.ToggleRead -> Action.MarkRead
    MobileAction.ToggleStar -> Action.Star
    MobileAction.Trash -> Action.Trash
    MobileAction.ViewHeaders -> Action.ViewHeaders
    MobileAction.ViewHtml -> Action.ViewHtml
    is MobileAction.Other -> null // We don't show unsupported actions
}

fun Action.toLocalMobileAction(): MobileAction? = when (this) {
    Action.Reply,
    Action.ReplyAll -> MobileAction.Reply

    Action.Forward -> MobileAction.Forward
    Action.MarkRead,
    Action.MarkUnread -> MobileAction.ToggleRead

    Action.Star,
    Action.Unstar -> MobileAction.ToggleStar

    Action.Label -> MobileAction.Label
    Action.Move -> MobileAction.Move
    Action.Trash,
    Action.Delete -> MobileAction.Trash

    Action.Archive -> MobileAction.Archive

    Action.Spam -> MobileAction.Spam

    Action.ViewInLightMode,
    Action.ViewInDarkMode -> MobileAction.ToggleLight

    Action.Print -> MobileAction.Print
    Action.ViewHeaders -> MobileAction.ViewHeaders
    Action.ViewHtml -> MobileAction.ViewHtml
    Action.ReportPhishing -> MobileAction.ReportPhishing
    Action.Remind -> MobileAction.Remind
    Action.SenderEmails -> MobileAction.SenderEmails
    Action.SaveAttachments -> MobileAction.SaveAttachments

    Action.Snooze -> MobileAction.Snooze

    Action.Inbox,
    Action.SavePdf,
    Action.Pin,
    Action.Unpin,
    Action.CustomizeToolbar,
    Action.More -> null // Not handled
}
