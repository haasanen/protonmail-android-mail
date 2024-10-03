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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesPaginator
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
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
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MessagePaginator
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

    private val messagePaginator: MessagePaginator = mockk()
    private val rustMailbox: RustMailbox = mockk(relaxUnitFun = true)
    private val createRustMessagesPaginator: CreateRustMessagesPaginator = mockk()
    private val userSessionRepository = mockk<UserSessionRepository>()

    private val invalidationTracker: RustInvalidationTracker = mockk(relaxUnitFun = true)

    private val rustMessageQuery = RustMessageQueryImpl(
        userSessionRepository,
        invalidationTracker,
        createRustMessagesPaginator,
        rustMailbox
    )

    @Test
    fun `returns initial value from watcher when messages watcher is initialized`() = runTest {
        // Given
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val userId = UserIdTestData.userId
        val localLabelId = LocalLabelId(1u)
        val pageKey = PageKey(labelId = localLabelId.toLabelId())
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { messagePaginator.nextPage() } returns expectedMessages
        coEvery {
            createRustMessagesPaginator.invoke(
                userSession,
                localLabelId,
                capture(mailboxCallbackSlot)
            )
        } returns messagePaginator
        coEvery { rustMailbox.switchToMailbox(userId, localLabelId) } just Runs

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)
        // Then
        assertEquals(expectedMessages, actual)
        coVerify { createRustMessagesPaginator(userSession, localLabelId, mailboxCallbackSlot.captured) }
    }

    @Test
    fun `get messages with labelId switches mailbox if needed`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val localLabelId = LocalLabelId(1u)
        val pageKey = PageKey(labelId = localLabelId.toLabelId())
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { rustMailbox.switchToMailbox(userId, localLabelId) } just Runs
        coEvery { messagePaginator.nextPage() } returns expectedMessages
        coEvery { createRustMessagesPaginator.invoke(userSession, localLabelId, any()) } returns messagePaginator

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertNotNull(actual)
        coVerify { rustMailbox.switchToMailbox(userId, localLabelId) }
    }

    @Test
    fun `invalidate paging data when mailbox live query callback is called`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val localLabelId = LocalLabelId(1u)
        val pageKey = PageKey(labelId = localLabelId.toLabelId())
        val userSession = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { rustMailbox.switchToMailbox(userId, localLabelId) } just Runs
        coEvery { messagePaginator.nextPage() } returns emptyList()
        coEvery {
            createRustMessagesPaginator.invoke(
                userSession,
                localLabelId,
                capture(mailboxCallbackSlot)
            )
        } returns messagePaginator

        // When
        rustMessageQuery.getMessages(userId, pageKey)
        mailboxCallbackSlot.captured.onUpdate()

        // Then
        verify { invalidationTracker.notifyInvalidation(any()) }
    }

    @Test
    fun `returns first page when called with PageToLoad First`() = runTest {
        // Given
        val expectedConversations = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId)
        val session = mockk<MailUserSession>()
        val paginator = mockk<MessagePaginator> {
            coEvery { this@mockk.nextPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `returns next page when called with PageToLoad Next`() = runTest {
        // Given
        val expectedConversations = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId, pageToLoad = PageToLoad.Next)
        val session = mockk<MailUserSession>()
        val paginator = mockk<MessagePaginator> {
            coEvery { this@mockk.nextPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `returns all pages when called with PageToLoad All`() = runTest {
        // Given
        val expectedConversations = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId, pageToLoad = PageToLoad.All)
        val session = mockk<MailUserSession>()
        val paginator = mockk<MessagePaginator> {
            coEvery { this@mockk.reload() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `switches rust Mailbox to new label when creating a new paginator`() = runTest {
        // Given
        val expectedConversations = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId)
        val session = mockk<MailUserSession>()
        val paginator = mockk<MessagePaginator> {
            coEvery { this@mockk.nextPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        rustMessageQuery.getMessages(userId, pageKey)

        // Then
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, labelId.toLocalLabelId()) }
    }

    @Test
    fun `initialised paginator only once for any given label`() = runTest {
        // Given
        val firstPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val nextPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId)
        val nextPageKey = pageKey.copy(pageToLoad = PageToLoad.Next)
        val session = mockk<MailUserSession>()
        val paginator = mockk<MessagePaginator> {
            coEvery { this@mockk.nextPage() } returns firstPage
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        rustMessageQuery.getMessages(userId, pageKey)
        coEvery { paginator.nextPage() } returns nextPage
        rustMessageQuery.getMessages(userId, nextPageKey)

        // Then
        coVerify(exactly = 1) { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) }
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, labelId.toLocalLabelId()) }
    }

    @Test
    fun `re initialises paginator when labelId changes`() = runTest {
        // Given
        val firstPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val newLabelId = SystemLabelId.Archive.labelId
        val pageKey = PageKey(labelId = labelId)
        val newPageKey = pageKey.copy(newLabelId)
        val session = mockk<MailUserSession>()
        val paginator = mockk<MessagePaginator> {
            coEvery { this@mockk.nextPage() } returns firstPage
            coEvery { this@mockk.handle().disconnect() } just Runs
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator
        coEvery { createRustMessagesPaginator(session, newLabelId.toLocalLabelId(), any()) } returns paginator

        // When
        rustMessageQuery.getMessages(userId, pageKey)
        rustMessageQuery.getMessages(userId, newPageKey)

        // Then
        coVerify(exactly = 1) { createRustMessagesPaginator(session, labelId.toLocalLabelId(), any()) }
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, labelId.toLocalLabelId()) }

        coVerify { paginator.handle().disconnect() }

        coVerify(exactly = 1) { createRustMessagesPaginator(session, newLabelId.toLocalLabelId(), any()) }
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, newLabelId.toLocalLabelId()) }
    }
}
