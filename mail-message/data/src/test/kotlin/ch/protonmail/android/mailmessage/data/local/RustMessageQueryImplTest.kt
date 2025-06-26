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

package ch.protonmail.android.mailmessage.data.local

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.wrapper.MailboxMessagePaginatorWrapper
import ch.protonmail.android.mailpagination.domain.cache.PagingCacheWithInvalidationFilter
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.LiveQueryCallback
import kotlin.test.Test
import kotlin.test.assertEquals

class RustMessageQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expectedMessages = listOf(
        LocalMessageTestData.AugWeatherForecast,
        LocalMessageTestData.SepWeatherForecast,
        LocalMessageTestData.OctWeatherForecast
    )

    private val messagePaginator: MailboxMessagePaginatorWrapper = mockk()

    private val messagePaginatorManager = mockk<MessagePaginatorManager>()
    private val cacheWithInvalidationFilter = mockk<PagingCacheWithInvalidationFilter<LocalMessageMetadata>> {
        coEvery { reset() } just Runs
    }

    private val rustMessageQuery = RustMessageQueryImpl(
        messagePaginatorManager,
        CoroutineScope(mainDispatcherRule.testDispatcher),
        cacheWithInvalidationFilter
    )

    @Test
    fun `returns initial value from watcher when messages watcher is initialized`() = runTest {
        // Given
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val userId = UserIdTestData.userId
        val localLabelId = LocalLabelId(1u)
        val pageKey = PageKey.DefaultPageKey(labelId = localLabelId.toLabelId())
        coEvery {
            messagePaginatorManager.getOrCreatePaginator(
                userId, pageKey, capture(mailboxCallbackSlot), any()
            )
        } returns messagePaginator.right()
        coEvery { messagePaginator.nextPage() } returns expectedMessages.right()
        coEvery { cacheWithInvalidationFilter.replaceData(expectedMessages, true) } just Runs

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedMessages, actual)
        coVerify {
            messagePaginatorManager.getOrCreatePaginator(
                userId, pageKey, mailboxCallbackSlot.captured, any()
            )
        }
        coVerify { cacheWithInvalidationFilter.replaceData(expectedMessages, true) }
    }

    @Test
    fun `returns first page when called with PageToLoad First`() = runTest {
        // Given
        val expectedMessages = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedMessages.right()
        }
        coEvery {
            messagePaginatorManager.getOrCreatePaginator(
                userId, pageKey, any(), any()
            )
        } returns paginator.right()
        coEvery { cacheWithInvalidationFilter.replaceData(expectedMessages, true) } just Runs

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedMessages, actual)
        coVerify { cacheWithInvalidationFilter.replaceData(expectedMessages, true) }
    }

    @Test
    fun `returns next page when called with PageToLoad Next`() = runTest {
        // Given
        val expectedMessages = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.Next)
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedMessages.right()
        }
        coEvery {
            messagePaginatorManager.getOrCreatePaginator(
                userId, pageKey, any(), any()
            )
        } returns paginator.right()
        coEvery { cacheWithInvalidationFilter.storeNextPage(expectedMessages) } just Runs

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedMessages, actual)
        coVerify { cacheWithInvalidationFilter.storeNextPage(expectedMessages) }
    }

    @Test
    fun `returns all pages when called with PageToLoad All`() = runTest {
        // Given
        val expectedMessages = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.All)
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { this@mockk.reload() } returns expectedMessages.right()
        }
        coEvery {
            messagePaginatorManager.getOrCreatePaginator(
                userId, pageKey, any(), any()
            )
        } returns paginator.right()
        coEvery { messagePaginatorManager.getPaginator() } returns paginator
        coEvery { cacheWithInvalidationFilter.popUnseenData(any(), any()) } returns null
        coEvery { cacheWithInvalidationFilter.replaceData(expectedMessages, true) } just Runs

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedMessages, actual)
        coVerify { cacheWithInvalidationFilter.replaceData(expectedMessages, true) }

    }

    @Test
    fun `calls reload when paginator throw 'dirty state' error`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.Next)
        val dirtyStateError = PaginationError.DirtyPaginationData
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { this@mockk.reload() } returns emptyList<LocalMessageMetadata>().right()
            coEvery { this@mockk.nextPage() } returns dirtyStateError.left()
        }
        coEvery {
            messagePaginatorManager.getOrCreatePaginator(
                userId, pageKey, any(), any()
            )
        } returns paginator.right()

        // When
        rustMessageQuery.getMessages(userId, pageKey)

        // Then
        coVerifySequence {
            paginator.nextPage()
            paginator.reload()
        }
    }

    @Test
    fun `submits invalidation when onUpdate callback is fired`() = runTest {
        // Given
        val event = PageInvalidationEvent.MessagesInvalidated
        val firstPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)

        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { nextPage() } returns firstPage.right()
        }
        coEvery { messagePaginatorManager.getPaginator() } returns paginator
        val callbackSlot = slot<LiveQueryCallback>()

        coEvery {
            messagePaginatorManager.getOrCreatePaginator(
                userId, pageKey, capture(callbackSlot), any()
            )
        } returns paginator.right()

        coEvery { cacheWithInvalidationFilter.submitInvalidation(event, any()) } just Runs
        coEvery { cacheWithInvalidationFilter.replaceData(firstPage, true) } just Runs

        // When
        rustMessageQuery.getMessages(userId, pageKey)
        callbackSlot.captured.onUpdate()

        advanceUntilIdle()

        // Then
        coVerify {
            cacheWithInvalidationFilter.submitInvalidation(
                event,
                any()
            )
        }
    }
}
