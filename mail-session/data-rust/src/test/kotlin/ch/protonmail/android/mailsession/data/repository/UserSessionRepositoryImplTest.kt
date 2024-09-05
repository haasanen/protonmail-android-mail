package ch.protonmail.android.mailsession.data.repository

import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.StoredSession
import kotlin.test.Test
import kotlin.test.assertEquals

class UserSessionRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val loggingRule = LoggingTestRule()

    private val mailSessionRepository = mockk<MailSessionRepository>()

    private val userSessionRepository = UserSessionRepositoryImpl(mailSessionRepository)

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
    fun `ensures session is initialized only once with concurrent access`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSession>()
        val mailSession = mailSessionWithUserSessionStored(userId, expectedMailUserSession)

        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        coEvery { mailSessionRepository.getMailSession() } coAnswers {
            delay(100)
            mailSession
        }

        // When
        coroutineScope {
            val deferredResults = (1..10).map {
                async {
                    userSessionRepository.getUserSession(userId)
                }
            }
            deferredResults.awaitAll()
        }

        // Then
        // Ensure that the session was initialized only once
        coVerify(exactly = 1) { mailSessionRepository.getMailSession() }
        assertEquals(expectedMailUserSession, userSessionRepository.getUserSession(userId))
    }


    private fun mailSessionWithNoUserSessionsStored() = mockk<MailSession> {
        coEvery { storedSessions() } returns emptyList()
    }

    private fun mailSessionWithUserSessionStored(
        expectedSessionUserId: UserId,
        expectedMailUserSession: MailUserSession
    ) = mockk<MailSession> {
        val storedSession = mockk<StoredSession> {
            every { userId() } returns expectedSessionUserId.id
        }
        coEvery { storedSessions() } returns listOf(storedSession)
        coEvery { userContextFromSession(storedSession) } returns expectedMailUserSession
    }

}
