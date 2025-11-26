package ch.protonmail.android.mailconversation.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
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

internal class GetAllConversationBottomBarActionsTest {

    private val actionRepository = mockk<ConversationActionRepository>()

    private val getAllConversationBottomBarActions = GetAllConversationBottomBarActions(actionRepository)

    @Test
    fun `returns available actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Newsletter
        val expected = AllBottomBarActions(
            listOf(Action.MarkRead, Action.MarkUnread),
            listOf(Action.Star, Action.Label)
        ).right()

        coEvery {
            actionRepository.getAllBottomBarActions(userId, labelId, conversationId)
        } returns expected

        // When
        val actual = getAllConversationBottomBarActions(userId, labelId, conversationId)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Newsletter
        val expected = DataError.Local.CryptoError.left()
        coEvery {
            actionRepository.getAllBottomBarActions(userId, labelId, conversationId)
        } returns expected

        // When
        val actual = getAllConversationBottomBarActions(userId, labelId, conversationId)

        // Then
        assertEquals(expected, actual)
    }
}
