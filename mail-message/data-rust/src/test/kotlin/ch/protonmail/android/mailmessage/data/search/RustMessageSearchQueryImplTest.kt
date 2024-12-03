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

package ch.protonmail.android.mailmessage.data.search

import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.data.usecase.CreateRustSearchPaginator
import ch.protonmail.android.mailmessage.data.wrapper.MessagePaginatorWrapper
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.LiveQueryCallback
import kotlin.test.Test
import kotlin.test.assertEquals

class RustMessageSearchQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userId = UserIdSample.Primary
    private val expectedMessages = listOf(
        mockk<LocalMessageMetadata>(),
        mockk<LocalMessageMetadata>()
    )

    private val searchPaginator: MessagePaginatorWrapper = mockk()
    private val rustMailbox: RustMailbox = mockk(relaxUnitFun = true)
    private val createRustSearchPaginator: CreateRustSearchPaginator = mockk()
    private val userSessionRepository = mockk<UserSessionRepository>()
    private val invalidationTracker: RustInvalidationTracker = mockk(relaxUnitFun = true)

    private val rustMessageSearchQuery = RustMessageSearchQueryImpl(
        userSessionRepository,
        invalidationTracker,
        createRustSearchPaginator,
        rustMailbox
    )

    @Test
    fun `returns initial value from search when paginator is initialized`() = runTest {
        // Given
        val callbackSlot = slot<LiveQueryCallback>()
        val keyword = "keyword"
        val pageKey = PageKey.PageKeyForSearch(keyword)
        val userSession = mockk<MailUserSessionWrapper>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { searchPaginator.nextPage() } returns expectedMessages
        coEvery {
            createRustSearchPaginator.invoke(
                userSession,
                keyword, capture(callbackSlot)
            )
        } returns searchPaginator

        // When
        val actual = rustMessageSearchQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedMessages, actual)
        coVerify { createRustSearchPaginator(userSession, keyword, callbackSlot.captured) }
    }

    @Test
    fun `get messages switches mailbox when all mail label is found`() = runTest {
        // Given
        val keyword = "keyword"
        val pageKey = PageKey.PageKeyForSearch(keyword)
        val userSession = mockk<MailUserSessionWrapper>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { searchPaginator.nextPage() } returns expectedMessages
        coEvery { createRustSearchPaginator.invoke(userSession, keyword, any()) } returns searchPaginator
        coEvery { rustMailbox.switchToAllMailMailbox(userId) } just Runs

        // When
        val actual = rustMessageSearchQuery.getMessages(userId, pageKey)

        // Then
        assertNotNull(actual)
        coVerify { rustMailbox.switchToAllMailMailbox(userId) }
    }

    @Test
    fun `invalidate paging data when search live query callback is called`() = runTest {
        // Given
        val callbackSlot = slot<LiveQueryCallback>()
        val keyword = "test"
        val pageKey = PageKey.PageKeyForSearch(keyword)
        val userSession = mockk<MailUserSessionWrapper>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { searchPaginator.nextPage() } returns emptyList()
        coEvery {
            createRustSearchPaginator.invoke(
                userSession, keyword,
                capture(callbackSlot)
            )
        } returns searchPaginator

        // When
        rustMessageSearchQuery.getMessages(userId, pageKey)
        callbackSlot.captured.onUpdate()

        // Then
        verify { invalidationTracker.notifyInvalidation(any()) }
    }

    @Test
    fun `returns first page when called with PageToLoad First`() = runTest {
        // Given
        val expectedConversations = listOf(mockk<LocalMessageMetadata>())
        val keyword = "keyword"
        val pageKey = PageKey.PageKeyForSearch(keyword, PageToLoad.First)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<MessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustSearchPaginator(session, keyword, any()) } returns paginator

        // When
        val actual = rustMessageSearchQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `returns next page when called with PageToLoad Next`() = runTest {
        // Given
        val expectedConversations = listOf(mockk<LocalMessageMetadata>())
        val keyword = "keyword"
        val pageKey = PageKey.PageKeyForSearch(keyword, PageToLoad.Next)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<MessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustSearchPaginator(session, keyword, any()) } returns paginator

        // When
        val actual = rustMessageSearchQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `returns all pages when called with PageToLoad All`() = runTest {
        // Given
        val expectedConversations = listOf(mockk<LocalMessageMetadata>())
        val keyword = "keyword"
        val pageKey = PageKey.PageKeyForSearch(keyword, PageToLoad.All)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<MessagePaginatorWrapper> {
            coEvery { this@mockk.reload() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustSearchPaginator(session, keyword, any()) } returns paginator

        // When
        val actual = rustMessageSearchQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `switches rust Mailbox to all mail label when creating a new paginator`() = runTest {
        // Given
        val expectedConversations = listOf(mockk<LocalMessageMetadata>())
        val keyword = "keyword"
        val pageKey = PageKey.PageKeyForSearch(keyword)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<MessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustSearchPaginator(session, keyword, any()) } returns paginator
        coEvery { rustMailbox.switchToAllMailMailbox(userId) } just Runs

        // When
        rustMessageSearchQuery.getMessages(userId, pageKey)

        // Then
        coVerify(exactly = 1) { rustMailbox.switchToAllMailMailbox(userId) }
    }

    @Test
    fun `initializes paginator only once for the same keyword`() = runTest {
        // Given
        val firstPage = listOf(mockk<LocalMessageMetadata>())
        val nextPage = listOf(mockk<LocalMessageMetadata>())
        val keyword = "keyword"
        val pageKey = PageKey.PageKeyForSearch(keyword)
        val nextPageKey = pageKey.copy(pageToLoad = PageToLoad.Next)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<MessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns firstPage
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustSearchPaginator(session, keyword, any()) } returns paginator

        // When
        rustMessageSearchQuery.getMessages(userId, pageKey)
        coEvery { paginator.nextPage() } returns nextPage
        rustMessageSearchQuery.getMessages(userId, nextPageKey)

        // Then
        coVerify(exactly = 1) { createRustSearchPaginator(session, keyword, any()) }
        coVerify(exactly = 1) { rustMailbox.switchToAllMailMailbox(userId) }
    }

    @Test
    fun `reinitializes paginator when keyword changes`() = runTest {
        // Given
        val firstPage = listOf(mockk<LocalMessageMetadata>())
        val keyword = "first query"
        val newKeyword = "second query"
        val pageKey = PageKey.PageKeyForSearch(keyword)
        val newPageKey = PageKey.PageKeyForSearch(newKeyword)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<MessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns firstPage
            coEvery { this@mockk.destroy() } just Runs
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustSearchPaginator(session, keyword, any()) } returns paginator
        coEvery { createRustSearchPaginator(session, newKeyword, any()) } returns paginator

        // When
        rustMessageSearchQuery.getMessages(userId, pageKey)
        rustMessageSearchQuery.getMessages(userId, newPageKey)

        // Then
        coVerify(exactly = 1) { createRustSearchPaginator(session, keyword, any()) }
        coVerify { paginator.destroy() }
        coVerify(exactly = 1) { createRustSearchPaginator(session, newKeyword, any()) }
    }
}
