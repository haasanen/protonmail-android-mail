package ch.protonmail.android.mailconversation.data.local

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalConversation
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.data.local.RustConversationsQueryImpl.Companion.NONE_FOLLOWUP_GRACE_MS
import ch.protonmail.android.mailconversation.data.usecase.CreateRustConversationPaginator
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.maillabel.data.local.RustMailboxFactory
import ch.protonmail.android.maillabel.data.wrapper.MailboxWrapper
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailpagination.domain.model.PageInvalidationEvent
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailpagination.domain.model.ShowSpamTrash
import ch.protonmail.android.mailpagination.domain.repository.PageInvalidationRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.ConversationScrollerListUpdate
import uniffi.proton_mail_uniffi.ConversationScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.ConversationScrollerUpdate
import kotlin.test.assertEquals

class RustConversationsQueryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createRustConversationPaginator = mockk<CreateRustConversationPaginator>()
    private val rustMailboxFactory = mockk<RustMailboxFactory>()
    private val invalidationRepository = mockk<PageInvalidationRepository>()


    private val rustConversationsQuery = RustConversationsQueryImpl(
        rustMailboxFactory,
        createRustConversationPaginator,
        CoroutineScope(mainDispatcherRule.testDispatcher),
        invalidationRepository
    )

    @Test
    fun `returns IllegalStateError when mailbox is null`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val pageKey = PageKey.DefaultPageKey()
        coEvery { rustMailboxFactory.create(userId) } returns DataError.Local.Unknown.left()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(PaginationError.Other(DataError.Local.IllegalStateError).left(), actual)
        verify { createRustConversationPaginator wasNot Called }
    }

    @Test
    fun `returns first page when called with PageToLoad First and rust emits items in the callback`() = runTest {
        // Given
        val expectedConversations = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(expectedConversations)
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }
        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedConversations.right(), actual)
    }

    @Test
    fun `returns next page when called with PageToLoad Next`() = runTest {
        // Given
        val expectedConversations = listOf(LocalConversationTestData.OctConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.Next)
        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(expectedConversations)
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }
        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                capture(callbackSlot)
            )
        } returns paginator.right()


        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedConversations.right(), actual)
    }

    @Test
    fun `returns all pages when called with PageToLoad All`() = runTest {
        // Given
        val expectedConversations = listOf(LocalConversationTestData.spamConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.All)
        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            launch {
                delay(100) // Simulate callback delay compared to nextPage invocation
                callbackSlot.captured.onUpdate(
                    ConversationScrollerUpdate.List(
                        ConversationScrollerListUpdate.ReplaceFrom(
                            0uL,
                            expectedConversations
                        )
                    )
                )
            }
            coEvery { this@mockk.reload() } returns Unit.right()
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }
        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedConversations.right(), actual)
    }

    @Test
    fun `initialises paginator only once for any given label`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val nextPage = listOf(LocalConversationTestData.OctConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val nextPageKey = pageKey.copy(pageToLoad = PageToLoad.Next)
        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        var methodCallCounter = 0
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } answers {
                methodCallCounter++
                val expectedPage = when {
                    methodCallCounter == 1 -> firstPage
                    else -> nextPage
                }
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(
                                expectedPage
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }
        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, nextPageKey)

        // Then
        coVerify(exactly = 1) {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = any()
            )
        }
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
        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(
                                firstPage
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.disconnect() } just Runs
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }
        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, newPageKey)

        // Then
        coVerify(exactly = 2) {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = any()
            )
        }

        coVerify { paginator.disconnect() }
    }

    @Test
    fun `updates paginator without re-initialising it when read status changes`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val readStatus = ReadStatus.All
        val newReadStatus = ReadStatus.Unread
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, readStatus = readStatus)
        val newPageKey = pageKey.copy(readStatus = newReadStatus, pageToLoad = PageToLoad.Next)
        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(
                                firstPage
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.disconnect() } just Runs
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.filterUnread(true) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }
        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, newPageKey)

        // Then
        coVerify(exactly = 1) {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = any()
            )
        }
    }

    @Test
    fun `updates paginator without re-initialising when show trash and spam status changes`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val showSpamTrash = ShowSpamTrash.Hide
        val newShowSpamTrash = ShowSpamTrash.Show
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, showSpamTrash = showSpamTrash)
        val newPageKey = pageKey.copy(showSpamTrash = newShowSpamTrash, pageToLoad = PageToLoad.Next)
        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { this@mockk.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(
                                firstPage
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.disconnect() } just Runs
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(true) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }
        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        rustConversationsQuery.getConversations(userId, newPageKey)

        // Then
        coVerify(exactly = 1) {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = any()
            )
        }
    }

    @Test
    fun `submits invalidation when onUpdate callback is fired with ReplaceBefore event`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)

        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(
                                firstPage
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }

        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        coEvery {
            invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated)
        } just Runs

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        callbackSlot.captured.onUpdate(
            ConversationScrollerUpdate.List(ConversationScrollerListUpdate.ReplaceBefore(2uL, firstPage))
        )

        // Then
        coVerify { invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated) }
    }

    @Test
    fun `submits invalidation when onUpdate is fired with ReplaceFrom event with index greater than 0`() = runTest {
        // Given
        val firstPage = listOf(LocalConversationTestData.AugConversation)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)

        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(
                                firstPage
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }

        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        coEvery {
            invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated)
        } just Runs

        // When
        rustConversationsQuery.getConversations(userId, pageKey)
        callbackSlot.captured.onUpdate(
            ConversationScrollerUpdate.List(
                ConversationScrollerListUpdate.ReplaceFrom(
                    2uL,
                    firstPage
                )
            )
        )

        // Then
        coVerify { invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated) }
    }

    @Test
    fun `Append None followed by ReplaceBefore(0) within grace returns follow-up items`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.First)

        val expectedFollowUp = listOf(LocalConversationTestData.OctConversation)

        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()

        // Emit immediate None, then within the grace window emit ReplaceBefore(0, items)
        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { nextPage() } answers {
                CoroutineScope(mainDispatcherRule.testDispatcher).launch {
                    callbackSlot.captured.onUpdate(ConversationScrollerUpdate.List(ConversationScrollerListUpdate.None))
                    delay(NONE_FOLLOWUP_GRACE_MS - 100)
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.ReplaceBefore(
                                0uL,
                                expectedFollowUp
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }

        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(expectedFollowUp.right(), actual)
    }

    @Test
    fun `Append None then follow-up arrives after grace returns empty`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.First)

        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()
        coEvery { invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated) } just Runs

        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { nextPage() } answers {
                CoroutineScope(mainDispatcherRule.testDispatcher).launch {
                    callbackSlot.captured.onUpdate(ConversationScrollerUpdate.List(ConversationScrollerListUpdate.None))
                    delay(NONE_FOLLOWUP_GRACE_MS + 100)
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.ReplaceBefore(
                                0uL,
                                listOf(LocalConversationTestData.OctConversation)
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }

        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(emptyList<LocalConversation>().right(), actual)
    }

    @Test
    fun `Append None then late Append after grace period triggers invalidation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, pageToLoad = PageToLoad.First)

        val mailbox = mockk<MailboxWrapper>()
        val callbackSlot = slot<ConversationScrollerLiveQueryCallback>()

        coEvery { invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated) } just Runs

        val lateItems = listOf(LocalConversationTestData.OctConversation)

        val paginator = mockk<ConversationPaginatorWrapper> {
            coEvery { nextPage() } answers {
                CoroutineScope(mainDispatcherRule.testDispatcher).launch {
                    callbackSlot.captured.onUpdate(ConversationScrollerUpdate.List(ConversationScrollerListUpdate.None))
                    delay(NONE_FOLLOWUP_GRACE_MS + 100)
                    callbackSlot.captured.onUpdate(
                        ConversationScrollerUpdate.List(
                            ConversationScrollerListUpdate.Append(
                                lateItems
                            )
                        )
                    )
                }
                Unit.right()
            }
            coEvery { this@mockk.filterUnread(false) } just Runs
            coEvery { this@mockk.showSpamAndTrash(false) } just Runs
        }

        coEvery { rustMailboxFactory.create(userId) } returns mailbox.right()
        coEvery {
            createRustConversationPaginator(
                mailbox = mailbox,
                callback = capture(callbackSlot)
            )
        } returns paginator.right()

        // When
        val actual = rustConversationsQuery.getConversations(userId, pageKey)

        // Then
        assertEquals(emptyList<LocalConversation>().right(), actual)

        // Late Append causes invalidation
        testScheduler.advanceUntilIdle()
        coVerify(exactly = 1) { invalidationRepository.submit(PageInvalidationEvent.ConversationsInvalidated) }
    }
}
