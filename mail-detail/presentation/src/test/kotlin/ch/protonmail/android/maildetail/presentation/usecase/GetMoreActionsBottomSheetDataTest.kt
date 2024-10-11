package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationAvailableActions
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.MessageSample
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageAvailableActions
import ch.protonmail.android.mailmessage.domain.usecase.ObserveMessage
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import ch.protonmail.android.testdata.action.AvailableActionsTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetMoreActionsBottomSheetDataTest {

    private val observeMessage = mockk<ObserveMessage>()
    private val getConversationAvailableActions = mockk<GetConversationAvailableActions>()
    private val getMessageAvailableActions = mockk<GetMessageAvailableActions>()

    private val getMoreBottomSheetData = GetMoreActionsBottomSheetData(
        getMessageAvailableActions,
        getConversationAvailableActions,
        observeMessage
    )

    @Test
    fun `should return bottom sheet more actions data when all operations succeeded for message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Archive.labelId
        val messageId = MessageIdSample.PlainTextMessage
        val message = MessageSample.Invoice
        val availableActions = AvailableActionsTestData.replyActionsOnly
        coEvery {
            getMessageAvailableActions(userId, labelId, listOf(messageId))
        } returns availableActions.right()
        coEvery { observeMessage(userId, messageId) } returns flowOf(message.right())

        // When
        val actual = getMoreBottomSheetData.forMessage(userId, labelId, messageId)

        // Then
        val expected = DetailMoreActionsBottomSheetState.DetailMoreActionsBottomSheetEvent.DataLoaded(
            messageSender = message.sender.name,
            messageSubject = message.subject,
            messageIdInConversation = message.messageId.id,
            participantsCount = message.allRecipientsDeduplicated.size,
            availableActions = availableActions
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should return empty bottom sheet action data when observing labels failed for message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = SystemLabelId.Archive.labelId
        val messageId = MessageIdSample.PlainTextMessage
        val message = MessageSample.Invoice
        coEvery {
            getMessageAvailableActions(userId, labelId, listOf(messageId))
        } returns DataError.Local.NoDataCached.left()
        coEvery { observeMessage(userId, messageId) } returns flowOf(message.right())

        // When
        val actual = getMoreBottomSheetData.forMessage(userId, labelId, messageId)

        // Then
        assertNull(actual)
    }
}
