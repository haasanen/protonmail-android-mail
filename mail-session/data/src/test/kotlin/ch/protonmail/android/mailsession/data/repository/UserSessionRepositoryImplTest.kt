package ch.protonmail.android.mailsession.data.repository

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalUser
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.data.user.RustUserDataSource
import ch.protonmail.android.mailsession.data.wrapper.MailSessionWrapper
import ch.protonmail.android.mailsession.domain.model.ForkedSessionId
import ch.protonmail.android.mailsession.domain.model.SessionError
import ch.protonmail.android.mailsession.domain.model.User
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.testdata.user.LocalUserTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import me.proton.android.core.account.domain.usecase.ObserveStoredAccounts
import org.junit.Rule
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import kotlin.test.Test
import kotlin.test.assertEquals

class UserSessionRepositoryImplTest {

    @get:Rule
    val loggingRule = LoggingTestRule()

    private val mailSessionRepository = mockk<MailSessionRepository>()
    private val observeStoredAccounts = mockk<ObserveStoredAccounts>()
    private val rustUserDataSource = mockk<RustUserDataSource>()

    private val userSessionRepository = UserSessionRepositoryImpl(
        mailSessionRepository,
        rustUserDataSource,
        observeStoredAccounts
    )

    @Test
    fun `initializes session from repository and returns it when not already active`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSessionWrapper>()
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
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
        val expectedMailUserSession = mockk<MailUserSessionWrapper>()
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
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
        val expectedMailUserSession = mockk<MailUserSessionWrapper> {
            coEvery { fork() } returns expectedSessionId.right()
        }
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
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
        val expectedMailUserSession = mockk<MailUserSessionWrapper> {
            coEvery { fork() } returns DataError.Local.NoUserSession.left()
        }
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val result = userSessionRepository.forkSession(userId)

        // Then
        assertEquals(SessionError.Local.Unknown.left(), result)
        coVerify { expectedMailUserSession.fork() }
    }

    @Test
    fun `observe user returns the user entity and subsequent updates`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedUser = User(
            userId = userId,
            displayName = "userDisplayName",
            name = "username",
            email = "userEmail",
            services = 0,
            subscribed = 0
        )
        val expectedMailUserSession = mockk<MailUserSessionWrapper>()
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
        val localUser = LocalUserTestData.build(subscribed = 0, services = 0)
        val updatedLocalUser = LocalUserTestData.build(subscribed = 1, services = 1)
        val flow = MutableSharedFlow<Either<DataError, LocalUser>>()
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        every { rustUserDataSource.observeUser(expectedMailUserSession) } returns flow

        // When + Then
        userSessionRepository.observeUser(userId).test {
            flow.emit(localUser.right())
            assertEquals(expectedUser.right(), awaitItem())

            flow.emit(updatedLocalUser.right())
            assertEquals(expectedUser.copy(subscribed = 1, services = 1).right(), awaitItem())
        }
    }

    @Test
    fun `observe user returns error when data can't be fetched`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSessionWrapper>()
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
        val expectedError = DataError.Local.Unknown.left()
        val flow = MutableSharedFlow<Either<DataError, LocalUser>>()
        coEvery { mailSessionRepository.getMailSession() } returns mailSession
        every { rustUserDataSource.observeUser(expectedMailUserSession) } returns flow

        // When + Then
        userSessionRepository.observeUser(userId).test {
            flow.emit(expectedError)
            assertEquals(expectedError, awaitItem())
        }
    }

    @Test
    fun `ensures single session instance is created for a user`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expectedMailUserSession = mockk<MailUserSessionWrapper>()
        val mailSession = mailSessionWithUserSessionStored(expectedMailUserSession)
        coEvery { mailSessionRepository.getMailSession() } returns mailSession

        // When
        val firstSession = userSessionRepository.getUserSession(userId)
        val secondSession = userSessionRepository.getUserSession(userId)

        // Then
        assertEquals(firstSession, secondSession)
        coVerify(exactly = 1) { mailSession.userContextFromSession(any()) }
    }

    private fun mailSessionWithNoUserSessionsStored() = mockk<MailSessionWrapper> {
        coEvery { getAccount(any()) } returns DataError.Local.NoDataCached.left()
        coEvery { getAccounts() } returns emptyList<StoredAccount>().right()
    }

    private fun mailSessionWithUserSessionStored(expectedMailUserSession: MailUserSessionWrapper) =
        mockk<MailSessionWrapper> {
            val storedAccount = mockk<StoredAccount>()
            val storedSession = mockk<StoredSession>()
            coEvery {
                userContextFromSession(
                    storedSession
                )
            } returns expectedMailUserSession.right()
            coEvery { getAccount(any()) } returns storedAccount.right()
            coEvery { getAccounts() } returns listOf(storedAccount).right()
            coEvery { getAccountSessions(storedAccount) } returns listOf(storedSession).right()
        }

}
