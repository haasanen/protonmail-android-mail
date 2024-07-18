package ch.protonmail.android.mailsession.data.repository

import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
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
    fun `initializes session from repository and returns it when not already active`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSession>()
        val mailSession = mailSessionWithUserSessionStored(userId, expectedMailUserSession)
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val actual = userSessionRepository.getUserSession(userId)
        // Then
        assertEquals(expectedMailUserSession, actual)
    }

    @Test
    fun `returns active session without re initialising it`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSession>()
        val mailSession = mailSessionWithUserSessionStored(userId, expectedMailUserSession)
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val actual = userSessionRepository.getUserSession(userId)
        // Then
        assertEquals(expectedMailUserSession, actual)
        // When
        val newValue = userSessionRepository.getUserSession(userId)
        assertEquals(expectedMailUserSession, actual)
        coVerify(exactly = 1) { mailSessionRepository.getMailSession() }
    }

    @Test
    fun `emits null when no session exists`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mailSessionWithNoUserSessionsStored()
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val actual = userSessionRepository.getUserSession(userId)
        // Then
        assertEquals(null, actual)
        verify(exactly = 0) { mailSession.userContextFromSession(any(), any()) }
    }

    @Test
    fun `removes active session when session is deleted`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            userId,
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val actual = userSessionRepository.getUserSession(userId)
        // Then
        assertEquals(expectedMailUserSession, actual)
        // When
        sessionCallbackCaptor.captured.onSessionDeleted()
        val newValue = userSessionRepository.getUserSession(userId)
        // Then
        assertEquals(null, newValue)
    }

    @Test
    fun `logs error and emits null when session errors`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            userId,
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        val expectedError = SessionError.CRYPTO
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        val actual = userSessionRepository.getUserSession(userId)
        // Given there's an existing session
        assertEquals(expectedMailUserSession, actual)
        // When
        sessionCallbackCaptor.captured.onError(expectedError)
        val newValue = userSessionRepository.getUserSession(userId)
        // Then
        assertEquals(null, newValue)
    }

    @Test
    fun `logs warning and removes active session when session refresh fails`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            userId,
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        val expectedError = SessionError.HTTP
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        val actual = userSessionRepository.getUserSession(userId)
        // Given there's an existing session
        assertEquals(expectedMailUserSession, actual)
        // When
        sessionCallbackCaptor.captured.onRefreshFailed(expectedError)
        val newValue = userSessionRepository.getUserSession(userId)
        // Then
        assertEquals(null, newValue)
    }

    @Test
    fun `no op when session is refreshed successfully`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSession>()
        val sessionCallbackCaptor = slot<SessionCallback>()
        val mailSession = mailSessionWithUserSessionStored(
            userId,
            expectedMailUserSession,
            sessionCallbackCaptor
        )
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        val actual = userSessionRepository.getUserSession(userId)
        // When
        sessionCallbackCaptor.captured.onSessionRefresh()
        // Then the existing session is still available on the flow
        assertEquals(expectedMailUserSession, actual)
    }

    private fun mailSessionWithNoUserSessionsStored() = mockk<MailSession> {
        every { storedSessions() } returns emptyList()
    }

    private fun mailSessionWithUserSessionStored(
        expectedSessionUserId: UserId,
        expectedMailUserSession: MailUserSession,
        callbackCaptor: CapturingSlot<SessionCallback> = slot()
    ) = mockk<MailSession> {
        val storedSession = mockk<StoredSession> {
            every { userId() } returns expectedSessionUserId.id
        }
        every { storedSessions() } returns listOf(storedSession)
        every {
            userContextFromSession(storedSession, capture(callbackCaptor))
        } returns expectedMailUserSession
    }

}
