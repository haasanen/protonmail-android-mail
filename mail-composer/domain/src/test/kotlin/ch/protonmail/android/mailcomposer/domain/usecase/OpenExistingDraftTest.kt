package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.model.DraftFieldsWithSyncStatus
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.testdata.composer.DraftFieldsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class OpenExistingDraftTest {

    private val draftRepository = mockk<DraftRepository>()

    private val openExistingDraft = OpenExistingDraft(draftRepository)

    @Test
    fun `returns success when init with existing draft succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val expected = DraftFieldsWithSyncStatus.Remote(DraftFieldsTestData.BasicDraftFields)
        coEvery { draftRepository.openDraft(userId, messageId) } returns expected.right()

        // When
        val actual = openExistingDraft(userId, messageId)

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
        val actual = openExistingDraft(userId, messageId)

        // Then
        assertEquals(expected.left(), actual)
    }

}
