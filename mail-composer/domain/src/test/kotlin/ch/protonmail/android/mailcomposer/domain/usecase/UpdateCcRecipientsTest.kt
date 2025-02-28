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

class UpdateCcRecipientsTest {

    private val draftRepository = mockk<DraftRepository>()

    private val updateCcRecipients = UpdateCcRecipients(draftRepository)

    @Test
    fun `save implementation calls repository save To Recipients`() = runTest {
        // Given
        val recipientToAdd = RecipientSample.Bob
        coEvery { draftRepository.addCcRecipient(recipientToAdd) } returns Unit.right()

        // When
        val actualEither = updateCcRecipients.save(
            recipientToAdd
        )

        // Then
        coVerify { draftRepository.addCcRecipient(recipientToAdd) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `remove implementation calls repository remove To Recipients`() = runTest {
        // Given
        val recipientToRemove = RecipientSample.Bob
        coEvery { draftRepository.removeCcRecipient(recipientToRemove) } returns Unit.right()

        // When
        val actualEither = updateCcRecipients.remove(
            recipientToRemove
        )

        // Then
        coVerify { draftRepository.removeCcRecipient(recipientToRemove) }
        assertEquals(Unit.right(), actualEither)
    }

}
