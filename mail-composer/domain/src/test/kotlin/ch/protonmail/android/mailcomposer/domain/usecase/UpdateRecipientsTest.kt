package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
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
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipientToAdd = RecipientSample.Bob
        val existingRecipient = RecipientSample.Alice
        val currentRecipients = listOf(existingRecipient)
        coEvery { updateRecipients.save(userId, messageId, recipientToAdd) } returns Unit.right()

        // When
        val actualEither = updateRecipients(
            userId,
            messageId,
            currentRecipients,
            listOf(existingRecipient, recipientToAdd)
        )

        // Then
        coVerify { updateRecipients.save(userId, messageId, recipientToAdd) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when saving To recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Bob
        val expected = DataError.Local.SaveDraftError.DuplicateRecipient
        coEvery { updateRecipients.save(userId, messageId, recipient) } returns expected.left()
        // When
        val actualEither = updateRecipients(userId, messageId, emptyList(), listOf(recipient))

        // Then
        assertEquals(expected.left(), actualEither)
    }

    @Test
    fun `removes To recipients that were removed`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipientToRemove = RecipientSample.Bob
        val unchangedRecipient = RecipientSample.Alice
        coEvery { updateRecipients.remove(userId, messageId, recipientToRemove) } returns Unit.right()

        // When
        val actualEither = updateRecipients(
            userId = userId,
            messageId = messageId,
            currentRecipients = listOf(recipientToRemove, unchangedRecipient),
            updatedRecipients = listOf(unchangedRecipient)
        )

        // Then
        coVerify { updateRecipients.remove(userId, messageId, recipientToRemove) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when removing To recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Bob
        val expected = DataError.Local.SaveDraftError.DuplicateRecipient
        coEvery { updateRecipients.remove(userId, messageId, recipient) } returns expected.left()
        // When
        val actualEither = updateRecipients(userId, messageId, listOf(recipient), emptyList())

        // Then
        assertEquals(expected.left(), actualEither)
    }

}
