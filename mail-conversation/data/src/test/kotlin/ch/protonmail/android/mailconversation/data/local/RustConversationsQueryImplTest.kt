package ch.protonmail.android.mailconversation.data.local

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.LiveQueryCallback
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RustConversationsQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createRustConversationPaginator = mockk<CreateRustConversationPaginator>()
    private val userSessionRepository = mockk<UserSessionRepository>()
    private val invalidationTracker = mockk<RustInvalidationTracker>(relaxUnitFun = true)


    private val rustConversationsQuery = RustConversationsQueryImpl(
        userSessionRepository,
        invalidationTracker,
        createRustConversationPaginator
    )

    @Test
    fun `returns null when no valid session is found`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val pageKey = PageKey.DefaultPageKey()
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertNull(actual)
        verify { createRustConversationPaginator wasNot Called }
    }

    @Test
    fun `returns first page when called with PageToLoad First`() = runTest {
        // Given
        val expectedConversations = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedConversations.right()
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(session, labelId.toLocalLabelId(), false, any())
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `returns next page when called with PageToLoad Next`() = runTest {
        // Given
        val expectedConversations = listOf(LocalConversationTestData.OctConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.Next)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns expectedConversations.right()
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(
                session,
                labelId.toLocalLabelId(),
                false,
                any()
            )
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `returns all pages when called with PageToLoad All`() = runTest {
        // Given
        val expectedConversations = listOf(LocalConversationTestData.spamConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.All)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.reload() } returns expectedConversations.right()
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(
                session,
                labelId.toLocalLabelId(),
                false,
                any()
            )
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `initialised paginator only once for any given label`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val nextPage = listOf(LocalConversationTestData.OctConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val nextPageKey = pageKey.copy(pageToLoad = PageToLoad.Next)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns firstPage.right()
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(
                session,
                labelId.toLocalLabelId(),
                false,
                any()
            )
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        coEvery { paginator.nextPage() } returns nextPage.right()
        rustConversationsQuery.getConversations(userId, nextPageKey)

        // Then
        coVerify(exactly = 1) { createRustConversationPaginator(session, labelId.toLocalLabelId(), false, any()) }
    }

    @Test
    fun `re initialises paginator when labelId changes`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val newLabelId = SystemLabelId.Archive.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val newPageKey = pageKey.copy(newLabelId)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns firstPage.right()
            coEvery { this@mockk.disconnect() } just Runs
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(
                session, labelId.toLocalLabelId(),
                false, any()
            )
        } returns paginator.right()
        coEvery {
            createRustConversationPaginator(
                session, newLabelId.toLocalLabelId(),
                false, any()
            )
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, newPageKey)

        // Then
        coVerify(exactly = 1) {
            createRustConversationPaginator(
                session, labelId.toLocalLabelId(),
                false, any()
            )
        }

        coVerify { paginator.disconnect() }

        coVerify(exactly = 1) {
            createRustConversationPaginator(
                session, newLabelId.toLocalLabelId(),
                false, any()
            )
        }
    }

    @Test
    fun `re initialises paginator when read status changes`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val readStatus = ReadStatus.All
        val newReadStatus = ReadStatus.Unread
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, readStatus = readStatus)
        val newPageKey = pageKey.copy(readStatus = newReadStatus)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns firstPage.right()
            coEvery { this@mockk.disconnect() } just Runs
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(
                session,
                labelId.toLocalLabelId(),
                false,
                any()
            )
        } returns paginator.right()
        coEvery {
            createRustConversationPaginator(
                session,
                labelId.toLocalLabelId(),
                true,
                any()
            )
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, newPageKey)

        // Then
        coVerify(exactly = 1) { createRustConversationPaginator(session, labelId.toLocalLabelId(), false, any()) }

        coVerify { paginator.disconnect() }

        coVerify(exactly = 1) { createRustConversationPaginator(session, labelId.toLocalLabelId(), true, any()) }
    }

    @Test
    fun `invalidates data source when onUpdated callback is fired`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val session = mockk<MailUserSessionWrapper>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } returns firstPage.right()
        }
        val callbackSlot = slot<LiveQueryCallback>()
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(session, labelId.toLocalLabelId(), false, capture(callbackSlot))
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        callbackSlot.captured.onUpdate()

        // Then
        coVerify {
            invalidationTracker.notifyInvalidation(setOf(RustDataSourceId.CONVERSATION, RustDataSourceId.LABELS))
        }
    }
}
