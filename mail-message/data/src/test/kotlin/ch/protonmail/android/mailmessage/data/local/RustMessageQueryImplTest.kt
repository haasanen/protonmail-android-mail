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

import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.wrapper.MailboxMessagePaginatorWrapper
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
    private val invalidationTracker: RustInvalidationTracker = mockk(relaxUnitFun = true)

    private val rustMessageQuery = RustMessageQueryImpl(
        invalidationTracker,
        messagePaginatorManager
    )

    @Test
    fun `returns initial value from watcher when messages watcher is initialized`() = runTest {
        // Given
        val mailboxCallbackSlot = slot<LiveQueryCallback>()
        val userId = UserIdTestData.userId
        val localLabelId = LocalLabelId(1u)
        val pageKey = PageKey.DefaultPageKey(labelId = localLabelId.toLabelId())
        coEvery {
            messagePaginatorManager.getOrCreatePaginator(userId, pageKey, capture(mailboxCallbackSlot))
        } returns messagePaginator.right()
        coEvery { messagePaginator.nextPage() } returns expectedMessages.right()

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)
        // Then
        assertEquals(expectedMessages, actual)
        coVerify { messagePaginatorManager.getOrCreatePaginator(userId, pageKey, mailboxCallbackSlot.captured) }
    }

    @Test
    fun `invalidate paging data when mailbox live query callback is called`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val callbackSlot = slot<LiveQueryCallback>()
        val localLabelId = LocalLabelId(1u)
        val pageKey = PageKey.DefaultPageKey(labelId = localLabelId.toLabelId())
        coEvery {
            messagePaginatorManager.getOrCreatePaginator(userId, pageKey, capture(callbackSlot))
        } returns messagePaginator.right()
        coEvery { messagePaginator.nextPage() } returns emptyList<LocalMessageMetadata>().right()

        // When
        rustMessageQuery.getMessages(userId, pageKey)
        callbackSlot.captured.onUpdate()

        // Then
        verify { invalidationTracker.notifyInvalidation(any()) }
    }

    @Test
    fun `returns first page when called with PageToLoad First`() = runTest {
        // Given
        val expectedConversations = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedConversations.right()
        }
        coEvery { messagePaginatorManager.getOrCreatePaginator(userId, pageKey, any()) } returns paginator.right()

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
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.Next)
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedConversations.right()
        }
        coEvery { messagePaginatorManager.getOrCreatePaginator(userId, pageKey, any()) } returns paginator.right()

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
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.All)
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { this@mockk.reload() } returns expectedConversations.right()
        }
        coEvery { messagePaginatorManager.getOrCreatePaginator(userId, pageKey, any()) } returns paginator.right()

        // When
        val actual = rustMessageQuery.getMessages(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

}
