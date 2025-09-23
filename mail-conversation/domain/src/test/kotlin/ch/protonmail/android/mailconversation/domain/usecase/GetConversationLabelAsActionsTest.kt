package ch.protonmail.android.mailconversation.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.repository.ConversationActionRepository
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.testdata.label.rust.LabelAsActionsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetConversationLabelAsActionsTest {

    private val actionRepository = mockk<ConversationActionRepository>()

    private val getConversationLabelAsActions = GetConversationLabelAsActions(actionRepository)

    @Test
    fun `returns available label as actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationIds = listOf(ConversationIdSample.Newsletter)
        val expected = LabelAsActionsTestData.onlySelectedActions
        coEvery {
            actionRepository.getAvailableLabelAsActions(userId, labelId, conversationIds)
        } returns expected.right()

        // When
        val actual = getConversationLabelAsActions(userId, labelId, conversationIds)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationIds = listOf(ConversationIdSample.Newsletter)
        val expected = DataError.Local.CryptoError.left()
        coEvery { actionRepository.getAvailableLabelAsActions(userId, labelId, conversationIds) } returns expected

        // When
        val actual = getConversationLabelAsActions(userId, labelId, conversationIds)

        // Then
        assertEquals(expected, actual)
    }
}
