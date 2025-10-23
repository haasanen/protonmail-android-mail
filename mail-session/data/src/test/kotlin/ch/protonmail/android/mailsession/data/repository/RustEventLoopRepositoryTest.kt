package ch.protonmail.android.mailsession.data.repository

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RustEventLoopRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userSessionRepository = mockk<UserSessionRepository>()

    private val eventLoopRepository = RustEventLoopRepository(
        userSessionRepository
    )

    @Test
    fun `triggers event loop for the given user's session`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val mailSession = mockk<MailUserSessionWrapper>(relaxUnitFun = true) {
            coEvery { this@mockk.pollEvents() } returns Unit.right()
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession

        // When
        eventLoopRepository.trigger(userId)

        // Then
        coVerify { mailSession.pollEvents() }
    }

    @Test
    fun `triggers event loop and waits until it is completed`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val mailSession = mockk<MailUserSessionWrapper>(relaxUnitFun = true) {
            coEvery { this@mockk.pollEventsAndWait() } returns Unit.right()
        }
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession

        // When
        eventLoopRepository.triggerAndWait(userId)

        // Then
        coVerify(exactly = 1) { mailSession.pollEventsAndWait() }
    }

}
