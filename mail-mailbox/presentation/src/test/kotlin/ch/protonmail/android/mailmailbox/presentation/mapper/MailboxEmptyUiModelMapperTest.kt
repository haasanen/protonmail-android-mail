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

package ch.protonmail.android.mailmailbox.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.CappedNumberUiModel
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.MailboxEmptyUiModelMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxEmptyUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.UnreadFilterState
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MailboxEmptyUiModelMapperTest {

    @Test
    fun `should map to empty unread state`() {
        // Given
        val unreadFilterState = UnreadFilterState.Data(
            unreadCount = CappedNumberUiModel.Zero, isFilterEnabled = true
        )
        val listState = mockk<MailboxListState.Data.ViewMode>()
        val expected = MailboxEmptyUiModel(
            R.drawable.illustration_empty_mailbox_unread,
            R.string.mailbox_is_empty_no_unread_messages_title,
            R.string.mailbox_is_empty_description
        )

        // When
        val actual = MailboxEmptyUiModelMapper.toEmptyMailboxUiModel(
            unreadFilterState = unreadFilterState,
            listState = listState
        )

        // Then
        assertEquals(actual, expected)
    }

    @Test
    fun `should map to empty default custom mailbox state`() {
        // Given
        val unreadFilterState = UnreadFilterState.Data(
            unreadCount = CappedNumberUiModel.Zero, isFilterEnabled = false
        )
        val listState = getListStateForCustomLocation()
        val expected = expectedEmptyDefaultModel

        // When
        val actual = MailboxEmptyUiModelMapper.toEmptyMailboxUiModel(
            unreadFilterState = unreadFilterState,
            listState = listState
        )

        // Then
        assertEquals(actual, expected)
    }

    @Test
    fun `should map to empty default system mailbox state`() {
        // Given
        val unreadFilterState = UnreadFilterState.Data(
            unreadCount = CappedNumberUiModel.Zero, isFilterEnabled = false
        )
        val listState = getListStateForSystemLabel(SystemLabelId.AllMail)
        val expected = expectedEmptyDefaultModel

        // When
        val actual = MailboxEmptyUiModelMapper.toEmptyMailboxUiModel(
            unreadFilterState = unreadFilterState,
            listState = listState
        )

        // Then
        assertEquals(actual, expected)
    }

    @Test
    fun `should map to empty mailbox inbox state`() {
        // Given
        val unreadFilterState = UnreadFilterState.Data(
            unreadCount = CappedNumberUiModel.Zero, isFilterEnabled = false
        )
        val listState = getListStateForSystemLabel(SystemLabelId.Inbox)
        val expected = expectedEmptyInboxModel

        // When
        val actual = MailboxEmptyUiModelMapper.toEmptyMailboxUiModel(
            unreadFilterState = unreadFilterState,
            listState = listState
        )

        // Then
        assertEquals(actual, expected)
    }

    @Test
    fun `should map to empty mailbox trash state`() {
        // Given
        val unreadFilterState = UnreadFilterState.Data(
            unreadCount = CappedNumberUiModel.Zero, isFilterEnabled = false
        )
        val listState = getListStateForSystemLabel(SystemLabelId.Trash)
        val expected = expectedEmptyTrashModel

        // When
        val actual = MailboxEmptyUiModelMapper.toEmptyMailboxUiModel(
            unreadFilterState = unreadFilterState,
            listState = listState
        )

        // Then
        assertEquals(actual, expected)
    }

    @Test
    fun `should map to empty mailbox spam state`() {
        // Given
        val unreadFilterState = UnreadFilterState.Data(
            unreadCount = CappedNumberUiModel.Zero, isFilterEnabled = false
        )
        val listState = getListStateForSystemLabel(SystemLabelId.Spam)
        val expected = expectedEmptySpamModel

        // When
        val actual = MailboxEmptyUiModelMapper.toEmptyMailboxUiModel(
            unreadFilterState = unreadFilterState,
            listState = listState
        )

        // Then
        assertEquals(actual, expected)
    }

    private fun getListStateForSystemLabel(labelId: SystemLabelId): MailboxListState.Data.ViewMode {
        return mockk<MailboxListState.Data.ViewMode> {
            every { this@mockk.currentMailLabel } returns MailLabel.System(
                id = MailLabelId.System(labelId.labelId),
                systemLabelId = labelId,
                order = 0
            )
        }
    }

    private fun getListStateForCustomLocation(): MailboxListState.Data.ViewMode {
        return mockk<MailboxListState.Data.ViewMode> {
            every { this@mockk.currentMailLabel } returns mockk<MailLabel.Custom>()
        }
    }

    private companion object {

        val expectedEmptyTrashModel = MailboxEmptyUiModel(
            R.drawable.illustration_empty_mailbox_trash,
            R.string.trash_is_empty_title,
            R.string.mailbox_is_empty_trash_description
        )

        val expectedEmptySpamModel = MailboxEmptyUiModel(
            R.drawable.illustration_empty_mailbox_spam,
            R.string.mailbox_is_empty_title,
            R.string.mailbox_is_empty_spam_description
        )

        val expectedEmptyInboxModel = MailboxEmptyUiModel(
            R.drawable.illustration_empty_mailbox_no_messages,
            R.string.mailbox_is_empty_title,
            R.string.mailbox_is_empty_description
        )

        val expectedEmptyDefaultModel = MailboxEmptyUiModel(
            R.drawable.illustration_empty_mailbox_no_messages,
            R.string.mailbox_is_empty_title,
            R.string.mailbox_is_empty_folder_description
        )
    }
}
