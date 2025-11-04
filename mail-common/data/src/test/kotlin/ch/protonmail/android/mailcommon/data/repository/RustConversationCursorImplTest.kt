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
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RustConversationCursorImplTest {

    private val conversationId = ConversationId("100")

    @Test
    fun `given cursor initialises then initialises correctly with next and previous pages`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("200"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("50"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        Assert.assertNotNull(sut.current)
        Assert.assertNotNull(sut.next)
        Assert.assertNotNull(sut.previous)
    }

    @Test
    fun `given cursor initialises with no previous page then initialises correctly`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns End
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("50"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        assertEquals(sut.previous, End)
    }

    @Test
    fun `given when next is end then current is end and goForwards is not called`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns End

        val expectedConversationId = conversationId
        // when
        val sut = RustConversationCursorImpl(CursorId(expectedConversationId, null), conversationCursorWrapper)
        assertIs<Cursor>(sut.current)
        assertEquals(expectedConversationId, (sut.current as Cursor).conversationId)

        sut.moveForward()

        // then
        assertEquals(End, sut.current)
        coVerify(exactly = 0) { conversationCursorWrapper.goForwards() }
    }

    @Test
    fun `when next increments then moves the cursor forward`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            Cursor(ConversationId("200")) andThen Cursor(ConversationId("300"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        assertEquals(conversationId, (sut.current as? Cursor)?.conversationId)
        sut.moveForward()

        // then
        coVerify { conversationCursorWrapper.goForwards() }
        assertIs<Cursor>(sut.current)
        assertIs<Cursor>(sut.previous)
        assertIs<Cursor>(sut.next)
        assertEquals(ConversationId("200"), (sut.current as Cursor).conversationId)
        assertEquals(conversationId, (sut.previous as Cursor).conversationId)
        assertEquals(ConversationId("300"), (sut.next as Cursor).conversationId)
        coVerify(exactly = 2) { conversationCursorWrapper.nextPage() }
    }


    @Test
    fun `when prev then move the cursor backwards`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns Cursor(ConversationId("200"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        assertIs<Cursor>(sut.current)
        assertEquals(conversationId, (sut.current as Cursor).conversationId)
        sut.moveBackward()

        // then
        coVerify { conversationCursorWrapper.goBackwards() }
        assertIs<Cursor>(sut.current)
        assertIs<Cursor>(sut.next)
        assertIs<Cursor>(sut.previous)
        assertEquals(ConversationId("50"), (sut.current as Cursor).conversationId)
        assertEquals(conversationId, (sut.next as Cursor).conversationId)
        assertEquals(ConversationId("300"), (sut.previous as Cursor).conversationId)
        coVerify(exactly = 2) { conversationCursorWrapper.previousPage() }
    }

    @Test
    fun `when prev and unrecoverable Error then move the cursor backwards and ignore error`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.nextPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.previousPage() } returns
            Error(ConversationCursorError.Other(IllegalStateError)) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        // previous page is error
        assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.previous)
        sut.moveBackward()
        // current page is error
        assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.current)
        sut.moveBackward()
        // next page is error
        assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.next)

        // prev pages loaded as normal
        assertIs<Cursor>(sut.current)
        assertIs<Cursor>(sut.previous)
        assertEquals(ConversationId("200"), (sut.current as Cursor).conversationId)
        assertEquals(ConversationId("400"), (sut.previous as Cursor).conversationId)
    }

    @Test
    fun `when next and unrecoverable Error then move the cursor backwards and ignore error`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            Error(ConversationCursorError.Other(IllegalStateError)) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        // previous page is error
        assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.next)
        sut.moveForward()
        // current page is error
        assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.current)
        sut.moveForward()
        // prev page is error
        assertEquals(Error(ConversationCursorError.Other(IllegalStateError)), sut.previous)

        // next pages loaded as normal
        assertIs<Cursor>(sut.current)
        assertIs<Cursor>(sut.next)
        assertEquals(ConversationId("200"), (sut.current as Cursor).conversationId)
        assertEquals(ConversationId("400"), (sut.next as Cursor).conversationId)
    }

    @Test
    fun `when prev and Error is recoverable then set null and reload to recover`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.nextPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.previousPage() } returns
            Error(ConversationCursorError.Offline) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        // previous page is error
        assertEquals(null, sut.previous)
        assertIs<Cursor>(sut.current)
        assertEquals(conversationId, (sut.current as Cursor).conversationId)
        sut.moveBackward()
        assertIs<Cursor>(sut.next)
        assertEquals(conversationId, (sut.next as Cursor).conversationId)

        // page reloaded
        assertIs<Cursor>(sut.current)
        assertEquals(ConversationId("200"), (sut.current as Cursor).conversationId)
        verify(exactly = 3) { conversationCursorWrapper.previousPage() }
        verify(exactly = 1) { conversationCursorWrapper.goBackwards() }
    }

    @Test
    fun `when next and Error is recoverable then set null and reload to recover`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            Cursor(ConversationId("50")) andThen Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            Error(ConversationCursorError.Offline) andThen Cursor(ConversationId("200")) andThen
            Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        // then
        // previous page is error
        assertEquals(null, sut.next)
        assertIs<Cursor>(sut.current)
        assertEquals(conversationId, (sut.current as Cursor).conversationId)
        sut.moveForward()
        assertIs<Cursor>(sut.next)
        assertEquals(ConversationId("400"), (sut.next as Cursor).conversationId)
        // page reloaded
        assertIs<Cursor>(sut.current)
        assertEquals(ConversationId("200"), (sut.current as Cursor).conversationId)
        coVerify(exactly = 3) { conversationCursorWrapper.nextPage() }
        verify(exactly = 1) { conversationCursorWrapper.goForwards() }
    }
}
