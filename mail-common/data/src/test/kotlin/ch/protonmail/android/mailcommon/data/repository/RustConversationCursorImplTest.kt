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

import ch.protonmail.android.mailcommon.data.wrapper.ConversationCursor
import ch.protonmail.android.mailcommon.domain.model.ConversationCursorError
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.Cursor
import ch.protonmail.android.mailcommon.domain.model.CursorId
import ch.protonmail.android.mailcommon.domain.model.DataError.Local.IllegalStateError
import ch.protonmail.android.mailcommon.domain.model.End
import ch.protonmail.android.mailcommon.domain.model.Error
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class RustConversationCursorImplTest {

    private val conversationId = ConversationId("100")

    @Test
    fun `test cursor initial state is set`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        Assert.assertNotNull(sut.current)
    }

    @Test
    fun `test cursor initialises correctly with next and previous pages`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("200"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("50"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()

        Assert.assertNotNull(sut.current)
        Assert.assertNotNull(sut.next)
        Assert.assertNotNull(sut.previous)
    }

    @Test
    fun `test cursor initialises correctly with no previous page`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns End
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("50"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()

        Assert.assertEquals(sut.previous, End)
    }

    @Test
    fun `test when next is end then current is end and goForwards is not called`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns End

        val expectedConversationId = conversationId
        val sut = RustConversationCursorImpl(CursorId(expectedConversationId, null), conversationCursorWrapper)
        sut.init()
        Assert.assertEquals(expectedConversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveForward()
        Assert.assertEquals(End, sut.current)
        coVerify(exactly = 0) { conversationCursorWrapper.goForwards() }
    }

    @Test
    fun `test when next increments moves the cursor forward`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            Cursor(ConversationId("200")) andThen Cursor(ConversationId("300"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        Assert.assertEquals(conversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveForward()

        coVerify { conversationCursorWrapper.goForwards() }
        Assert.assertEquals(ConversationId("200"), (sut.current as? Cursor)?.conversationId)
        Assert.assertEquals(conversationId, (sut.previous as? Cursor)?.conversationId)
        Assert.assertEquals(ConversationId("300"), (sut.next as? Cursor)?.conversationId)
        coVerify(exactly = 2) { conversationCursorWrapper.nextPage() }
    }


    @Test
    fun `test when prev then move the cursor backwards`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("200"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        Assert.assertEquals(conversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveBackward()

        coVerify { conversationCursorWrapper.goBackwards() }
        Assert.assertEquals(ConversationId("50"), (sut.current as? Cursor)?.conversationId)
        Assert.assertEquals(conversationId, (sut.next as? Cursor)?.conversationId)
        Assert.assertEquals(ConversationId("300"), (sut.previous as? Cursor)?.conversationId)
        coVerify(exactly = 2) { conversationCursorWrapper.previousPage() }
    }

    @Test
    fun `test when prev and unrecoverable Error then move the cursor backwards and ignore error`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.nextPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.previousPage() } returns
            Error(ConversationCursorError.Other(IllegalStateError)) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        // previous page is error
        Assert.assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.previous)
        sut.moveBackward()
        // current page is error
        Assert.assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.current)
        sut.moveBackward()
        // next page is error
        Assert.assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.next)

        // prev pages loaded as nornal
        Assert.assertEquals(ConversationId("200"), (sut.current as? Cursor)?.conversationId)
        Assert.assertEquals(ConversationId("400"), (sut.previous as? Cursor)?.conversationId)
    }

    @Test
    fun `test when next and unrecoverable Error then move the cursor backwards and ignore error`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            Error(ConversationCursorError.Other(IllegalStateError)) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        // previous page is error
        Assert.assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.next)
        sut.moveForward()
        // current page is error
        Assert.assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.current)
        sut.moveForward()
        // prev page is error
        Assert.assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.previous)

        // next pages loaded as normnal
        Assert.assertEquals(ConversationId("200"), (sut.current as? Cursor)?.conversationId)
        Assert.assertEquals(ConversationId("400"), (sut.next as? Cursor)?.conversationId)
    }

    @Test
    fun `test when prev and Error is recoverable then set null and reload to recover`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.nextPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.previousPage() } returns
            Error(ConversationCursorError.Offline) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        // previous page is error
        Assert.assertEquals(null, sut.previous)
        Assert.assertEquals(conversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveBackward()
        Assert.assertEquals(conversationId, (sut.next as? Cursor)?.conversationId)
        // page reloaded
        Assert.assertEquals(ConversationId("200"), (sut.current as? Cursor)?.conversationId)
        verify(exactly = 3) { conversationCursorWrapper.previousPage() }
        verify(exactly = 1) { conversationCursorWrapper.goBackwards() }
    }

    @Test
    fun `test when next and Error is recoverable then set null and reload to recover`() = runTest {
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            Error(ConversationCursorError.Offline) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        sut.init()
        // previous page is error
        Assert.assertEquals(null, sut.next)
        Assert.assertEquals(conversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveForward()
        Assert.assertEquals(ConversationId("400"), (sut.next as? Cursor)?.conversationId)
        // page reloaded
        Assert.assertEquals(ConversationId("200"), (sut.current as? Cursor)?.conversationId)
        coVerify(exactly = 3) { conversationCursorWrapper.nextPage() }
        verify(exactly = 1) { conversationCursorWrapper.goForwards() }
    }
}
