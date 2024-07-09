package ch.protonmail.android.mailsession.data.repository

import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.SessionCallback
import uniffi.proton_mail_uniffi.SessionError
import uniffi.proton_mail_uniffi.StoredSession
import kotlin.test.Test
import kotlin.test.assertEquals

class UserSessionRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val loggingRule = LoggingTestRule()

    private val mailSessionRepository = mockk<MailSessionRepository>()
    private val scope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val userSessionRepository = UserSessionRepositoryImpl(
        mailSessionRepository,
        scope
    )

    @Test
    fun `emits user session when existing`() = runTest {
        // Given
        val expectedMailUserSession = mockk<MailUserSession>()
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        userSessionRepository.observeCurrentUserSession().test {
            // Then
            assertEquals(expectedMailUserSession, awaitItem())
        }
    }

    @Test
    fun `emits null when no session exists`() = runTest {
        // Given
        val mailSession = mailSessionWithNoUserSessionsStored()
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        userSessionRepository.observeCurrentUserSession().test {
            // Then
            assertEquals(null, awaitItem())
        }
        verify(exactly = 0) { mailSession.userContextFromSession(any(), any()) }
    }

    private fun mailSessionWithNoUserSessionsStored() = mockk<MailSession> {
        every { storedSessions() } returns emptyList()
    }

    @Test
    fun `emits null when session is deleted`() = runTest {
        // Given
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        userSessionRepository.observeCurrentUserSession().test {
            assertEquals(expectedMailUserSession, awaitItem())
            // When
            sessionCallbackCaptor.captured.onSessionDeleted()
            // Then
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `logs error and emits null when session errors`() = runTest {
        // Given
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        val expectedError = SessionError.CRYPTO
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        userSessionRepository.observeCurrentUserSession().test {
            // Given there's an existing session
            assertEquals(expectedMailUserSession, awaitItem())
            // When
            sessionCallbackCaptor.captured.onError(expectedError)
            // Then
            assertEquals(null, awaitItem())
            loggingRule.assertErrorLogged("rust-session: error: ${expectedError.name}")
        }
    }

    @Test
    fun `logs warning and emits null when session refresh fails`() = runTest {
        // Given
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        val expectedError = SessionError.HTTP
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        userSessionRepository.observeCurrentUserSession().test {
            // Given there's an existing session
            assertEquals(expectedMailUserSession, awaitItem())
            // When
            sessionCallbackCaptor.captured.onRefreshFailed(expectedError)
            // Then
            assertEquals(null, awaitItem())
            loggingRule.assertWarningLogged("rust-session: refresh failed: ${expectedError.name}")
        }
    }

    @Test
    fun `no op when session is refreshed successfully`() = runTest {
        // Given
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        userSessionRepository.observeCurrentUserSession().test {
            // When
            sessionCallbackCaptor.captured.onSessionRefresh()
            // Then the existing session is still available on the flow
            assertEquals(expectedMailUserSession, awaitItem())
        }
    }

    private fun mailSessionWithUserSessionStored(
        expectedMailUserSession: MailUserSession,
        callbackCaptor: CapturingSlot<SessionCallback> = slot()
    ) = mockk<MailSession> {
        val storedSession = mockk<StoredSession>()
        every { storedSessions() } returns listOf(storedSession)
        every {
            userContextFromSession(storedSession, capture(callbackCaptor))
        } returns expectedMailUserSession
    }

}
