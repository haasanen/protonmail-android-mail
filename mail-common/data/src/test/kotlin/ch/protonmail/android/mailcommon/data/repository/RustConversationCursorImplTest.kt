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
import ch.protonmail.android.mailcommon.domain.model.CursorId
import ch.protonmail.android.mailcommon.domain.model.CursorResult
import ch.protonmail.android.mailcommon.domain.model.DataError.Local.IllegalStateError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class RustConversationCursorImplTest {

    private val conversationId = ConversationId("100")

    @Test
    fun `given cursor initialises then initialises correctly with next and previous pages`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns CursorResult.Cursor(ConversationId("200"))
        coEvery { conversationCursorWrapper.nextPage() } returns CursorResult.Cursor(ConversationId("50"))

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
        coEvery { conversationCursorWrapper.previousPage() } returns CursorResult.End
        coEvery { conversationCursorWrapper.nextPage() } returns CursorResult.Cursor(ConversationId("50"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        assertEquals(sut.previous, CursorResult.End)
    }

    @Test
    fun `given when next is end then current is end and goForwards is not called`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns CursorResult.Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns CursorResult.End

        val expectedConversationId = conversationId
        // when
        val sut = RustConversationCursorImpl(CursorId(expectedConversationId, null), conversationCursorWrapper)
        assertIs<CursorResult.Cursor>(sut.current)
        assertEquals(expectedConversationId, (sut.current as CursorResult.Cursor).conversationId)

        sut.moveForward()

        // then
        assertEquals(CursorResult.End, sut.current)
        coVerify(exactly = 0) { conversationCursorWrapper.goForwards() }
    }

    @Test
    fun `when next increments then moves the cursor forward`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns CursorResult.Cursor(ConversationId("50"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            CursorResult.Cursor(ConversationId("200")) andThen CursorResult.Cursor(ConversationId("300"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        assertEquals(conversationId, (sut.current as? CursorResult.Cursor)?.conversationId)
        sut.moveForward()

        // then
        coVerify { conversationCursorWrapper.goForwards() }
        assertIs<CursorResult.Cursor>(sut.current)
        assertIs<CursorResult.Cursor>(sut.previous)
        assertIs<CursorResult.Cursor>(sut.next)
        assertEquals(ConversationId("200"), (sut.current as CursorResult.Cursor).conversationId)
        assertEquals(conversationId, (sut.previous as CursorResult.Cursor).conversationId)
        assertEquals(ConversationId("300"), (sut.next as CursorResult.Cursor).conversationId)
        coVerify(exactly = 2) { conversationCursorWrapper.nextPage() }
    }


    @Test
    fun `when prev then move the cursor backwards`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            CursorResult.Cursor(ConversationId("50")) andThen CursorResult.Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns CursorResult.Cursor(ConversationId("200"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        assertIs<CursorResult.Cursor>(sut.current)
        assertEquals(conversationId, (sut.current as CursorResult.Cursor).conversationId)
        sut.moveBackward()

        // then
        coVerify { conversationCursorWrapper.goBackwards() }
        assertIs<CursorResult.Cursor>(sut.current)
        assertIs<CursorResult.Cursor>(sut.next)
        assertIs<CursorResult.Cursor>(sut.previous)
        assertEquals(ConversationId("50"), (sut.current as CursorResult.Cursor).conversationId)
        assertEquals(conversationId, (sut.next as CursorResult.Cursor).conversationId)
        assertEquals(ConversationId("300"), (sut.previous as CursorResult.Cursor).conversationId)
        coVerify(exactly = 2) { conversationCursorWrapper.previousPage() }
    }

    @Test
    fun `when prev and unrecoverable Error then move the cursor backwards and ignore error`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.nextPage() } returns
            CursorResult.Cursor(ConversationId("50")) andThen CursorResult.Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.previousPage() } returns
            CursorResult.Error(ConversationCursorError.Other(IllegalStateError)) andThen
            CursorResult.Cursor(ConversationId("200")) andThen
            CursorResult.Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        // previous page is error
        assertEquals(CursorResult.Error(ConversationCursorError.Other(IllegalStateError)), sut.previous)
        sut.moveBackward()
        // current page is error
        assertEquals(CursorResult.Error(ConversationCursorError.Other(IllegalStateError)), sut.current)
        sut.moveBackward()
        // next page is error
        assertEquals(CursorResult.Error(ConversationCursorError.Other(IllegalStateError)), sut.next)

        // prev pages loaded as normal
        assertIs<CursorResult.Cursor>(sut.current)
        assertIs<CursorResult.Cursor>(sut.previous)
        assertEquals(ConversationId("200"), (sut.current as CursorResult.Cursor).conversationId)
        assertEquals(ConversationId("400"), (sut.previous as CursorResult.Cursor).conversationId)
    }

    @Test
    fun `when next and unrecoverable Error then move the cursor backwards and ignore error`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            CursorResult.Cursor(ConversationId("50")) andThen CursorResult.Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            CursorResult.Error(ConversationCursorError.Other(IllegalStateError)) andThen
            CursorResult.Cursor(ConversationId("200")) andThen
            CursorResult.Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        // previous page is error
        assertEquals(CursorResult.Error(ConversationCursorError.Other(IllegalStateError)), sut.next)
        sut.moveForward()
        // current page is error
        assertEquals(CursorResult.Error(ConversationCursorError.Other(IllegalStateError)), sut.current)
        sut.moveForward()
        // prev page is error
        assertEquals(CursorResult.Error(ConversationCursorError.Other(IllegalStateError)), sut.previous)

        // next pages loaded as normal
        assertIs<CursorResult.Cursor>(sut.current)
        assertIs<CursorResult.Cursor>(sut.next)
        assertEquals(ConversationId("200"), (sut.current as CursorResult.Cursor).conversationId)
        assertEquals(ConversationId("400"), (sut.next as CursorResult.Cursor).conversationId)
    }

    @Test
    fun `when prev and Error is recoverable then set null and reload to recover`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.nextPage() } returns
            CursorResult.Cursor(ConversationId("50")) andThen CursorResult.Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.previousPage() } returns
            CursorResult.Error(ConversationCursorError.Offline) andThen
            CursorResult.Cursor(ConversationId("200")) andThen
            CursorResult.Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)

        // then
        // previous page is error
        assertEquals(null, sut.previous)
        assertIs<CursorResult.Cursor>(sut.current)
        assertEquals(conversationId, (sut.current as CursorResult.Cursor).conversationId)
        sut.moveBackward()
        assertIs<CursorResult.Cursor>(sut.next)
        assertEquals(conversationId, (sut.next as CursorResult.Cursor).conversationId)

        // page reloaded
        assertIs<CursorResult.Cursor>(sut.current)
        assertEquals(ConversationId("200"), (sut.current as CursorResult.Cursor).conversationId)
        verify(exactly = 3) { conversationCursorWrapper.previousPage() }
        verify(exactly = 1) { conversationCursorWrapper.goBackwards() }
    }

    @Test
    fun `when next and Error is recoverable then set null and reload to recover`() = runTest {
        // given
        val conversationCursorWrapper = mockk<ConversationCursor>(relaxed = true)
        coEvery { conversationCursorWrapper.previousPage() } returns
            CursorResult.Cursor(ConversationId("50")) andThen CursorResult.Cursor(ConversationId("300"))
        coEvery { conversationCursorWrapper.nextPage() } returns
            CursorResult.Error(ConversationCursorError.Offline) andThen
            CursorResult.Cursor(ConversationId("200")) andThen
            CursorResult.Cursor(ConversationId("400"))

        // when
        val sut = RustConversationCursorImpl(CursorId(conversationId, null), conversationCursorWrapper)
        // then
        // previous page is error
        assertEquals(null, sut.next)
        assertIs<CursorResult.Cursor>(sut.current)
        assertEquals(conversationId, (sut.current as CursorResult.Cursor).conversationId)
        sut.moveForward()
        assertIs<CursorResult.Cursor>(sut.next)
        assertEquals(ConversationId("400"), (sut.next as CursorResult.Cursor).conversationId)
        // page reloaded
        assertIs<CursorResult.Cursor>(sut.current)
        assertEquals(ConversationId("200"), (sut.current as CursorResult.Cursor).conversationId)
        coVerify(exactly = 3) { conversationCursorWrapper.nextPage() }
        verify(exactly = 1) { conversationCursorWrapper.goForwards() }
    }
}
