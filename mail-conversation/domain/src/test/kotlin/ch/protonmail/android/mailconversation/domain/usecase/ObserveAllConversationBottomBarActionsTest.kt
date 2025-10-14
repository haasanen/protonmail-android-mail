package ch.protonmail.android.mailconversation.domain.usecase

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.ConversationIdSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.entity.ConversationDetailEntryPoint
import ch.protonmail.android.mailconversation.domain.repository.ConversationActionRepository
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ObserveAllConversationBottomBarActionsTest {

    private val actionRepository = mockk<ConversationActionRepository>()

    private val observeAllConversationBottomBarActions = ObserveAllConversationBottomBarActions(actionRepository)

    @Test
    fun `returns available actions when repo succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Newsletter
        val expected = flowOf(
            AllBottomBarActions(
                listOf(Action.MarkRead, Action.MarkUnread),
                listOf(Action.Star, Action.Label)
            ).right()
        )
        val entryPoint = ConversationDetailEntryPoint.Mailbox
        val showAll = false
        coEvery {
            actionRepository.observeAllBottomBarActions(userId, labelId, conversationId, entryPoint, showAll)
        } returns expected

        // When
        val actual = observeAllConversationBottomBarActions(userId, labelId, conversationId, entryPoint, showAll)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `returns available actions updated when new data is available`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Newsletter
        val sharedFlow = MutableSharedFlow<Either<DataError, AllBottomBarActions>>()
        val entryPoint = ConversationDetailEntryPoint.Mailbox
        val showAll = false

        val expectedFirst =
            AllBottomBarActions(listOf(Action.MarkRead, Action.MarkUnread), listOf(Action.Star, Action.Label)).right()

        val expectedSecond =
            AllBottomBarActions(
                listOf(Action.Star, Action.Move),
                listOf(Action.CustomizeToolbar, Action.Delete)
            ).right()

        coEvery {
            actionRepository.observeAllBottomBarActions(userId, labelId, conversationId, entryPoint, showAll)
        } returns sharedFlow

        // When + Then
        observeAllConversationBottomBarActions(userId, labelId, conversationId, entryPoint, showAll).test {
            sharedFlow.emit(expectedFirst)
            assertEquals(expectedFirst, awaitItem())
            sharedFlow.emit(expectedSecond)
            assertEquals(expectedSecond, awaitItem())
        }
    }

    @Test
    fun `returns error when repository fails to get available actions`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val conversationId = ConversationIdSample.Newsletter
        val expected = flowOf(DataError.Local.CryptoError.left())
        val entryPoint = ConversationDetailEntryPoint.Mailbox
        val showAll = false
        coEvery {
            actionRepository.observeAllBottomBarActions(
                userId,
                labelId,
                conversationId,
                entryPoint,
                showAll
            )
        } returns expected

        // When
        val actual = observeAllConversationBottomBarActions(userId, labelId, conversationId, entryPoint, showAll)

        // Then
        assertEquals(expected, actual)
    }
}
