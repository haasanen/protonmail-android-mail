package ch.protonmail.android.mailsession.domain.usecase

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailsession.domain.repository.EventLoopRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class RustEventManagerStarterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val eventLoopRepository = mockk<EventLoopRepository>(relaxUnitFun = true)
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val rustEventManagerStarter = RustEventManagerStarter(
        userSessionRepository,
        eventLoopRepository,
        testCoroutineScope
    )

    @Test
    fun `triggers event loop for the primary user`() {
        // Given
        val userId = UserIdSample.Primary
        coEvery { userSessionRepository.observeCurrentUserId() } returns flowOf(userId)

        // When
        rustEventManagerStarter.start()

        // Then
        coVerify { eventLoopRepository.trigger(userId) }
    }
}
