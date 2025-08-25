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

import ch.protonmail.android.mailcommon.domain.model.Action
import uniffi.proton_mail_uniffi.MobileAction
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ToolbarActionsMapperTest {

    @Test
    fun `should map mobile actions to actions correctly`() {
        // Given
        val mobileActions = listOf(
            MobileAction.Archive,
            MobileAction.Forward,
            MobileAction.Label,
            MobileAction.Move,
            MobileAction.Print,
            MobileAction.Remind,
            MobileAction.Reply,
            MobileAction.ReportPhishing,
            MobileAction.SaveAttachments,
            MobileAction.SenderEmails,
            MobileAction.Snooze,
            MobileAction.Spam,
            MobileAction.ToggleLight,
            MobileAction.ToggleRead,
            MobileAction.ToggleStar,
            MobileAction.Trash,
            MobileAction.ViewHeaders,
            MobileAction.ViewHtml,
            MobileAction.Other("other-action")
        )

        val expectedActions = listOf(
            Action.Archive,
            Action.Forward,
            Action.Label,
            Action.Move,
            Action.Print,
            Action.Remind,
            Action.Reply,
            Action.ReportPhishing,
            Action.SaveAttachments,
            Action.SenderEmails,
            Action.Snooze,
            Action.Spam,
            Action.ViewInLightMode,
            Action.MarkRead,
            Action.Star,
            Action.Trash,
            Action.ViewHeaders,
            Action.ViewHtml,
            null
        )

        // When
        val mappings = mobileActions.zip(expectedActions)

        // Then
        mappings.forEach { (mobileAction, expectedAction) ->
            val actual = mobileAction.toAction()
            assertEquals(expectedAction, actual)
        }
    }

    @Test
    fun `should map actions to mobile actions correctly`() {
        // Given
        val actions = listOf(
            Action.Reply,
            Action.ReplyAll,
            Action.Forward,
            Action.MarkRead,
            Action.MarkUnread,
            Action.Star,
            Action.Unstar,
            Action.Label,
            Action.Move,
            Action.Trash,
            Action.Delete,
            Action.Archive,
            Action.Spam,
            Action.ViewInLightMode,
            Action.ViewInDarkMode,
            Action.Print,
            Action.ViewHeaders,
            Action.ViewHtml,
            Action.ReportPhishing,
            Action.Remind,
            Action.SenderEmails,
            Action.SaveAttachments,
            Action.Snooze,
            Action.Inbox,
            Action.SavePdf,
            Action.Pin,
            Action.Unpin,
            Action.CustomizeToolbar,
            Action.More
        )

        val expectedMobileActions = listOf(
            MobileAction.Reply, // Action.Reply
            MobileAction.Reply, // Action.ReplyAll
            MobileAction.Forward, // Action.Forward
            MobileAction.ToggleRead, // Action.MarkRead
            MobileAction.ToggleRead, // Action.MarkUnread
            MobileAction.ToggleStar, // Action.Star
            MobileAction.ToggleStar, // Action.Unstar
            MobileAction.Label, // Action.Label
            MobileAction.Move, // Action.Move
            MobileAction.Trash, // Action.Trash
            MobileAction.Trash, // Action.Delete
            MobileAction.Archive,
            MobileAction.Spam,
            MobileAction.ToggleLight, // Action.ViewInLightMode
            MobileAction.ToggleLight, // Action.ViewInDarkMode
            MobileAction.Print,
            MobileAction.ViewHeaders,
            MobileAction.ViewHtml,
            MobileAction.ReportPhishing,
            MobileAction.Remind,
            MobileAction.SenderEmails,
            MobileAction.SaveAttachments,
            MobileAction.Snooze,
            null, // Action.Inbox
            null, // Action.SavePdf
            null, // Action.Pin
            null, // Action.Unpin
            null, // Action.CustomizeToolbar
            null // Action.More
        )

        // When
        val mappings = actions.zip(expectedMobileActions)

        // Then
        mappings.forEach { (action, expectedMobileAction) ->
            val actual = action.toLocalMobileAction()
            assertEquals(expectedMobileAction, actual)
        }
    }
}
