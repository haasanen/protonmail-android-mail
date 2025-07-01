package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.model.OpenDraftError
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.testdata.composer.DraftFieldsTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CreateEmptyDraftTest {

    private val draftRepository = mockk<DraftRepository>()

    private val createEmptyDraft = CreateEmptyDraft(draftRepository)

    @Test
    fun `returns success when init with new empty draft action succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val action = DraftAction.Compose
        val expected = DraftFieldsTestData.BasicDraftFields
        coEvery { draftRepository.createDraft(userId, action) } returns expected.right()

        // When
        val actual = createEmptyDraft(userId)

        // Then
        coVerify { draftRepository.createDraft(userId, action) }
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when init with new empty draft fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val action = DraftAction.Compose
        val expected = OpenDraftError.OpenDraftFailed
        coEvery { draftRepository.createDraft(userId, action) } returns expected.left()

        // When
        val actual = createEmptyDraft(userId)

        // Then
        assertEquals(expected.left(), actual)
    }
}
