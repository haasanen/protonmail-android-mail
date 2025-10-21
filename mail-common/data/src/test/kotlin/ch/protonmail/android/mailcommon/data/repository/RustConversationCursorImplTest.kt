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

import ch.protonmail.android.mailcommon.domain.repository.ConversationCursor
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.Cursor
import ch.protonmail.android.mailcommon.domain.model.CursorId
import ch.protonmail.android.mailcommon.domain.model.End
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class RustConversationCursorImplTest {

    private val conversationId = ConversationId("100")
    private val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)

    @Test
    fun `test cursor initial state is set`() = runTest {
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        Assert.assertNotNull(sut.current)
        Assert.assertTrue(sut.currentIndex() == 1)
    }

    @Test
    fun `test cursor initialises correctly with next and previous pages`() = runTest {
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("200"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("50"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()

        Assert.assertTrue(sut.size() == 3)
        Assert.assertNotNull(sut.current)
        Assert.assertNotNull(sut.next)
        Assert.assertNotNull(sut.previous)

        Assert.assertTrue(sut.currentIndex() == 1)
    }

    @Test
    fun `test cursor initialises correctly with no previous page`() = runTest {
        coEvery { conversationCursorWrapper.previousPage() } returns End
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("50"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()

        Assert.assertEquals(sut.size(), 2)
        Assert.assertEquals(sut.currentIndex(), 0)
    }

    @Test
    fun `test when end then calling next does not increment the current page`() = runTest {
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns End

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        Assert.assertEquals(1, sut.currentIndex())
        sut.moveForward()
        Assert.assertEquals(1, sut.currentIndex())
    }

    @Test
    fun `test when next increments moves the cursor forward`() = runTest {
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("200"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        Assert.assertEquals(conversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveForward()

        coVerify { conversationCursorWrapper.goForwards() }
        Assert.assertEquals(ConversationId("200"), (sut.current as? Cursor)?.conversationId)
    }

    @Test
    fun `test when next then preload next`() = runTest {
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("200"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        sut.moveForward()

        coVerify(exactly = 2) { conversationCursorWrapper.nextPage() }
    }


    @Test
    fun `test when prev then preload prev`() = runTest {
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("200"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        sut.moveBackward()

        coVerify(exactly = 2) { conversationCursorWrapper.previousPage() }
    }

    @Test
    fun `test when prev then move current to prev`() = runTest {
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("200"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        Assert.assertEquals(conversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveBackward()

        coVerify { conversationCursorWrapper.goBackwards() }
        Assert.assertEquals(ConversationId("50"), (sut.current as? Cursor)?.conversationId)
    }
}
