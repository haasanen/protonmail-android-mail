package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.testdata.label.rust.LabelAsActionsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.mailsettings.domain.entity.ViewMode
import org.junit.Test
import kotlin.test.assertEquals

class GetLabelAsBottomSheetContentTest {

    private val conversationRepository = mockk<ConversationRepository>()
    private val messageRepository = mockk<MessageRepository>()

    private val getLabelAsBottomSheetContent = GetLabelAsBottomSheetContent(
        messageRepository,
        conversationRepository
    )

    @Test
    fun `gets available label as actions for messages when view mode for selected items is message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val messageIds = items.map { MessageId(it.value) }
        val viewMode = ViewMode.NoConversationGrouping
        val expected = LabelAsActionsTestData.onlySelectedActions
        coEvery { messageRepository.getAvailableLabelAsActions(userId, labelId, messageIds) } returns expected.right()

        // When
        val actual = getLabelAsBottomSheetContent(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available label actions for conversations when view mode for selected items is conversation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = LabelAsActionsTestData.onlySelectedActions
        coEvery {
            conversationRepository.getAvailableLabelAsActions(userId, labelId, convoIds)
        } returns expected.right()

        // When
        val actual = getLabelAsBottomSheetContent(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when failing to get available label as actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = DataError.Local.Unknown.left()
        coEvery { conversationRepository.getAvailableLabelAsActions(userId, labelId, convoIds) } returns expected

        // When
        val actual = getLabelAsBottomSheetContent(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected, actual)
    }

}
