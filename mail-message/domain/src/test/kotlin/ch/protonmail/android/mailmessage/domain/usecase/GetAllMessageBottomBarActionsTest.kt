package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
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

class GetAllMessageBottomBarActionsTest {

    private val actionRepository = mockk<MessageActionRepository>()

    private val getAllMessageBottomBarActions = GetAllMessageBottomBarActions(actionRepository)

    @Test
    fun `returns available actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageIds = listOf(MessageIdSample.AlphaAppQAReport)
        val expected = AllBottomBarActions(
            listOf(Action.MarkRead, Action.MarkUnread),
            listOf(Action.Star, Action.Label)
        )
        coEvery { actionRepository.getAllListBottomBarActions(userId, labelId, messageIds) } returns expected.right()

        // When
        val actual = getAllMessageBottomBarActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val messageIds = listOf(MessageIdSample.AlphaAppQAReport)
        val expected = DataError.Local.CryptoError.left()
        coEvery { actionRepository.getAllListBottomBarActions(userId, labelId, messageIds) } returns expected

        // When
        val actual = getAllMessageBottomBarActions(userId, labelId, messageIds)

        // Then
        assertEquals(expected, actual)
    }
}
