package ch.protonmail.android.maildetail.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class UnblockSenderTest {

    private val messageRepository = mockk<MessageRepository>()

    private val unblockSender = UnblockSender(messageRepository)

    @Test
    fun `returns success when unblocking sender succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val email = "abc@pm.me"
        coEvery { messageRepository.unblockSender(userId, email) } returns Unit.right()

        // When
        val actual = unblockSender(userId, email)

        // Then
        coVerify { messageRepository.unblockSender(userId, email) }
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when unblocking sender fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val email = "abc@pm.me"
        val expected = DataError.Local.Unknown
        coEvery { messageRepository.unblockSender(userId, email) } returns expected.left()

        // When
        val actual = unblockSender(userId, email)

        // Then
        assertEquals(expected.left(), actual)
    }
}
