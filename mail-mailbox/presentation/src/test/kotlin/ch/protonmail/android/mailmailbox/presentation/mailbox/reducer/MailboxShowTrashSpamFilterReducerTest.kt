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
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ShowTrashSpamIncludeFilterState
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class MailboxShowTrashSpamFilterReducerTest(
    private val testName: String,
    private val state: ShowTrashSpamIncludeFilterState,
    private val operation: MailboxOperation.AffectingShowTrashSpamFilter,
    private val expected: ShowTrashSpamIncludeFilterState
) {

    private val reducer = MailboxShowTrashSpamFilterReducer()

    @Test
    fun `should produce the expected new state`() {
        val actual = reducer.newStateFrom(state, operation)
        assertEquals(expected, actual, testName)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "New label selected on loading",
                ShowTrashSpamIncludeFilterState.Loading,
                MailboxEvent.NewLabelSelected(mockk(), null),
                ShowTrashSpamIncludeFilterState.Loading
            ),
            arrayOf(
                "New label selected on hidden",
                ShowTrashSpamIncludeFilterState.Data.Hidden,
                MailboxEvent.NewLabelSelected(mockk(), null),
                ShowTrashSpamIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "New label selected on shown",
                ShowTrashSpamIncludeFilterState.Data.Shown(true),
                MailboxEvent.NewLabelSelected(mockk(), null),
                ShowTrashSpamIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Show filter on loading",
                ShowTrashSpamIncludeFilterState.Loading,
                MailboxEvent.ShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(enabled = false)
            ),
            arrayOf(
                "Show filter on hidden",
                ShowTrashSpamIncludeFilterState.Data.Hidden,
                MailboxEvent.ShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(enabled = false)
            ),
            arrayOf(
                "Show filter on already shown",
                ShowTrashSpamIncludeFilterState.Data.Shown(true),
                MailboxEvent.ShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(enabled = false)
            ),
            arrayOf(
                "Hide filter on loading",
                ShowTrashSpamIncludeFilterState.Loading,
                MailboxEvent.HideTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Hide filter on already hidden",
                ShowTrashSpamIncludeFilterState.Data.Hidden,
                MailboxEvent.HideTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Hide filter on shown",
                ShowTrashSpamIncludeFilterState.Data.Shown(true),
                MailboxEvent.HideTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Enable filter on loading",
                ShowTrashSpamIncludeFilterState.Loading,
                MailboxViewAction.EnableShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Loading
            ),
            arrayOf(
                "Enable filter on hidden",
                ShowTrashSpamIncludeFilterState.Data.Hidden,
                MailboxViewAction.EnableShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Enable filter on shown",
                ShowTrashSpamIncludeFilterState.Data.Shown(false),
                MailboxViewAction.EnableShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(true)
            ),
            arrayOf(
                "Enable filter on shown (already enabled)",
                ShowTrashSpamIncludeFilterState.Data.Shown(true),
                MailboxViewAction.EnableShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(true)
            ),
            arrayOf(
                "Disable filter on hidden",
                ShowTrashSpamIncludeFilterState.Data.Hidden,
                MailboxViewAction.DisableShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(false)
            ),
            arrayOf(
                "Disable filter on shown",
                ShowTrashSpamIncludeFilterState.Data.Shown(true),
                MailboxViewAction.DisableShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(false)
            ),
            arrayOf(
                "Disable filter on shown (already disabled)",
                ShowTrashSpamIncludeFilterState.Data.Shown(false),
                MailboxViewAction.DisableShowTrashSpamFilter,
                ShowTrashSpamIncludeFilterState.Data.Shown(false)
            )
        )
    }
}
