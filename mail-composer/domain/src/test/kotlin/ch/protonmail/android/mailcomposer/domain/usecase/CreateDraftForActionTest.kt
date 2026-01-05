package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.model.OpenDraftError
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.composer.DraftFieldsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CreateDraftForActionTest {

    private val draftRepository = mockk<DraftRepository>()

    private val createDraftForAction = CreateDraftForAction(draftRepository)


    @Test
    fun `returns success when init with draft action succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val parentMessageId = MessageIdSample.PlainTextMessage
        val action = DraftAction.Reply(parentMessageId)
        val expected = DraftFieldsTestData.BasicDraftFields
        coEvery { draftRepository.createDraft(userId, action) } returns expected.right()

        // When
        val actual = createDraftForAction(userId, action)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when init with draft action fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val parentMessageId = MessageIdSample.PlainTextMessage
        val action = DraftAction.Forward(parentMessageId)
        val expected = OpenDraftError.DraftDoesNotExist
        coEvery { draftRepository.createDraft(userId, action) } returns expected.left()

        // When
        val actual = createDraftForAction(userId, action)

        // Then
        assertEquals(expected.left(), actual)
    }
}
