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

package ch.protonmail.android.mailpagination.data.cache

import ch.protonmail.android.mailpagination.domain.cache.PagingFetcher
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kotlin.test.assertNull
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.repository.PageInvalidationRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse

class PagingCacheWithInvalidationFilterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fetcher: PagingFetcher<LocalConversationStub> = mockk()
    private val pageInvalidationRepository: PageInvalidationRepository = mockk()
    private val cache = PagingCacheWithInvalidationFilterImpl<LocalConversationStub>(
        pageInvalidationRepository = pageInvalidationRepository,
        coroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)
    )

    private val conversationSample1 = LocalConversationStub(
        id = "1",
        subject = "Subject 1",
        timestamp = 1_622_547_800_000L
    )

    private val conversationSample2 = LocalConversationStub(
        id = "2",
        subject = "Subject 2",
        timestamp = 1_622_547_900_000L
    )

    @Test
    fun `forwards invalidation event when cache is empty`() = runTest {
        // Given
        val event = PageInvalidationEvent.ConversationsInvalidated
        coEvery { pageInvalidationRepository.submit(event) } just Runs

        // When
        cache.submitInvalidation(event, fetcher)

        // Then
        coVerify(exactly = 1) { pageInvalidationRepository.submit(event) }
    }

    @Test
    fun `updates cache and forwards event when new data is different`() = runTest {
        // Given
        val event = PageInvalidationEvent.ConversationsInvalidated
        val oldData = listOf(conversationSample1)
        val newData = listOf(conversationSample2)
        cache.replaceData(oldData, markAsSeen = true)

        coEvery { fetcher.reload() } returns newData
        coEvery { pageInvalidationRepository.submit(event) } just Runs

        // When
        cache.submitInvalidation(event, fetcher)

        // Then
        coVerify { fetcher.reload() }
        coVerify { pageInvalidationRepository.submit(event) }
    }

    @Test
    fun `skips invalidation when fetched data is unchanged`() = runTest {
        // Given
        val event = PageInvalidationEvent.ConversationsInvalidated
        val data = listOf(conversationSample1)
        cache.replaceData(data, markAsSeen = true)

        coEvery { fetcher.reload() } returns data

        // When
        cache.submitInvalidation(event, fetcher)

        // Then
        coVerify { fetcher.reload() }
        coVerify(exactly = 0) { pageInvalidationRepository.submit(any()) }
    }

    @Test
    fun `returns cached data and triggers background refresh when unseen data exists`() = runTest {
        // Given
        val event = PageInvalidationEvent.ConversationsInvalidated
        val data = listOf(conversationSample1)
        val updated = listOf(conversationSample2)
        cache.replaceData(data, markAsSeen = true)

        coEvery { fetcher.reload() } returns updated
        coEvery { pageInvalidationRepository.submit(event) } just Runs

        // Mark unseen
        cache.submitInvalidation(event, fetcher)
        advanceUntilIdle()

        // When
        val popped = cache.popUnseenData(event, fetcher)
        advanceUntilIdle()

        // Then
        assertEquals(updated, popped)
        coVerify(atLeast = 2) { fetcher.reload() }
        coVerify(atLeast = 1) { pageInvalidationRepository.submit(event) }
    }

    @Test
    fun `returns null when there is no unseen data`() = runTest {
        // Given
        val event = PageInvalidationEvent.ConversationsInvalidated
        val data = listOf(conversationSample1)
        cache.replaceData(data, markAsSeen = true)

        // When
        val result = cache.popUnseenData(event, fetcher)

        // Then
        assertNull(result)
    }

    @Test
    fun `clears cache and unseen flag on reset`() = runTest {
        // Given
        val event = PageInvalidationEvent.ConversationsInvalidated
        val data = listOf(conversationSample1)
        cache.replaceData(data, markAsSeen = true)

        // When
        cache.reset()

        // Then
        assertFalse(cache.hasUnseenData())
        val result = cache.popUnseenData(event, fetcher)
        assertNull(result)
    }

    @Test
    fun `replaces cache with new data and marks as seen when requested`() = runTest {
        // Given
        val event = PageInvalidationEvent.ConversationsInvalidated
        val data = listOf(conversationSample1)

        // When
        cache.replaceData(data, markAsSeen = true)

        // Then
        assertFalse(cache.hasUnseenData())
        val popped = cache.popUnseenData(event, fetcher)
        assertNull(popped)
    }


    private data class LocalConversationStub(
        val id: String,
        val subject: String,
        val timestamp: Long
    )
}
