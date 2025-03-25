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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxState

@Composable
fun MailboxStickyHeader(
    modifier: Modifier = Modifier,
    state: MailboxState,
    actions: MailboxStickyHeader.Actions
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = ProtonDimens.Spacing.Large,
                end = ProtonDimens.Spacing.Large,
                bottom = ProtonDimens.Spacing.Standard,
                top = 0.dp
            ),
        horizontalArrangement = Arrangement.Start
    ) {
        if (state.mailboxListState is MailboxListState.Data.SelectionMode) {
            SelectDeselectAllButton(
                modifier = Modifier.height(MailDimens.UnreadFilterChipHeight),
                areAllItemsSelected = state.mailboxListState.areAllItemsSelected,
                actions = SelectDeselectAllButton.Actions(
                    onSelectAllClicked = actions.onSelectAllClicked,
                    onDeselectAllClicked = actions.onDeselectAllClicked
                )
            )

        } else {
            UnreadItemsFilter(
                modifier = Modifier.height(MailDimens.UnreadFilterChipHeight),
                state = state.unreadFilterState,
                onFilterEnabled = actions.onUnreadFilterEnabled,
                onFilterDisabled = actions.onUnreadFilterDisabled
            )
        }

    }
}

object MailboxStickyHeader {
    data class Actions(
        val onUnreadFilterEnabled: () -> Unit,
        val onUnreadFilterDisabled: () -> Unit,
        val onSelectAllClicked: () -> Unit,
        val onDeselectAllClicked: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onUnreadFilterEnabled = {},
                onUnreadFilterDisabled = {},
                onSelectAllClicked = {},
                onDeselectAllClicked = {}
            )
        }
    }
}
