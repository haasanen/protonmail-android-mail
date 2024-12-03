package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.composer.DraftFieldsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class InitializeComposerStateTest {

    private val draftRepository = mockk<DraftRepository>()

    private val initializeComposerState = InitializeComposerState(draftRepository)

    @Test
    fun `returns success when init with existing draft succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val expected = DraftFieldsTestData.BasicDraftFields
        coEvery { draftRepository.openDraft(userId, messageId) } returns expected.right()

        // When
        val actual = initializeComposerState.withExistingDraft(userId, messageId)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when init with existing draft fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val expected = DataError.Local.Unknown
        coEvery { draftRepository.openDraft(userId, messageId) } returns expected.left()

        // When
        val actual = initializeComposerState.withExistingDraft(userId, messageId)

        // Then
        assertEquals(expected.left(), actual)
    }

    @Test
    fun `returns success when init with draft action succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val parentMessageId = MessageIdSample.PlainTextMessage
        val action = DraftAction.Reply(parentMessageId)
        val expected = DraftFieldsTestData.BasicDraftFields
        coEvery { draftRepository.createDraft(userId, action) } returns expected.right()

        // When
        val actual = initializeComposerState.withDraftAction(userId, action)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when init with draft action fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val parentMessageId = MessageIdSample.PlainTextMessage
        val action = DraftAction.Forward(parentMessageId)
        val expected = DataError.Local.Unknown
        coEvery { draftRepository.createDraft(userId, action) } returns expected.left()

        // When
        val actual = initializeComposerState.withDraftAction(userId, action)

        // Then
        assertEquals(expected.left(), actual)
    }

}
