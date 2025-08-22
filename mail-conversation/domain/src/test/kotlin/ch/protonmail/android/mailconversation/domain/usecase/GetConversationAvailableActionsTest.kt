package ch.protonmail.android.mailconversation.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.repository.ConversationActionRepository
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetConversationAvailableActionsTest {

    private val actionRepository = mockk<ConversationActionRepository>()

    private val getConversationAvailableActions = GetConversationAvailableActions(actionRepository)

    @Test
    fun `returns available actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Newsletter
        val expected = AvailableActions(
            listOf(Action.Reply, Action.Forward),
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive),
            listOf(Action.ViewHeaders)
        )
        coEvery {
            actionRepository.getAvailableActions(userId, labelId, conversationId)
        } returns expected.right()

        // When
        val actual = getConversationAvailableActions(userId, labelId, conversationId)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Newsletter
        val expected = DataError.Local.Unknown.left()
        coEvery { actionRepository.getAvailableActions(userId, labelId, conversationId) } returns expected

        // When
        val actual = getConversationAvailableActions(userId, labelId, conversationId)

        // Then
        assertEquals(expected, actual)
    }
}
