package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.sample.ConversationSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationAvailableActions
import ch.protonmail.android.mailconversation.domain.usecase.ObserveConversation
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.MessageSample
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageAvailableActions
import ch.protonmail.android.mailmessage.domain.usecase.ObserveMessage
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import ch.protonmail.android.testdata.action.AvailableActionsTestData
import ch.protonmail.android.testdata.conversation.ConversationTestData.conversation
import ch.protonmail.android.testdata.message.MessageThemeOptionsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetMoreActionsBottomSheetDataTest {

    private val observeMessage = mockk<ObserveMessage>()
    private val observeConversation = mockk<ObserveConversation>()
    private val getConversationAvailableActions = mockk<GetConversationAvailableActions>()
    private val getMessageAvailableActions = mockk<GetMessageAvailableActions>()

    private val getMoreBottomSheetData = GetMoreActionsBottomSheetData(
        getMessageAvailableActions,
        getConversationAvailableActions,
        observeMessage,
        observeConversation
    )

    @Test
    fun `should return bottom sheet more actions data when all operations succeeded for message`() = runTest {
        // Given
        val themeOptions = MessageThemeOptionsTestData.darkOverrideLight
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Archive.labelId
        val messageId = MessageIdSample.PlainTextMessage
        val message = MessageSample.Invoice
        val availableActions = AvailableActionsTestData.replyActionsOnly
        coEvery {
            getMessageAvailableActions(userId, labelId, messageId, themeOptions)
        } returns availableActions.right()
        coEvery { observeMessage(userId, messageId) } returns flowOf(message.right())

        // When
        val actual = getMoreBottomSheetData.forMessage(userId, labelId, messageId, themeOptions)

        // Then
        val expected = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
            messageSender = message.sender.name,
            messageSubject = message.subject,
            messageIdInConversation = message.messageId.id,
            availableActions = availableActions,
            customizeToolbarAction = null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return empty bottom sheet action data when observing labels failed for message`() = runTest {
        // Given
        val themeOptions = MessageThemeOptionsTestData.darkOverrideLight
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Archive.labelId
        val messageId = MessageIdSample.PlainTextMessage
        val message = MessageSample.Invoice
        coEvery {
            getMessageAvailableActions(userId, labelId, messageId, themeOptions)
        } returns DataError.Local.NoDataCached.left()
        coEvery { observeMessage(userId, messageId) } returns flowOf(message.right())

        // When
        val actual = getMoreBottomSheetData.forMessage(userId, labelId, messageId, themeOptions)

        // Then
        assertNull(actual)
    }

    @Test
    fun `should return bottom sheet more actions data when all operations succeeded for conversation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Archive.labelId
        val conversationId = ConversationIdSample.WeatherForecast
        val conversation = ConversationSample.WeatherForecast
        val availableActions = AvailableActionsTestData.replyActionsOnly
        coEvery {
            getConversationAvailableActions(userId, labelId, listOf(conversationId))
        } returns availableActions.right()
        coEvery { observeConversation(userId, conversationId, labelId) } returns flowOf(conversation.right())

        // When
        val actual = getMoreBottomSheetData.forConversation(userId, labelId, conversationId)

        // Then
        val expected = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
            messageSender = conversation.senders.first().name,
            messageSubject = conversation.subject,
            messageIdInConversation = null,
            availableActions = availableActions,
            customizeToolbarAction = Action.CustomizeToolbar
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return empty bottom sheet action data when observing labels failed for conversation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Archive.labelId
        val conversationId = ConversationIdSample.WeatherForecast
        coEvery {
            getConversationAvailableActions(userId, labelId, listOf(conversationId))
        } returns DataError.Local.NoDataCached.left()
        coEvery { observeConversation(userId, conversationId, labelId) } returns flowOf(conversation.right())

        // When
        val actual = getMoreBottomSheetData.forConversation(userId, labelId, conversationId)

        // Then
        assertNull(actual)
    }
}
