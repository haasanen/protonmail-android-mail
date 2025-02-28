package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
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
        val recipientToAdd = RecipientSample.Bob
        coEvery { draftRepository.addToRecipient(recipientToAdd) } returns Unit.right()

        // When
        val actualEither = updateToRecipients.save(
            recipientToAdd
        )

        // Then
        coVerify { draftRepository.addToRecipient(recipientToAdd) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `remove implementation calls repository remove To Recipients`() = runTest {
        // Given
        val recipientToRemove = RecipientSample.Bob
        coEvery { draftRepository.removeToRecipient(recipientToRemove) } returns Unit.right()

        // When
        val actualEither = updateToRecipients.remove(
            recipientToRemove
        )

        // Then
        coVerify { draftRepository.removeToRecipient(recipientToRemove) }
        assertEquals(Unit.right(), actualEither)
    }

}
