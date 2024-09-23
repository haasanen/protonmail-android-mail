package ch.protonmail.android.mailsession.data.repository

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.MailUserSession

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
        val mailSession = mockk<MailUserSession>(relaxUnitFun = true)
        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession

        // When
        eventLoopRepository.trigger(userId)

        // Then
        coVerify { mailSession.pollEvents() }
    }
}
