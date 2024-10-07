package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationAvailableActions
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageAvailableActions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.mailsettings.domain.entity.ViewMode
import org.junit.Test
import kotlin.test.assertEquals

class GetBottomSheetActionsTest {

    private val getConversationAvailableActions = mockk<GetConversationAvailableActions>()
    private val getMessageAvailableActions = mockk<GetMessageAvailableActions>()

    private val getBottomSheetActions = GetBottomSheetActions(
        getMessageAvailableActions,
        getConversationAvailableActions
    )

    @Test
    fun `gets available actions for messages when view mode for selected items is message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val messageIds = items.map { MessageId(it.value) }
        val viewMode = ViewMode.NoConversationGrouping
        val expected = AvailableActions(
            listOf(Action.Reply, Action.Forward),
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive),
            listOf(Action.ViewHeaders)
        )
        coEvery { getMessageAvailableActions(userId, labelId, messageIds) } returns expected.right()

        // When
        val actual = getBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available actions for conversations when view mode for selected items is conversation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = AvailableActions(
            listOf(Action.Reply, Action.Forward),
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive),
            listOf(Action.ViewHeaders)
        )
        coEvery { getConversationAvailableActions(userId, labelId, convoIds) } returns expected.right()

        // When
        val actual = getBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when failing to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = DataError.Local.Unknown.left()
        coEvery { getConversationAvailableActions(userId, labelId, convoIds) } returns expected

        // When
        val actual = getBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected, actual)
    }

}
