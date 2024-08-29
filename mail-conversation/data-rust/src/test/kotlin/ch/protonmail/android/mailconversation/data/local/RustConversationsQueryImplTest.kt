package ch.protonmail.android.mailconversation.data.local

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.mapper.LocalLabelId
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationForLabelWatcher
import ch.protonmail.android.mailmessage.data.local.RustMailbox
import ch.protonmail.android.mailmessage.domain.paging.RustInvalidationTracker
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.WatchedConversations
import kotlin.test.Test
import kotlin.test.assertEquals

class RustConversationsQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val expectedConversations = listOf(
        LocalConversationTestData.AugConversation,
        LocalConversationTestData.SepConversation
    )

    private val rustMailbox: RustMailbox = mockk()
    private val userSessionRepository = mockk<UserSessionRepository>()
    private val createRustConversationForLabelWatcher = mockk<CreateRustConversationForLabelWatcher>()
    private val invalidationTracker: RustInvalidationTracker = mockk {
        every { notifyInvalidation(any()) } just Runs
    }

    private val rustConversationMessageQuery = RustConversationsQueryImpl(
        userSessionRepository,
        invalidationTracker,
        createRustConversationForLabelWatcher,
        rustMailbox,
        testCoroutineScope
    )

    @Test
    fun `switches mailbox to the current label id when observe is called`() {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val labelId = LocalLabelId(1uL)
        val mockWatcher = mockk<WatchedConversations> {
            coEvery { this@mockk.conversations } returns expectedConversations
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        coEvery { createRustConversationForLabelWatcher(mailSession, labelId, any()) } returns mockWatcher

        // When
        rustConversationMessageQuery.observeConversationsByLabel(userId, labelId)

        // Then
        coVerify { rustMailbox.switchToMailbox(userId, labelId) }
    }

    @Test
    fun `emits initial value from watcher when observe conversations watcher is initialised`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSession>()
        val labelId = LocalLabelId(1uL)
        val mockWatcher = mockk<WatchedConversations> {
            coEvery { this@mockk.conversations } returns expectedConversations
        }
        val callbackSlot = slot<LiveQueryCallback>()
        coEvery { rustMailbox.switchToMailbox(userId, labelId) } just Runs
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery {
            createRustConversationForLabelWatcher(mailSession, labelId, capture(callbackSlot))
        } returns mockWatcher

        // When
        rustConversationMessageQuery.observeConversationsByLabel(userId, labelId).test {
            // Then
            assertEquals(expectedConversations, awaitItem())
        }
    }
}
