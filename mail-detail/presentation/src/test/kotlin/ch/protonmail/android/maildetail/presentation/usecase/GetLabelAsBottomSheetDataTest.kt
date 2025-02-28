package ch.protonmail.android.maildetail.presentation.usecase

import android.graphics.Color
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationLabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.presentation.MailLabelUiModel
import ch.protonmail.android.maillabel.presentation.toUiModel
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageLabelAsActions
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetEntryPoint
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import ch.protonmail.android.testdata.label.rust.LabelAsActionsTestData
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLabelAsBottomSheetDataTest {

    private val userId = UserIdTestData.userId

    private val getConversationLabelAsActions = mockk<GetConversationLabelAsActions>()
    private val getMessageLabelAsActions = mockk<GetMessageLabelAsActions>()

    private val getLabelAsBottomSheetData = GetLabelAsBottomSheetData(
        getMessageLabelAsActions,
        getConversationLabelAsActions
    )

    @Before
    fun setUp() {
        mockkStatic(Color::parseColor)
        every { Color.parseColor(any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Color::parseColor)
    }

    @Test
    fun `should return bottom sheet action data when all operations succeeded for message`() = runTest {
        // Given
        val labelId = SystemLabelId.Trash.labelId
        val messageId = MessageIdSample.PlainTextMessage
        coEvery {
            getMessageLabelAsActions(userId, labelId, listOf(messageId))
        } returns LabelAsActionsTestData.unselectedActions.right()

        // When
        val actual = getLabelAsBottomSheetData.forMessage(userId, labelId, messageId)

        // Then
        val expected = LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
            customLabelList = listOf(
                MailLabelTestData.document, MailLabelTestData.label2021, MailLabelTestData.label2022
            ).map {
                it.toUiModel(
                    emptyMap(), MailLabelTestData.inboxSystemLabel.id
                ) as MailLabelUiModel.Custom
            }.toImmutableList(),
            selectedLabels = emptyList<LabelId>().toImmutableList(),
            entryPoint = LabelAsBottomSheetEntryPoint.Message(messageId)
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return empty bottom sheet action data when observing labels failed for message`() = runTest {
        // Given
        val labelId = SystemLabelId.Trash.labelId
        val messageId = MessageIdSample.PlainTextMessage
        coEvery {
            getMessageLabelAsActions(userId, labelId, listOf(messageId))
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = getLabelAsBottomSheetData.forMessage(userId, labelId, messageId)

        // Then
        val expected = LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
            customLabelList = emptyList<MailLabelUiModel.Custom>().toImmutableList(),
            selectedLabels = emptyList<LabelId>().toImmutableList(),
            entryPoint = LabelAsBottomSheetEntryPoint.Message(messageId)
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return bottom sheet action data when all operations succeeded for conversation`() = runTest {
        // Given
        val labelId = SystemLabelId.Trash.labelId
        val conversationId = ConversationIdSample.Invoices
        coEvery {
            getConversationLabelAsActions(userId, labelId, listOf(conversationId))
        } returns LabelAsActionsTestData.unselectedActions.right()

        // When
        val actual = getLabelAsBottomSheetData.forConversation(userId, labelId, conversationId)

        // Then
        val expected = LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
            customLabelList = listOf(
                MailLabelTestData.document, MailLabelTestData.label2021, MailLabelTestData.label2022
            ).map {
                it.toUiModel(
                    emptyMap(), MailLabelTestData.inboxSystemLabel.id
                ) as MailLabelUiModel.Custom
            }.toImmutableList(),
            selectedLabels = emptyList<LabelId>().toImmutableList(),
            entryPoint = LabelAsBottomSheetEntryPoint.Conversation
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return empty bottom sheet action data when observing labels failed for conversation`() = runTest {
        // Given
        val labelId = SystemLabelId.Trash.labelId
        val conversationId = ConversationIdSample.Invoices
        coEvery {
            getConversationLabelAsActions(userId, labelId, listOf(conversationId))
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = getLabelAsBottomSheetData.forConversation(userId, labelId, conversationId)

        // Then
        val expected = LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
            customLabelList = emptyList<MailLabelUiModel.Custom>().toImmutableList(),
            selectedLabels = emptyList<LabelId>().toImmutableList(),
            entryPoint = LabelAsBottomSheetEntryPoint.Conversation
        )
        assertEquals(expected, actual)
    }

}
