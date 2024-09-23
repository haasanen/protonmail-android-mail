package ch.protonmail.android.mailconversation.data.local

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.domain.paging.RustDataSourceId
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
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
import uniffi.proton_mail_uniffi.ConversationPaginator
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSession
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RustConversationsQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createRustConversationPaginator = mockk<CreateRustConversationPaginator>()
    private val rustMailbox = mockk<RustMailbox>(relaxUnitFun = true)
    private val userSessionRepository = mockk<UserSessionRepository>()
    private val invalidationTracker = mockk<RustInvalidationTracker>(relaxUnitFun = true)


    private val rustConversationsQuery = RustConversationsQueryImpl(
        userSessionRepository,
        invalidationTracker,
        createRustConversationPaginator,
        rustMailbox
    )

    @Test
    fun `returns null when no valid session is found`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val pageKey = PageKey()
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
        val pageKey = PageKey(labelId = labelId)
        val session = mockk<MailUserSession>()
        val paginator = mockk<ConversationPaginator> {
            coEvery { this@mockk.currentPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

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
        val pageKey = PageKey(labelId = labelId, pageToLoad = PageToLoad.Next)
        val session = mockk<MailUserSession>()
        val paginator = mockk<ConversationPaginator> {
            coEvery { this@mockk.nextPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

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
        val pageKey = PageKey(labelId = labelId, pageToLoad = PageToLoad.All)
        val session = mockk<MailUserSession>()
        val paginator = mockk<ConversationPaginator> {
            coEvery { this@mockk.reload() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedConversations, actual)
    }

    @Test
    fun `switches rust Mailbox to new label when creating a new paginator`() = runTest {
        // Given
        val expectedConversations = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId)
        val session = mockk<MailUserSession>()
        val paginator = mockk<ConversationPaginator> {
            coEvery { this@mockk.currentPage() } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, labelId.toLocalLabelId()) }
    }

    @Test
    fun `initialised paginator only once for any given label`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val nextPage = listOf(LocalConversationTestData.OctConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId)
        val nextPageKey = pageKey.copy(pageToLoad = PageToLoad.Next)
        val session = mockk<MailUserSession>()
        val paginator = mockk<ConversationPaginator> {
            coEvery { this@mockk.currentPage() } returns firstPage
            coEvery { this@mockk.nextPage() } returns nextPage
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, nextPageKey)

        // Then
        coVerify(exactly = 1) { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) }
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, labelId.toLocalLabelId()) }
    }

    @Test
    fun `re initialises paginator when labelId changes`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val newLabelId = SystemLabelId.Archive.labelId
        val pageKey = PageKey(labelId = labelId)
        val newPageKey = pageKey.copy(newLabelId)
        val session = mockk<MailUserSession>()
        val paginator = mockk<ConversationPaginator> {
            coEvery { this@mockk.currentPage() } returns firstPage
            coEvery { this@mockk.handle().disconnect() } just Runs
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) } returns paginator
        coEvery { createRustConversationPaginator(session, newLabelId.toLocalLabelId(), any()) } returns paginator

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, newPageKey)

        // Then
        coVerify(exactly = 1) { createRustConversationPaginator(session, labelId.toLocalLabelId(), any()) }
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, labelId.toLocalLabelId()) }

        coVerify { paginator.handle().disconnect() }

        coVerify(exactly = 1) { createRustConversationPaginator(session, newLabelId.toLocalLabelId(), any()) }
        coVerify(exactly = 1) { rustMailbox.switchToMailbox(userId, newLabelId.toLocalLabelId()) }
    }

    @Test
    fun `invalidates data source when onUpdated callback is fired`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val nextPage = listOf(LocalConversationTestData.OctConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey(labelId = labelId)
        val session = mockk<MailUserSession>()
        val paginator = mockk<ConversationPaginator> {
            coEvery { this@mockk.currentPage() } returns firstPage
        }
        val callbackSlot = slot<LiveQueryCallback>()
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustConversationPaginator(session, labelId.toLocalLabelId(), capture(callbackSlot))
        } returns paginator

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        callbackSlot.captured.onUpdate()

        // Then
        coVerify {
            invalidationTracker.notifyInvalidation(setOf(RustDataSourceId.CONVERSATION, RustDataSourceId.LABELS))
        }
    }
}
