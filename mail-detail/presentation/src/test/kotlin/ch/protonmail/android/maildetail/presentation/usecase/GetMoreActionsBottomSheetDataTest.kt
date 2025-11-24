package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.sample.ConversationSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationAvailableActions
import ch.protonmail.android.maildetail.presentation.model.MoreActionsBottomSheetEntryPoint
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.MessageSample
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageAvailableActions
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import ch.protonmail.android.testdata.action.AvailableActionsTestData
import ch.protonmail.android.testdata.conversation.ConversationTestData.conversation
import ch.protonmail.android.testdata.message.MessageThemeOptionsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetMoreActionsBottomSheetDataTest {

    private val getConversationAvailableActions = mockk<GetConversationAvailableActions>()
    private val getMessageAvailableActions = mockk<GetMessageAvailableActions>()

    private val getMoreBottomSheetData = GetMoreActionsBottomSheetData(
        getMessageAvailableActions,
        getConversationAvailableActions
    )

    @Test
    fun `should return bottom sheet more actions data when all operations succeeded for message`() = runTest {
        // Given
        val themeOptions = MessageThemeOptionsTestData.darkOverrideLight
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Archive.labelId
        val messageId = MessageSample.Invoice.messageId
        val message = MessageSample.Invoice
        val availableActions = AvailableActionsTestData.replyActionsOnly

        val payload = MoreMessageActionsBottomSheetDataPayload(
            userId = userId,
            labelId = labelId,
            messageId = messageId,
            messageThemeOptions = themeOptions,
            entryPoint = MoreActionsBottomSheetEntryPoint.MessageHeader,
            subject = message.subject
        )
        coEvery {
            getMessageAvailableActions(userId, labelId, messageId, themeOptions)
        } returns availableActions.right()

        // When
        val actual = getMoreBottomSheetData.forMessage(payload)

        // Then
        val expected = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
            messageSubject = message.subject,
            messageIdInConversation = message.messageId.id,
            availableActions = availableActions,
            customizeToolbarAction = null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return bottom sheet more actions data with customize toolbar when entry point is bottombar`() =
        runTest {
            // Given
            val themeOptions = MessageThemeOptionsTestData.darkOverrideLight
            val userId = UserIdSample.Primary
            val labelId = SystemLabelId.Archive.labelId
            val message = MessageSample.Invoice
            val messageId = message.messageId
            val availableActions = AvailableActionsTestData.replyActionsOnly

            val payload = MoreMessageActionsBottomSheetDataPayload(
                userId = userId,
                labelId = labelId,
                messageId = messageId,
                messageThemeOptions = themeOptions,
                entryPoint = MoreActionsBottomSheetEntryPoint.BottomBar,
                subject = message.subject
            )
            coEvery {
                getMessageAvailableActions(userId, labelId, messageId, themeOptions)
            } returns availableActions.right()

            // When
            val actual = getMoreBottomSheetData.forMessage(payload)

            // Then
            val expected = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
                messageSubject = message.subject,
                messageIdInConversation = message.messageId.id,
                availableActions = availableActions,
                customizeToolbarAction = Action.CustomizeToolbar
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
        val entryPoint = MoreActionsBottomSheetEntryPoint.MessageHeader

        val payload = MoreMessageActionsBottomSheetDataPayload(
            userId = userId,
            labelId = labelId,
            messageId = messageId,
            messageThemeOptions = themeOptions,
            entryPoint = entryPoint,
            subject = message.subject
        )

        coEvery {
            getMessageAvailableActions(userId, labelId, messageId, themeOptions)
        } returns DataError.Local.NoDataCached.left()

        // When
        val actual = getMoreBottomSheetData.forMessage(payload)

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
            getConversationAvailableActions(userId, labelId, conversationId)
        } returns availableActions.right()

        val payload = MoreConversationActionsBottomSheetDataPayload(
            userId = userId,
            labelId = labelId,
            subject = conversation.subject,
            conversationId = conversationId
        )

        // When
        val actual = getMoreBottomSheetData.forConversation(payload)

        // Then
        val expected = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
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
            getConversationAvailableActions(userId, labelId, conversationId)
        } returns DataError.Local.NoDataCached.left()

        val payload = MoreConversationActionsBottomSheetDataPayload(
            userId = userId,
            labelId = labelId,
            subject = conversation.subject,
            conversationId = conversationId
        )

        // When
        val actual = getMoreBottomSheetData.forConversation(payload)

        // Then
        assertNull(actual)
    }
}
