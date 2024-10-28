package ch.protonmail.android.mailsession.data.repository

import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.model.SessionError
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import kotlin.test.Test
import kotlin.test.assertEquals

class UserSessionRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val loggingRule = LoggingTestRule()

    private val mailSessionRepository = mockk<MailSessionRepository>()

    private val userSessionRepository = UserSessionRepositoryImpl(
        mailSessionRepository,
        TestScope(mainDispatcherRule.testDispatcher)
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
        coVerify(exactly = 0) { mailSession.userContextFromSession(any()) }
    }

    @Test
    fun `fork session should return session id on success`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedSessionId = "forked-session-id"
        val expectedMailUserSession = mockk<MailUserSession> {
            coEvery { fork() } returns expectedSessionId
        }
        val mailSession = mailSessionWithUserSessionStored(userId, expectedMailUserSession)
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val result = userSessionRepository.forkSession(userId)

        // Then
        assert(result.isRight())
        assertEquals(ForkedSessionId(expectedSessionId), result.getOrNull())
        coVerify { expectedMailUserSession.fork() }
    }

    @Test
    fun `forkSession should return SessionError when session is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mailSessionWithNoUserSessionsStored()
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val result = userSessionRepository.forkSession(userId)

        // Then
        assert(result.isLeft())
        assertEquals(SessionError.Local.Unknown, result.swap().getOrNull())
    }

    @Test
    fun `forkSession should return SessionError when fork operation fails`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val exception = RuntimeException("Fork failed")
        val expectedMailUserSession = mockk<MailUserSession> {
            coEvery { fork() } throws exception
        }
        val mailSession = mailSessionWithUserSessionStored(userId, expectedMailUserSession)
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val result = userSessionRepository.forkSession(userId)

        // Then
        assert(result.isLeft())
        assertEquals(SessionError.Local.Unknown, result.swap().getOrNull())
        coVerify { expectedMailUserSession.fork() }
    }


    private fun mailSessionWithNoUserSessionsStored() = mockk<MailSession> {
        coEvery { getAccount(any()) } returns null
        coEvery { getAccounts() } returns emptyList()
    }

    private fun mailSessionWithUserSessionStored(
        expectedSessionUserId: UserId,
        expectedMailUserSession: MailUserSession
    ) = mockk<MailSession> {
        val storedAccount = mockk<StoredAccount> {
            every { userId() } returns expectedSessionUserId.id
        }
        val storedSession = mockk<StoredSession> {
            every { userId() } returns expectedSessionUserId.id
        }
        coEvery { userContextFromSession(storedSession) } returns expectedMailUserSession
        coEvery { getAccount(any()) } returns storedAccount
        coEvery { getAccounts() } returns listOf(storedAccount)
        coEvery { getSessions(storedAccount) } returns listOf(storedSession)
    }

}
