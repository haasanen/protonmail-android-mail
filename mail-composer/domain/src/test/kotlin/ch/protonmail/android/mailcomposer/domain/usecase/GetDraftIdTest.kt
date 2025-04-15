package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetDraftIdTest {

    private val draftRepository = mockk<DraftRepository>()

    private val getDraftId = GetDraftId(draftRepository)

    @Test
    fun `get message id`() = runTest {
        // Given
        val messageId = MessageIdSample.PlainTextMessage
        givenGetMessageIdSucceeds(messageId)

        // When
        val actualEither = getDraftId()

        // Then
        coVerify { draftRepository.getMessageId() }
        assertEquals(messageId.right(), actualEither)
    }

    @Test
    fun `returns error when get message id fails`() = runTest {
        // Given
        val expected = DataError.Local.NoDraftId
        givenGetMessageIdFails(expected)

        // When
        val actualEither = getDraftId()

        // Then
        assertEquals(expected.left(), actualEither)
    }

    private fun givenGetMessageIdSucceeds(messageId: MessageId) {
        coEvery { draftRepository.getMessageId() } returns messageId.right()
    }

    private fun givenGetMessageIdFails(expected: DataError) {
        coEvery { draftRepository.getMessageId() } returns expected.left()
    }
}
