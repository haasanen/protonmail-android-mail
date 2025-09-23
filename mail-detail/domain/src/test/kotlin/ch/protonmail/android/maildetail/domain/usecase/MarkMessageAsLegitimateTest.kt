package ch.protonmail.android.maildetail.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class MarkMessageAsLegitimateTest {

    private val messageRepository = mockk<MessageRepository>()

    private val markMessageAsLegitimate = MarkMessageAsLegitimate(messageRepository)

    @Test
    fun `returns success when marking as legitimate succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        coEvery { messageRepository.markMessageAsLegitimate(userId, messageId) } returns Unit.right()

        // When
        val actual = markMessageAsLegitimate(userId, messageId)

        // Then
        coVerify { messageRepository.markMessageAsLegitimate(userId, messageId) }
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when init with new empty draft fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val expected = DataError.Local.CryptoError
        coEvery { messageRepository.markMessageAsLegitimate(userId, messageId) } returns expected.left()

        // When
        val actual = markMessageAsLegitimate(userId, messageId)

        // Then
        assertEquals(expected.left(), actual)
    }
}
