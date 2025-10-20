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

package ch.protonmail.android.mailmailbox.presentation.mailbox.reducer

import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxEvent
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxOperation
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxViewAction
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ShowSpamTrashIncludeFilterState
import javax.inject.Inject

class MailboxShowSpamTrashFilterReducer @Inject constructor() {

    internal fun newStateFrom(
        currentState: ShowSpamTrashIncludeFilterState,
        operation: MailboxOperation.AffectingShowSpamTrashFilter
    ): ShowSpamTrashIncludeFilterState {
        return when (operation) {
            is MailboxEvent.NewLabelSelected -> currentState.toNewStateForLabelSelected()
            MailboxEvent.ShowSpamTrashFilter -> currentState.toNewStateForFilterDisabled()
            MailboxEvent.HideSpamTrashFilter -> currentState.toNewStateForFilterHidden()
            MailboxViewAction.DisableShowSpamTrashFilter -> currentState.toNewStateForFilterDisabled()
            MailboxViewAction.EnableShowSpamTrashFilter -> currentState.toNewStateForFilterEnabled()
        }
    }

    private fun ShowSpamTrashIncludeFilterState.toNewStateForLabelSelected() = when (this) {
        is ShowSpamTrashIncludeFilterState.Loading,
        is ShowSpamTrashIncludeFilterState.Data.Hidden -> this

        is ShowSpamTrashIncludeFilterState.Data.Shown -> ShowSpamTrashIncludeFilterState.Data.Hidden
    }

    private fun ShowSpamTrashIncludeFilterState.toNewStateForFilterEnabled() = when (this) {
        is ShowSpamTrashIncludeFilterState.Loading,
        is ShowSpamTrashIncludeFilterState.Data.Hidden -> this

        is ShowSpamTrashIncludeFilterState.Data.Shown -> this.copy(enabled = true)
    }

    @Suppress("UnusedReceiverParameter")
    private fun ShowSpamTrashIncludeFilterState.toNewStateForFilterDisabled() =
        ShowSpamTrashIncludeFilterState.Data.Shown(enabled = false)

    @Suppress("UnusedReceiverParameter")
    private fun ShowSpamTrashIncludeFilterState.toNewStateForFilterHidden() =
        ShowSpamTrashIncludeFilterState.Data.Hidden
}
