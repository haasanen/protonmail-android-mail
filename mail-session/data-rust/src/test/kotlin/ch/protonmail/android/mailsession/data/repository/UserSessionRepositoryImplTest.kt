package ch.protonmail.android.mailsession.data.repository

import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.repository.MailSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.SessionCallback
import uniffi.proton_mail_uniffi.StoredSession
import kotlin.test.Test
import kotlin.test.assertEquals

class UserSessionRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mailSessionRepository = mockk<MailSessionRepository>()

    private val userSessionRepository = UserSessionRepositoryImpl(
        mailSessionRepository
    )

    @Test
    fun `emits user session when existing`() = runTest {
        // Given
        val expectedMailUserSession = mockk<MailUserSession>()
        val mailSession = mockk<MailSession> {
            val storedSession = mockk<StoredSession>()
            every { storedSessions() } returns listOf(storedSession)
            every {
                userContextFromSession(storedSession, any<SessionCallback>())
            } returns expectedMailUserSession
        }
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
        val mailSession = mockk<MailSession> {
            every { storedSessions() } returns emptyList()
        }
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        userSessionRepository.observeCurrentUserSession().test {
            // Then
            assertEquals(null, awaitItem())
        }
        verify(exactly = 0) { mailSession.userContextFromSession(any(), any()) }
    }
}
