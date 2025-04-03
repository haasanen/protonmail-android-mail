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
        val messageId = MessageIdSample.PlainTextMessage
        coEvery { draftDataSource.save() } returns messageId.right()

        // When
        val actual = draftRepository.save()

        // Then
        assertEquals(messageId.right(), actual)
    }

    @Test
    fun `returns error when save draft fails`() = runTest {
        // Given
        val expected = DataError.Local.SaveDraftError.Unknown
        coEvery { draftDataSource.save() } returns expected.left()

        // When
        val actual = draftRepository.save()

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft subject succeeds`() = runTest {
        // Given
        val messageId = MessageIdSample.PlainTextMessage
        val subject = Subject("test subject")
        coEvery { draftDataSource.saveSubject(subject) } returns messageId.right()

        // When
        val actual = draftRepository.saveSubject(subject)

        // Then
        assertEquals(messageId.right(), actual)
    }

    @Test
    fun `returns error when save draft subject fails`() = runTest {
        // Given
        val expected = DataError.Local.SaveDraftError.Unknown
        val subject = Subject("test subject")
        coEvery { draftDataSource.saveSubject(subject) } returns expected.left()

        // When
        val actual = draftRepository.saveSubject(subject)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when save draft body succeeds`() = runTest {
        // Given
        val messageId = MessageIdSample.PlainTextMessage
        val body = DraftBody("test body")
        coEvery { draftDataSource.saveBody(body) } returns messageId.right()

        // When
        val actual = draftRepository.saveBody(body)

        // Then
        assertEquals(messageId.right(), actual)
    }

    @Test
    fun `returns error when save draft body fails`() = runTest {
        // Given
        val expected = DataError.Local.SaveDraftError.Unknown
        val body = DraftBody("test body")
        coEvery { draftDataSource.saveBody(body) } returns expected.left()

        // When
        val actual = draftRepository.saveBody(body)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when send draft succeeds`() = runTest {
        // Given
        coEvery { draftDataSource.send() } returns Unit.right()

        // When
        val actual = draftRepository.send()

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `returns error when send draft fails`() = runTest {
        // Given
        val expected = DataError.Local.Unknown
        coEvery { draftDataSource.send() } returns expected.left()

        // When
        val actual = draftRepository.send()

        // Then
        assertEquals(expected.left(), actual)
    }

}
