package ch.protonmail.android.mailmailbox.domain.usecase

import android.graphics.Color
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.testdata.label.LabelTestData.buildLabel
import ch.protonmail.android.testdata.maillabel.MailLabelTestData.buildCustomFolder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.mailsettings.domain.entity.ViewMode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetMoveToBottomSheetActionsTest {

    private val conversationRepository = mockk<ConversationRepository>()
    private val messageRepository = mockk<MessageRepository>()
    private val labelRepository = mockk<LabelRepository>()

    private val getMoveToBottomSheetActions = GetMoveToBottomSheetActions(
        messageRepository,
        conversationRepository,
        labelRepository
    )

    @Before
    fun setUp() {
        mockkStatic(Color::class)
        every { Color.parseColor(any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Color::class)
    }

    @Test
    fun `gets available move to actions for messages when view mode for selected items is message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val messageIds = items.map { MessageId(it.value) }
        val viewMode = ViewMode.NoConversationGrouping
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery {
            messageRepository.getAvailableSystemMoveToActions(userId, labelId, messageIds)
        } returns expected.right()
        every { labelRepository.observeCustomFolders(userId) } returns flowOf(emptyList())

        // When
        val actual = getMoveToBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available move to actions for conversation when view mode for selected items is convo`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery {
            conversationRepository.getAvailableSystemMoveToActions(userId, labelId, convoIds)
        } returns expected.right()
        every { labelRepository.observeCustomFolders(userId) } returns flowOf(emptyList())


        // When
        val actual = getMoveToBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `merges available move to system actions with move to custom folder actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val customF0 = buildCustomFolder("0", level = 0, order = 0, parent = null, children = listOf("0.1", "0.2"))
        val customF01 = buildCustomFolder("0.1", level = 1, order = 0, parent = customF0)
        val customF02 = buildCustomFolder(
            "0.2", level = 1, order = 1, parent = customF0, children = listOf("0.2.1", "0.2.2")
        )
        val customF021 = buildCustomFolder("0.2.1", level = 2, order = 0, parent = customF02)
        val customF022 = buildCustomFolder("0.2.2", level = 2, order = 1, parent = customF02)
        val systemMoveTo = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        val expected = systemMoveTo + listOf(
            customF0,
            customF01,
            customF02,
            customF021,
            customF022
        )
        coEvery {
            conversationRepository.getAvailableSystemMoveToActions(userId, labelId, convoIds)
        } returns systemMoveTo.right()
        val labels = listOf(
            buildLabel(userId = userId, type = LabelType.MessageFolder, id = "0", order = 0),
            buildLabel(userId = userId, type = LabelType.MessageFolder, id = "0.1", order = 0, parentId = "0"),
            buildLabel(userId = userId, type = LabelType.MessageFolder, id = "0.2", order = 1, parentId = "0"),
            buildLabel(userId = userId, type = LabelType.MessageFolder, id = "0.2.1", order = 0, parentId = "0.2"),
            buildLabel(userId = userId, type = LabelType.MessageFolder, id = "0.2.2", order = 1, parentId = "0.2")
        )
        every { labelRepository.observeCustomFolders(userId) } returns flowOf(labels)

        // When
        val actual = getMoveToBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when getting custom actions fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expectedSystemActions = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0)
        )
        coEvery {
            conversationRepository.getAvailableSystemMoveToActions(userId, labelId, convoIds)
        } returns expectedSystemActions.right()
        every { labelRepository.observeCustomFolders(userId) } returns flowOf()

        // When
        val actual = getMoveToBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(DataError.Local.NoDataCached.left(), actual)
    }

    @Test
    fun `returns error when getting system actions fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MailboxItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val labels = listOf(buildLabel(userId = userId, type = LabelType.MessageFolder, id = "0", order = 0))
        val error = DataError.Local.Unknown.left()
        coEvery { conversationRepository.getAvailableSystemMoveToActions(userId, labelId, convoIds) } returns error
        every { labelRepository.observeCustomFolders(userId) } returns flowOf(labels)

        // When
        val actual = getMoveToBottomSheetActions(userId, labelId, items, viewMode)

        // Then
        assertEquals(error, actual)
    }
}
