package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class UpdateToRecipientsTest {

    private val draftRepository = mockk<DraftRepository>()

    private val updateToRecipients = UpdateToRecipients(draftRepository)

    @Test
    fun `save implementation calls repository save To Recipients`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipientToAdd = RecipientSample.Bob
        coEvery { draftRepository.saveToRecipient(userId, messageId, recipientToAdd) } returns Unit.right()

        // When
        val actualEither = updateToRecipients.save(
            userId,
            messageId,
            recipientToAdd
        )

        // Then
        coVerify { draftRepository.saveToRecipient(userId, messageId, recipientToAdd) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `remove implementation calls repository remove To Recipients`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipientToRemove = RecipientSample.Bob
        coEvery { draftRepository.removeToRecipient(userId, messageId, recipientToRemove) } returns Unit.right()

        // When
        val actualEither = updateToRecipients.remove(
            userId,
            messageId,
            recipientToRemove
        )

        // Then
        coVerify { draftRepository.removeToRecipient(userId, messageId, recipientToRemove) }
        assertEquals(Unit.right(), actualEither)
    }

}
