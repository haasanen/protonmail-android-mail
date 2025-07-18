package ch.protonmail.android.mailmessage.data.local

import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.data.model.PaginatorParams
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessagesPaginator
import ch.protonmail.android.mailmessage.data.usecase.CreateRustSearchPaginator
import ch.protonmail.android.mailmessage.data.wrapper.MailboxMessagePaginatorWrapper
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.mailpagination.domain.model.PageToLoad
import ch.protonmail.android.mailpagination.domain.model.ReadStatus
import ch.protonmail.android.mailsession.data.mapper.toLocalUserId
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.message.rust.LocalMessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.MessageScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.MessageScrollerUpdate
import kotlin.test.Test

class MessagePaginatorManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expectedMessages = listOf(
        LocalMessageTestData.AugWeatherForecast,
        LocalMessageTestData.SepWeatherForecast,
        LocalMessageTestData.OctWeatherForecast
    )

    private val messagePaginator: MailboxMessagePaginatorWrapper = mockk()
    private val createRustMessagesPaginator: CreateRustMessagesPaginator = mockk()
    private val createRustSearchPaginator: CreateRustSearchPaginator = mockk()
    private val userSessionRepository: UserSessionRepository = mockk()

    private val messagePaginatorManager = MessagePaginatorManager(
        userSessionRepository,
        createRustMessagesPaginator,
        createRustSearchPaginator
    )
    private val onNewPaginatorCallback: suspend () -> Unit = {}

    @Test
    fun `initialises paginator instance with given input label`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val localLabelId = LocalLabelId(1u)
        val pageKey = PageKey.DefaultPageKey(labelId = localLabelId.toLabelId())
        val userSession = mockk<MailUserSessionWrapper>()
        val callback = mockk<MessageScrollerLiveQueryCallback>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSession
        coEvery { messagePaginator.nextPage() } answers {
            launch {
                delay(100) // Simulate callback delay compared to nextPage invocation
                callback.onUpdate(MessageScrollerUpdate.Append(expectedMessages))
            }
            Unit.right()
        }
        coEvery {
            createRustMessagesPaginator.invoke(userSession, localLabelId, false, callback)
        } returns messagePaginator.right()

        // When
        val actual = messagePaginatorManager.getOrCreatePaginator(
            userId, pageKey, callback, onNewPaginatorCallback
        )

        // Then
        assertNotNull(actual)
    }

    @Test
    fun `initialised paginator only once for any given label`() = runTest {
        // Given
        val firstPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKeyFirstPage = PageKey.DefaultPageKey(
            labelId = labelId,
            pageToLoad = PageToLoad.First
        )
        val pageKeyNextPage = PageKey.DefaultPageKey(
            labelId = labelId,
            pageToLoad = PageToLoad.Next
        )
        val session = mockk<MailUserSessionWrapper>()
        val callback = mockk<MessageScrollerLiveQueryCallback>()
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { messagePaginator.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callback.onUpdate(MessageScrollerUpdate.Append(firstPage))
                }
                Unit.right()
            }
            coEvery { this@mockk.destroy() } just Runs
            coEvery { this@mockk.params } returns
                PaginatorParams(userId.toLocalUserId(), labelId.toLocalLabelId(), false)
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, callback)
        } returns paginator.right()

        // When
        messagePaginatorManager.getOrCreatePaginator(
            userId, pageKeyFirstPage, callback, onNewPaginatorCallback
        )
        messagePaginatorManager.getOrCreatePaginator(
            userId, pageKeyNextPage, callback, onNewPaginatorCallback
        )

        // Then
        coVerify(exactly = 1) { createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, any()) }
    }

    @Test
    fun `re initialises paginator when labelId changes`() = runTest {
        // Given
        val firstPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val newLabelId = SystemLabelId.Archive.labelId
        val pageKey = PageKey.DefaultPageKey(labelId = labelId)
        val newPageKey = pageKey.copy(newLabelId)
        val session = mockk<MailUserSessionWrapper>()
        val callback = mockk<MessageScrollerLiveQueryCallback>()
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { messagePaginator.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callback.onUpdate(MessageScrollerUpdate.Append(firstPage))
                }
                Unit.right()
            }
            coEvery { this@mockk.destroy() } just Runs
            coEvery { this@mockk.params } returns PaginatorParams(
                userId = userId.toLocalUserId(), labelId = labelId.toLocalLabelId(), unread = false
            )
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, callback)
        } returns paginator.right()
        coEvery {
            createRustMessagesPaginator(session, newLabelId.toLocalLabelId(), false, callback)
        } returns paginator.right()

        // When
        messagePaginatorManager.getOrCreatePaginator(userId, pageKey, callback, onNewPaginatorCallback)
        messagePaginatorManager.getOrCreatePaginator(userId, newPageKey, callback, onNewPaginatorCallback)

        // Then
        coVerify(exactly = 1) { createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, any()) }

        coVerify { paginator.destroy() }

        coVerify(exactly = 1) { createRustMessagesPaginator(session, newLabelId.toLocalLabelId(), false, any()) }
    }

    @Test
    fun `re initialises paginator when first page is requested again`() = runTest {
        // Given
        val firstPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val pageKeyFirstPage = PageKey.DefaultPageKey(
            labelId = labelId,
            pageToLoad = PageToLoad.First
        )
        val pageKeyNextPage = PageKey.DefaultPageKey(
            labelId = labelId,
            pageToLoad = PageToLoad.Next
        )
        val session = mockk<MailUserSessionWrapper>()
        val callback = mockk<MessageScrollerLiveQueryCallback>()
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { messagePaginator.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callback.onUpdate(MessageScrollerUpdate.Append(firstPage))
                }
                Unit.right()
            }
            coEvery { this@mockk.destroy() } just Runs
            coEvery { this@mockk.params } returns PaginatorParams(
                userId = userId.toLocalUserId(), labelId = labelId.toLocalLabelId(), unread = false
            )
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, callback)
        } returns paginator.right()

        // When
        messagePaginatorManager.getOrCreatePaginator(
            userId,
            pageKeyFirstPage,
            callback,
            onNewPaginatorCallback
        )
        messagePaginatorManager.getOrCreatePaginator(
            userId,
            pageKeyNextPage,
            callback,
            onNewPaginatorCallback
        )
        messagePaginatorManager.getOrCreatePaginator(
            userId,
            pageKeyFirstPage,
            callback,
            onNewPaginatorCallback
        )

        // Then
        coVerify(exactly = 2) { createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, any()) }
    }

    @Test
    fun `re initialises paginator when read status changes`() = runTest {
        // Given
        val firstPage = listOf(LocalMessageTestData.AugWeatherForecast)
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Inbox.labelId
        val readStatus = ReadStatus.All
        val pageKey = PageKey.DefaultPageKey(labelId = labelId, readStatus = readStatus)
        val newPageKey = pageKey.copy(readStatus = ReadStatus.Unread)
        val session = mockk<MailUserSessionWrapper>()
        val callback = mockk<MessageScrollerLiveQueryCallback>()
        val paginator = mockk<MailboxMessagePaginatorWrapper> {
            coEvery { messagePaginator.nextPage() } answers {
                launch {
                    delay(100) // Simulate callback delay compared to nextPage invocation
                    callback.onUpdate(MessageScrollerUpdate.Append(firstPage))
                }
                Unit.right()
            }
            coEvery { this@mockk.destroy() } just Runs
            coEvery { this@mockk.params } returns PaginatorParams(userId.toLocalUserId(), labelId.toLocalLabelId())
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery {
            createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, callback)
        } returns paginator.right()
        coEvery {
            createRustMessagesPaginator(session, labelId.toLocalLabelId(), true, callback)
        } returns paginator.right()

        // When
        messagePaginatorManager.getOrCreatePaginator(userId, pageKey, callback, onNewPaginatorCallback)
        messagePaginatorManager.getOrCreatePaginator(userId, newPageKey, callback, onNewPaginatorCallback)

        // Then
        coVerify(exactly = 1) { createRustMessagesPaginator(session, labelId.toLocalLabelId(), false, any()) }

        coVerify { paginator.destroy() }

        coVerify(exactly = 1) { createRustMessagesPaginator(session, labelId.toLocalLabelId(), true, any()) }
    }
}
