package ch.protonmail.android.maildetail.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsubscribeFromNewsletterTest {

    private val messageBodyRepository = mockk<MessageBodyRepository>()

    private val unsubscribeFromNewsletter = UnsubscribeFromNewsletter(messageBodyRepository)

    @Test
    fun `use case should call correct repository method`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = MessageIdSample.PlainTextMessage
        coEvery { messageBodyRepository.unsubscribeFromNewsletter(userId, messageId) } returns Unit.right()

        // When
        val actual = unsubscribeFromNewsletter(userId, messageId)

        // Then
        assertEquals(Unit.right(), actual)
        coVerify { messageBodyRepository.unsubscribeFromNewsletter(userId, messageId) }
    }
}
