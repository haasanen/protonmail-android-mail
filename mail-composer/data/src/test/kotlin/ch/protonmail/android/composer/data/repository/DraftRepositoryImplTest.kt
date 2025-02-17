package ch.protonmail.android.composer.data.repository

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.local.RustDraftDataSource
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample
import ch.protonmail.android.testdata.composer.DraftFieldsTestData
import ch.protonmail.android.testdata.composer.LocalDraftTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DraftRepositoryImplTest {

    private val draftDataSource = mockk<RustDraftDataSource>()

    private val draftRepository = DraftRepositoryImpl(draftDataSource)

    @Test
    fun `returns success when open draft succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val localFields = LocalDraftTestData.BasicLocalDraft
        coEvery { draftDataSource.open(userId, messageId) } returns localFields.right()

        // When
        val actual = draftRepository.openDraft(userId, messageId)

        // Then
        val expected = DraftFieldsTestData.BasicDraftFields
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when open draft fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val expected = DataError.Local.Unknown
        coEvery { draftDataSource.open(userId, messageId) } returns expected.left()

        // When
        val actual = draftRepository.openDraft(userId, messageId)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when create draft succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val localFields = LocalDraftTestData.BasicLocalDraft
        val action = DraftAction.Compose
        coEvery { draftDataSource.create(userId, action) } returns localFields.right()

        // When
        val actual = draftRepository.createDraft(userId, action)

        // Then
        val expected = DraftFieldsTestData.BasicDraftFields
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when create draft fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.Unknown
        val action = DraftAction.Compose
        coEvery { draftDataSource.create(userId, action) } returns expected.left()

        // When
        val actual = draftRepository.createDraft(userId, action)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        coEvery { draftDataSource.save() } returns Unit.right()

        // When
        val actual = draftRepository.save(userId, messageId)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when save draft fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        coEvery { draftDataSource.save() } returns expected.left()

        // When
        val actual = draftRepository.save(userId, messageId)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft subject succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val subject = Subject("test subject")
        coEvery { draftDataSource.saveSubject(subject) } returns Unit.right()

        // When
        val actual = draftRepository.saveSubject(userId, messageId, subject)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when save draft subject fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val subject = Subject("test subject")
        coEvery { draftDataSource.saveSubject(subject) } returns expected.left()

        // When
        val actual = draftRepository.saveSubject(userId, messageId, subject)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft body succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val body = DraftBody("test body")
        coEvery { draftDataSource.saveBody(body) } returns Unit.right()

        // When
        val actual = draftRepository.saveBody(userId, messageId, body)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when save draft body fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val body = DraftBody("test body")
        coEvery { draftDataSource.saveBody(body) } returns expected.left()

        // When
        val actual = draftRepository.saveBody(userId, messageId, body)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft to recipient succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.addToRecipient(recipient) } returns Unit.right()

        // When
        val actual = draftRepository.addToRecipient(userId, messageId, recipient)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when save draft to recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.addToRecipient(recipient) } returns expected.left()

        // When
        val actual = draftRepository.addToRecipient(userId, messageId, recipient)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft cc recipient succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.addCcRecipient(recipient) } returns Unit.right()

        // When
        val actual = draftRepository.addCcRecipient(userId, messageId, recipient)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when save draft cc recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.addCcRecipient(recipient) } returns expected.left()

        // When
        val actual = draftRepository.addCcRecipient(userId, messageId, recipient)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft bcc recipient succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.addBccRecipient(recipient) } returns Unit.right()

        // When
        val actual = draftRepository.addBccRecipient(userId, messageId, recipient)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when save draft bcc recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.addBccRecipient(recipient) } returns expected.left()

        // When
        val actual = draftRepository.addBccRecipient(userId, messageId, recipient)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when remove draft to recipient succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.removeToRecipient(recipient) } returns Unit.right()

        // When
        val actual = draftRepository.removeToRecipient(userId, messageId, recipient)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when remove draft to recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.removeToRecipient(recipient) } returns expected.left()

        // When
        val actual = draftRepository.removeToRecipient(userId, messageId, recipient)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when remove draft cc recipient succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.removeCcRecipient(recipient) } returns Unit.right()

        // When
        val actual = draftRepository.removeCcRecipient(userId, messageId, recipient)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when remove draft cc recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.removeCcRecipient(recipient) } returns expected.left()

        // When
        val actual = draftRepository.removeCcRecipient(userId, messageId, recipient)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when remove draft bcc recipient succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.removeBccRecipient(recipient) } returns Unit.right()

        // When
        val actual = draftRepository.removeBccRecipient(userId, messageId, recipient)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when remove draft bcc recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val expected = DataError.Local.SaveDraftError.Unknown
        val messageId = MessageIdSample.PlainTextMessage
        val recipient = RecipientSample.Bob
        coEvery { draftDataSource.removeBccRecipient(recipient) } returns expected.left()

        // When
        val actual = draftRepository.removeBccRecipient(userId, messageId, recipient)

        // Then
        assertEquals(expected.left(), actual)
    }
}
