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
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class MailboxShowSpamTrashFilterReducerTest(
    private val testName: String,
    private val state: ShowSpamTrashIncludeFilterState,
    private val operation: MailboxOperation.AffectingShowSpamTrashFilter,
    private val expected: ShowSpamTrashIncludeFilterState
) {

    private val reducer = MailboxShowSpamTrashFilterReducer()

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
                ShowSpamTrashIncludeFilterState.Loading,
                MailboxEvent.NewLabelSelected(mockk(), mockk(), null),
                ShowSpamTrashIncludeFilterState.Loading
            ),
            arrayOf(
                "New label selected on hidden",
                ShowSpamTrashIncludeFilterState.Data.Hidden,
                MailboxEvent.NewLabelSelected(mockk(), mockk(), null),
                ShowSpamTrashIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "New label selected on shown",
                ShowSpamTrashIncludeFilterState.Data.Shown(true),
                MailboxEvent.NewLabelSelected(mockk(), mockk(), null),
                ShowSpamTrashIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Show filter on loading",
                ShowSpamTrashIncludeFilterState.Loading,
                MailboxEvent.ShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(enabled = false)
            ),
            arrayOf(
                "Show filter on hidden",
                ShowSpamTrashIncludeFilterState.Data.Hidden,
                MailboxEvent.ShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(enabled = false)
            ),
            arrayOf(
                "Show filter on already shown",
                ShowSpamTrashIncludeFilterState.Data.Shown(true),
                MailboxEvent.ShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(enabled = false)
            ),
            arrayOf(
                "Hide filter on loading",
                ShowSpamTrashIncludeFilterState.Loading,
                MailboxEvent.HideSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Hide filter on already hidden",
                ShowSpamTrashIncludeFilterState.Data.Hidden,
                MailboxEvent.HideSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Hide filter on shown",
                ShowSpamTrashIncludeFilterState.Data.Shown(true),
                MailboxEvent.HideSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Enable filter on loading",
                ShowSpamTrashIncludeFilterState.Loading,
                MailboxViewAction.EnableShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Loading
            ),
            arrayOf(
                "Enable filter on hidden",
                ShowSpamTrashIncludeFilterState.Data.Hidden,
                MailboxViewAction.EnableShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Hidden
            ),
            arrayOf(
                "Enable filter on shown",
                ShowSpamTrashIncludeFilterState.Data.Shown(false),
                MailboxViewAction.EnableShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(true)
            ),
            arrayOf(
                "Enable filter on shown (already enabled)",
                ShowSpamTrashIncludeFilterState.Data.Shown(true),
                MailboxViewAction.EnableShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(true)
            ),
            arrayOf(
                "Disable filter on hidden",
                ShowSpamTrashIncludeFilterState.Data.Hidden,
                MailboxViewAction.DisableShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(false)
            ),
            arrayOf(
                "Disable filter on shown",
                ShowSpamTrashIncludeFilterState.Data.Shown(true),
                MailboxViewAction.DisableShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(false)
            ),
            arrayOf(
                "Disable filter on shown (already disabled)",
                ShowSpamTrashIncludeFilterState.Data.Shown(false),
                MailboxViewAction.DisableShowSpamTrashFilter,
                ShowSpamTrashIncludeFilterState.Data.Shown(false)
            )
        )
    }
}
