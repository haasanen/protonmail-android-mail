package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
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

class UpdateCcRecipientsTest {

    private val draftRepository = mockk<DraftRepository>()

    private val updateCcRecipients = UpdateCcRecipients(draftRepository)


    @Test
    fun `saves Cc recipients that were newly added`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipientToAdd = RecipientSample.Bob
        val existingRecipient = RecipientSample.Alice
        val currentRecipients = listOf(existingRecipient)
        coEvery { draftRepository.saveCcRecipient(userId, messageId, recipientToAdd) } returns Unit.right()

        // When
        val actualEither = updateCcRecipients(
            userId,
            messageId,
            currentRecipients,
            listOf(existingRecipient, recipientToAdd)
        )

        // Then
        coVerify { draftRepository.saveCcRecipient(userId, messageId, recipientToAdd) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when saving Cc recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Bob
        val expected = DataError.Local.SaveDraftError.DuplicateRecipient
        coEvery { draftRepository.saveCcRecipient(userId, messageId, recipient) } returns expected.left()
        // When
        val actualEither = updateCcRecipients(userId, messageId, emptyList(), listOf(recipient))

        // Then
        assertEquals(expected.left(), actualEither)
    }

}
