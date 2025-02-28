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

class UpdateBccRecipientsTest {

    private val draftRepository = mockk<DraftRepository>()

    private val updateBccRecipients = UpdateBccRecipients(draftRepository)

    @Test
    fun `save implementation calls repository save To Recipients`() = runTest {
        // Given
        val recipientToAdd = RecipientSample.Bob
        coEvery { draftRepository.addBccRecipient(recipientToAdd) } returns Unit.right()

        // When
        val actualEither = updateBccRecipients.save(
            recipientToAdd
        )

        // Then
        coVerify { draftRepository.addBccRecipient(recipientToAdd) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `remove implementation calls repository remove To Recipients`() = runTest {
        // Given
        val recipientToRemove = RecipientSample.Bob
        coEvery { draftRepository.removeBccRecipient(recipientToRemove) } returns Unit.right()

        // When
        val actualEither = updateBccRecipients.remove(
            recipientToRemove
        )

        // Then
        coVerify { draftRepository.removeBccRecipient(recipientToRemove) }
        assertEquals(Unit.right(), actualEither)
    }

}
