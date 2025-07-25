package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetAllConversationBottomBarActions
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetAllMessageBottomBarActions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.maillabel.domain.model.ViewMode
import kotlinx.coroutines.flow.flow
import org.junit.Test
import kotlin.test.assertEquals

class GetBottomSheetActionsTest {

    private val getAllConversationBottomBarActions = mockk<GetAllConversationBottomBarActions>()
    private val getAllMessageBottomBarActions = mockk<GetAllMessageBottomBarActions>()

    private var snoozeEnabled = false
    private val getBottomSheetActions = GetBottomSheetActions(
        getAllMessageBottomBarActions,
        getAllConversationBottomBarActions,
        flow {
            emit(snoozeEnabled)
        }
    )

    @Test
    fun `gets available actions for messages when view mode for selected items is message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val messageIds = items.map { MessageId(it.value) }
        val viewMode = ViewMode.NoConversationGrouping
        val expected = AllBottomBarActions(
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive)
        )
        coEvery { getAllMessageBottomBarActions(userId, labelId, messageIds) } returns expected.right()

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
        val expected = AllBottomBarActions(
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive)
        )
        coEvery { getAllConversationBottomBarActions(userId, labelId, convoIds) } returns expected.right()

        // When
        val actual = getBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available actions for conversations when snooze enabled`() = runTest {
        // Given
        snoozeEnabled = true
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = AllBottomBarActions(
            listOf(Action.Star, Action.Label, Action.Snooze),
            listOf(Action.Spam, Action.Archive)
        )

        val input = AllBottomBarActions(
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive)
        )
        coEvery { getAllConversationBottomBarActions(userId, labelId, convoIds) } returns input.right()

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
        coEvery { getAllConversationBottomBarActions(userId, labelId, convoIds) } returns expected

        // When
        val actual = getBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `removes 'More' Action from returned list of visible BottomBar actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val allActions = AllBottomBarActions(
            listOf(Action.Star, Action.Label, Action.More),
            listOf(Action.Spam, Action.Archive, Action.More)
        )
        coEvery { getAllConversationBottomBarActions(userId, labelId, convoIds) } returns allActions.right()

        // When
        val actual = getBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        val expected = AllBottomBarActions(
            listOf(Action.Star, Action.Label),
            listOf(Action.Spam, Action.Archive)
        )
        assertEquals(expected.right(), actual)
    }


}
