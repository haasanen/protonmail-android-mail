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

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmailbox.domain.repository.UnreadCountersRepository
import ch.protonmail.android.mailmessage.domain.model.UnreadCounter
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.maillabel.domain.model.LabelId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveUnreadCountersTest {

    private val repository = mockk<UnreadCountersRepository>()

    private val observeUnreadCounters = ObserveUnreadCounters(repository)

    @Test
    fun `returns counters from repository`() = runTest {
        // Given
        every { repository.observeUnreadCounters(userId) } returns flowOf(unreadCounters)

        // When
        observeUnreadCounters(userId).test {
            // Then
            val actual = awaitItem()
            assertTrue(unreadCounters.containsAll(actual))
            assertEquals(unreadCounters.count(), actual.count())
            awaitComplete()
        }
    }

    companion object TestData {
        private val userId = UserIdSample.Primary

        val unreadCounters = listOf(
            UnreadCounter(SystemLabelId.Inbox.labelId, 2),
            UnreadCounter(SystemLabelId.Archive.labelId, 7),
            UnreadCounter(SystemLabelId.Drafts.labelId, 0),
            UnreadCounter(SystemLabelId.Sent.labelId, 0),
            UnreadCounter(SystemLabelId.Trash.labelId, 1),
            UnreadCounter(SystemLabelId.AllMail.labelId, 10),
            UnreadCounter(LabelId("custom-label"), 0),
            UnreadCounter(LabelId("custom-folder"), 0)
        )
    }
}
