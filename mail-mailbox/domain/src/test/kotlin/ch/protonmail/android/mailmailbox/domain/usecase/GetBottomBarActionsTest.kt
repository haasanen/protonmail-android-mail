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

package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.testdata.label.LabelTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.maillabel.domain.model.ViewMode
import kotlin.test.Test
import kotlin.test.assertEquals

class GetBottomBarActionsTest {

    private val getBottomSheetActions = mockk<GetBottomSheetActions>()

    private val observeMailboxActions = GetBottomBarActions(getBottomSheetActions)

    @Test
    fun `returns visible bottom bar actions adding 'More' Action when use case succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelTestData.systemLabel.labelId
        val mailboxItems = listOf(MailboxItemId("1"))
        val viewMode = ViewMode.ConversationGrouping
        val visibleItems = listOf(Action.MarkUnread, Action.Archive, Action.Trash, Action.Move)
        coEvery { getBottomSheetActions(userId, labelId, mailboxItems, viewMode) } returns AllBottomBarActions(
            listOf(Action.MarkRead, Action.Delete),
            visibleItems
        ).right()

        // When
        val actions = observeMailboxActions(userId, labelId, mailboxItems, viewMode)

        // Then
        val expected = visibleItems + Action.More
        assertEquals(expected.right(), actions)
    }

    @Test
    fun `returns error when use case fails getting bottom bar actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelTestData.systemLabel.labelId
        val mailboxItems = listOf(MailboxItemId("1"))
        val viewMode = ViewMode.ConversationGrouping
        val expected = DataError.Local.CryptoError.left()
        coEvery { getBottomSheetActions(userId, labelId, mailboxItems, viewMode) } returns expected

        // When
        val actions = observeMailboxActions(userId, labelId, mailboxItems, viewMode)

        // Then
        assertEquals(expected, actions)
    }
}
