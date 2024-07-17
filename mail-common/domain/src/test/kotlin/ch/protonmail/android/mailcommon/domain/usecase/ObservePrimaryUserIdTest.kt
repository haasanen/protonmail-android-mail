package ch.protonmail.android.mailcommon.domain.usecase

import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import org.junit.Test
import kotlin.test.assertEquals

class ObservePrimaryUserIdTest {

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val accountManager = mockk<AccountManager>()

    private val observePrimaryUserId = ObservePrimaryUserId(accountManager, true, userSessionRepository)

    @Test
    fun `observes current user id from user session repository`() = runTest {
        // Given
        val expected = UserIdTestData.userId
        every { userSessionRepository.observeCurrentUserId() } returns flowOf(expected)

        // When
        observePrimaryUserId().test {

            // Then
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }
}
