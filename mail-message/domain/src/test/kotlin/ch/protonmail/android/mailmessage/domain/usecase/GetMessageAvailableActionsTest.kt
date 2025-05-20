package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.domain.repository.MessageActionRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetMessageAvailableActionsTest {

    private val actionRepository = mockk<MessageActionRepository>()

    private val getMessageAvailableActions = GetMessageAvailableActions(actionRepository)

    @Test
    fun `returns available actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageId = MessageIdSample.AlphaAppQAReport
        val expected = AvailableActions(
            listOf(Action.Reply, Action.Forward),
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive),
            listOf(Action.ViewHeaders)
        )
        coEvery { actionRepository.getAvailableActions(userId, labelId, messageId) } returns expected.right()

        // When
        val actual = getMessageAvailableActions(userId, labelId, messageId)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageId = MessageIdSample.AlphaAppQAReport
        val expected = DataError.Local.Unknown.left()
        coEvery { actionRepository.getAvailableActions(userId, labelId, messageId) } returns expected

        // When
        val actual = getMessageAvailableActions(userId, labelId, messageId)

        // Then
        assertEquals(expected, actual)
    }
}
