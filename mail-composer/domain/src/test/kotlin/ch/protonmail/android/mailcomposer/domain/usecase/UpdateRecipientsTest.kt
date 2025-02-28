package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class UpdateRecipientsTest {

    private val updateRecipients = spyk<UpdateRecipients>()

    @Test
    fun `saves To recipients that were newly added`() = runTest {
        // Given
        val recipientToAdd = RecipientSample.Bob
        val existingRecipient = RecipientSample.Alice
        val currentRecipients = listOf(existingRecipient)
        coEvery { updateRecipients.save(recipientToAdd) } returns Unit.right()

        // When
        val actualEither = updateRecipients(
            currentRecipients,
            listOf(existingRecipient, recipientToAdd)
        )

        // Then
        coVerify { updateRecipients.save(recipientToAdd) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when saving To recipient fails`() = runTest {
        // Given
        val recipient = RecipientSample.Bob
        val expected = DataError.Local.SaveDraftError.DuplicateRecipient
        coEvery { updateRecipients.save(recipient) } returns expected.left()
        // When
        val actualEither = updateRecipients(emptyList(), listOf(recipient))

        // Then
        assertEquals(expected.left(), actualEither)
    }

    @Test
    fun `removes To recipients that were removed`() = runTest {
        // Given
        val recipientToRemove = RecipientSample.Bob
        val unchangedRecipient = RecipientSample.Alice
        coEvery { updateRecipients.remove(recipientToRemove) } returns Unit.right()

        // When
        val actualEither = updateRecipients(
            currentRecipients = listOf(recipientToRemove, unchangedRecipient),
            updatedRecipients = listOf(unchangedRecipient)
        )

        // Then
        coVerify { updateRecipients.remove(recipientToRemove) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when removing To recipient fails`() = runTest {
        // Given
        val recipient = RecipientSample.Bob
        val expected = DataError.Local.SaveDraftError.DuplicateRecipient
        coEvery { updateRecipients.remove(recipient) } returns expected.left()
        // When
        val actualEither = updateRecipients(listOf(recipient), emptyList())

        // Then
        assertEquals(expected.left(), actualEither)
    }

}
