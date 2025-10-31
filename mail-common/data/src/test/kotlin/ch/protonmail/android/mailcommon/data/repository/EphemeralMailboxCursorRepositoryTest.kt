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

package ch.protonmail.android.mailcommon.data.repository

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.CursorId
import ch.protonmail.android.mailcommon.domain.model.EphemeralMailboxCursor
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class EphemeralMailboxCursorRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val sut = EphemeralMailboxCursorRepository(CoroutineScope(mainDispatcherRule.testDispatcher), 500)
    private val testCursorId = CursorId(ConversationId("test Id"))
    private val testCursor = RustConversationCursorImpl(
        testCursorId,
        mockk {
            every { disconnect() } just runs
            every { previousPage() } returns mockk()
            coEvery { nextPage() } returns mockk()
        }
    )

    @Test
    fun `test cursor emits values`() = runTest {
        sut.observeCursor().test {
            Assert.assertEquals(awaitItem(), EphemeralMailboxCursor.NotInitalised)
            sut.setEphemeralCursor(testCursor)
            Assert.assertEquals(awaitItem(), EphemeralMailboxCursor.Data(testCursor))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test cleanup`() = runTest {
        sut.observeCursor().test {
            Assert.assertEquals(awaitItem(), EphemeralMailboxCursor.NotInitalised)
            sut.setEphemeralCursor(testCursor)
            Assert.assertEquals(awaitItem(), EphemeralMailboxCursor.Data(testCursor))

            cancelAndConsumeRemainingEvents()
        }

        advanceTimeBy(600.milliseconds)
        verify(exactly = 1) { testCursor.close() }
    }
}
