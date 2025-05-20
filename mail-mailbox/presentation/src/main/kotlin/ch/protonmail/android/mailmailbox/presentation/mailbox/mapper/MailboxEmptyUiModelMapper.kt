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

package ch.protonmail.android.mailmailbox.presentation.mailbox.mapper

import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxEmptyUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.UnreadFilterState

internal object MailboxEmptyUiModelMapper {

    fun toEmptyMailboxUiModel(unreadFilterState: UnreadFilterState, listState: MailboxListState): MailboxEmptyUiModel {
        val isUnreadFilterEnabled = (unreadFilterState as? UnreadFilterState.Data)?.isFilterEnabled == true

        if (isUnreadFilterEnabled) {
            return MailboxEmptyUiModel(
                R.drawable.illustration_empty_mailbox_unread,
                R.string.mailbox_is_empty_no_unread_messages_title,
                R.string.mailbox_is_empty_description
            )
        }

        val currentMailLabel = (listState as? MailboxListState.Data)?.currentMailLabel

        if (currentMailLabel is MailLabel.System) {
            return when (currentMailLabel.systemLabelId) {
                SystemLabelId.Inbox -> MailboxEmptyUiModel(
                    R.drawable.illustration_empty_mailbox_no_messages,
                    R.string.mailbox_is_empty_title,
                    R.string.mailbox_is_empty_description
                )

                SystemLabelId.Spam -> MailboxEmptyUiModel(
                    R.drawable.illustration_empty_mailbox_spam,
                    R.string.mailbox_is_empty_title,
                    R.string.mailbox_is_empty_spam_description
                )

                SystemLabelId.Trash -> MailboxEmptyUiModel(
                    R.drawable.illustration_empty_mailbox_trash,
                    R.string.trash_is_empty_title,
                    R.string.mailbox_is_empty_trash_description
                )

                else -> getDefaultFolderAsset()
            }
        }

        return getDefaultFolderAsset()
    }

    private fun getDefaultFolderAsset() = MailboxEmptyUiModel(
        R.drawable.illustration_empty_mailbox_no_messages,
        R.string.mailbox_is_empty_title,
        R.string.mailbox_is_empty_folder_description
    )
}
