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

package ch.protonmail.android.mailpagination.data.scroller

import ch.protonmail.android.mailpagination.data.model.scroller.PendingRequest
import ch.protonmail.android.mailpagination.data.model.scroller.RequestType
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uniffi.proton_mail_uniffi.MailScrollerError
import uniffi.proton_mail_uniffi.ProtonError

class ScrollerOnUpdateHandlerTest {

    val invalidate = mockk<() -> Unit>(relaxed = true)

    private fun handler() = ScrollerOnUpdateHandler<ScrollerItem>(tag = "TEST", invalidate = invalidate)

    private fun pendingAppendRequest(): PendingRequest<ScrollerItem> =
        PendingRequest(RequestType.Append, CompletableDeferred())

    private fun pendingRefreshRequest(): PendingRequest<ScrollerItem> =
        PendingRequest(RequestType.Refresh, CompletableDeferred())


    @Test
    fun `when there is no pending request triggers invalidate`() = runTest {
        // Given
        val update = ScrollerUpdate.None
        val snapshot = emptyList<ScrollerItem>()

        // When
        handler().handleUpdate(pending = null, update = update, cacheSnapshot = snapshot)

        // Then
        verify(exactly = 1) { invalidate.invoke() }
    }

    @Test
    fun `when immediate Append response is received, completes with items from response`() = runTest {
        // Given
        val pending = pendingAppendRequest()
        val items = listOf(ScrollerItem("1"), ScrollerItem("2"))
        val update = ScrollerUpdate.Append(items)
        val snapshot = listOf(ScrollerItem("99")) // ignored in this path

        // When
        handler().handleUpdate(pending, update, snapshot)
        val completed = pending.response.await()

        // Then
        assertEquals(items, completed.getOrNull())
        verify(exactly = 0) { invalidate.invoke() }
    }

    @Test
    fun `when immediate Append None is received, completes with empty list`() = runTest {
        // Given
        val pending = pendingAppendRequest()
        val update = ScrollerUpdate.None
        val snapshot = listOf(ScrollerItem("1")) // ignored

        // When
        handler().handleUpdate(pending, update, snapshot)
        val completed = pending.response.await()

        // Then
        assertEquals(emptyList<ScrollerItem>(), completed.getOrNull())
        verify(exactly = 0) { invalidate.invoke() }
    }

    @Test
    fun `when Append Error is received, completes with error`() = runTest {
        // Given
        val pending = pendingAppendRequest()
        val update = ScrollerUpdate.Error(MailScrollerError.Other(ProtonError.Network))
        val snapshot = emptyList<ScrollerItem>() // ignored

        // When
        handler().handleUpdate(pending, update, snapshot)
        val completed = pending.response.await()

        // Then
        assertTrue(completed.isLeft())
        verify(exactly = 0) { invalidate.invoke() }
    }

    @Test
    fun `when unexpected response is received for Append, handles as indirect and completes with empty list`() =
        runTest {
            // Given
            val pending = pendingAppendRequest()
            val update = ScrollerUpdate.ReplaceFrom(idx = 0, items = listOf(ScrollerItem("42")))
            val snapshot = listOf(ScrollerItem("7"))

            // When
            handler().handleUpdate(pending, update, snapshot)
            val completed = pending.response.await()

            // Then
            assertEquals(emptyList<ScrollerItem>(), completed.getOrNull())
            verify(exactly = 0) { invalidate.invoke() }
        }

    @Test
    fun `when immediate ReplaceFrom zero is received for Refresh request, completes with items from response`() =
        runTest {
            // Given
            val pending = pendingRefreshRequest()
            val items = listOf(ScrollerItem("10"), ScrollerItem("11"))
            val update = ScrollerUpdate.ReplaceFrom(idx = 0, items = items)
            val snapshot = listOf(ScrollerItem("99")) // ignored in this path

            // When
            handler().handleUpdate(pending, update, snapshot)
            val completed = pending.response.await()

            // Then
            assertEquals(items, completed.getOrNull())
            verify(exactly = 0) { invalidate.invoke() }
        }

    @Test
    fun `when unexpected response is received for Refresh, completes with snapshot`() = runTest {
        // Given
        val pending = pendingRefreshRequest()
        val update = ScrollerUpdate.ReplaceFrom(idx = 5, items = listOf(ScrollerItem("1")))
        val snapshot = listOf(ScrollerItem("100"), ScrollerItem("101"))

        // When
        handler().handleUpdate(pending, update, snapshot)
        val completed = pending.response.await()

        // Then
        assertEquals(snapshot, completed.getOrNull())
        verify(exactly = 0) { invalidate.invoke() }
    }

    @Test
    fun `when None is received for Refresh request, completes with snapshot`() = runTest {
        // Given
        val pending = pendingRefreshRequest()
        val update = ScrollerUpdate.None
        val snapshot = listOf(ScrollerItem("200"))

        // When
        val result = handler().handleUpdate(pending, update, snapshot)
        val completed = pending.response.await()

        // Then
        assertEquals(snapshot, completed.getOrNull())
        verify(exactly = 0) { invalidate.invoke() }
    }

    @Test
    fun `when Error is received for Refresh request, completes with snapshot`() = runTest {
        // Note: For refresh we use getItems, and getItems should neve fail with error
        // Given
        val pending = pendingRefreshRequest()
        val update = ScrollerUpdate.Error(MailScrollerError.Other(ProtonError.Network))
        val snapshot = listOf(ScrollerItem("300"), ScrollerItem("301"))

        // When
        handler().handleUpdate(pending, update, snapshot)
        val completed = pending.response.await()

        // Then
        assertEquals(snapshot, completed.getOrNull())
        verify(exactly = 0) { invalidate.invoke() }
    }
}
